package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import io.github.haidarim.shard.utils.CacheUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;
import static io.github.haidarim.shard.utils.CacheUtils.*;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class ShardNodeCacheManagerImpl implements ShardNodeCacheManager {

    // L1
    private final Cache<@NonNull Long, ShardNodeModel> nodeCache;
    private final Cache<@NonNull Integer, Set<Long>> shardIndexCache;

    // L2
    private final RedisTemplate<String, ShardNodeModel> nodeRedisCache;
    private final RedisTemplate<String, String> shardIndexRedisCache;

    // L3
    private final ShardNodeRepository repository;

    // Synchronization locks
    private final Map<Long, ReentrantLock> nodeLocks = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> shardLocks = new ConcurrentHashMap<>();

    public ShardNodeCacheManagerImpl(
            Cache<@NonNull Long, ShardNodeModel> nodeCache,
            @Qualifier("shardNodeIdByShardIdCache") Cache<@NonNull Integer, Set<Long>> shardIndexCache,
            RedisTemplate<String, ShardNodeModel> nodeRedisCache,
            @Qualifier("redisShardIndexStringTemplate") RedisTemplate<String, String> shardIndexRedisCache,
            ShardNodeRepository repository
    ){
        this.nodeCache = nodeCache;
        this.shardIndexCache = shardIndexCache;
        this.nodeRedisCache = nodeRedisCache;
        this.shardIndexRedisCache = shardIndexRedisCache;
        this.repository = repository;
    }


    @Override
    public ShardNodeModel getNode(Long nodeId) {
        ReentrantLock lock = nodeLocks.computeIfAbsent(nodeId, key -> new ReentrantLock());
        lock.lock();

        ShardNodeModel model = nodeCache.getIfPresent(nodeId);

        if(model == null) {
            try {
                model = resolveNode(nodeId);
            } finally {
                lock.unlock();
                removeLock(nodeLocks, nodeId, lock);
            }
        }

        return model;
    }

    public Set<ShardNodeModel> getNodes(Integer shardId){
        ReentrantLock lock = shardLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        lock.lock();

        // check L1
        Set<ShardNodeModel> models = resolveLocalNodes(shardIndexCache.getIfPresent(shardId));

        if(models.isEmpty()) {
            try {
                models = resolveNodes(shardId);
            } finally {
                lock.unlock();
                removeLock(shardLocks, shardId, lock);
            }
        }

        return models;
    }


    @Override
    public void removeFromCaffeine(Long nodeId) {
        ShardNodeModel model = nodeCache.getIfPresent(nodeId);
        if (model == null) {
            return;
        }
        nodeCache.invalidate(nodeId);
        removeFromShardIndexCache(model.getShardId(), nodeId);
    }

    @Override
    public void clear(){
        clearCaffeineCache();
        clearRedisCache();
    }

    @Override
    public void clearCaffeineCache() {
        nodeCache.invalidateAll();
        shardIndexCache.invalidateAll();
    }

    private void clearRedisCache(){
        Set<String> nodeKeys = getRedisKeys(nodeRedisCache, ALL_SHARD_NODE_KEYS);
        if(!nodeKeys.isEmpty()){
            nodeRedisCache.delete(nodeKeys);
        }

        Set<String> indexKeys = getRedisKeys(shardIndexRedisCache, ALL_SHARD_NODE_INDEX_KEYS);
        if (!indexKeys.isEmpty()) {
            shardIndexRedisCache.delete(indexKeys);
        }
    }

    @Override
    public void refresh(){
        Set<ShardNodeModel> nodes = repository.findAll()
                .stream()
                .map(CacheUtils::mapToShardNodeModel).collect(Collectors.toSet());
        clear();
        applyToCaffeineCaches(nodes);
        applyToSharedRedisCaches(nodes);
    }

    @Override
    public void applyToSharedRedisCaches(ShardNodeModel model){
        if (model == null){
            return;
        }

        nodeRedisCache.opsForValue().set(
                shardNode(model.getNodeId()),
                model
        );

        shardIndexRedisCache.opsForSet().add(
                shardNodeByShardId(model.getShardId()),
                model.getNodeId().toString()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyToSharedRedisCaches(Set<ShardNodeModel> models){
        if (models == null || models.isEmpty()){
            return;
        }

        Map<String, ShardNodeModel> nodeValues = models
                .stream()
                .collect(Collectors.toMap(
                        model -> shardNode(model.getNodeId()),
                        model -> model
                ));
        nodeRedisCache.opsForValue().multiSet(nodeValues);

        Map<Integer, Set<String>> shardIndexValues = models
                .stream()
                .collect(Collectors.groupingBy(
                        ShardNodeModel::getShardId,
                        Collectors.mapping(
                                model -> model.getNodeId().toString(),
                                Collectors.toSet()
                        )
                ));

        shardIndexRedisCache.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) shardIndexRedisCache.getKeySerializer();

            shardIndexValues.forEach((shardId, nodeIds) -> {
                byte[] rawKey = keySerializer.serialize(shardNodeByShardId(shardId));

                byte[][] rawMembers = nodeIds.stream()
                        .map(id -> id.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toArray(byte[][]::new);

                connection.setCommands().sAdd(rawKey, rawMembers);
            });
            return null;
        });
    }

    @Override
    public void applyToCaffeineCaches(Set<ShardNodeModel> nodeModels){
        if (nodeModels == null || nodeModels.isEmpty()){
            return;
        }

        nodeModels.forEach(this::putNodeInLocalCaches);
    }

    @Override
    public void removeFromRedisCache(Long nodeId) {
        ShardNodeModel model = nodeRedisCache.opsForValue().get(shardNode(nodeId));
        if(model == null){
            return;
        }

        nodeRedisCache.delete(shardNode(nodeId));
        shardIndexRedisCache
                .opsForSet()
                .remove(
                        shardNodeByShardId(model.getShardId()),
                        nodeId.toString()
                );
    }

    @Override
    public Map<Long, ShardNodeModel> getAll(){
        return Map.copyOf(nodeCache.asMap());
    }

    private ShardNodeModel resolveNode(Long nodeId){
        // L2
        ShardNodeModel model = nodeRedisCache.opsForValue().get(shardNode(nodeId));
        if (model != null) {
            putNodeInLocalCaches(model);
            return model;
        }
        // L3
        return fetchFromDbAndUpdateCache(nodeId);
    }

    private Set<ShardNodeModel> resolveNodes(Integer shardId){
        //L2
        Set<String> redisNodeIds = shardIndexRedisCache.opsForSet()
                .members(shardNodeByShardId(shardId));
        if (redisNodeIds != null && !redisNodeIds.isEmpty()) {

            Set<Long> nodeIds = redisNodeIds.stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toUnmodifiableSet());

            shardIndexCache.put(shardId, nodeIds);

            // L2 - resolve actual node models
            Set<ShardNodeModel> models = getModelsFromRedisCache(nodeIds);
            if (!models.isEmpty()) {
                return models;
            }
        }

        // L3
        return fetchNodesFromDbAndUpdateCache(shardId);
    }

    private void putNodeInLocalCaches(ShardNodeModel model){
        nodeCache.put(model.getNodeId(), model);

        shardIndexCache.asMap().compute(
                model.getShardId(), (sharId, current) ->{
                    Set<Long> updated = current == null ? new HashSet<>() : new HashSet<>(current);

                    updated.add(model.getNodeId());
                    return Set.copyOf(updated);
                }
        );
    }

    private Set<ShardNodeModel> resolveLocalNodes(Set<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()){
            return Set.of();
        }

        Set<ShardNodeModel> models = nodeIds.stream()
                .map(nodeCache::getIfPresent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (models.size() == nodeIds.size()) {
            return models;
        }

        return Set.of();
    }

    private Set<ShardNodeModel> getModelsFromRedisCache(Set<Long> nodeIds){
        List<String> keys = nodeIds.stream()
                .map(CacheProperty::shardNode)
                .toList();

        List<ShardNodeModel> models =
                nodeRedisCache.opsForValue().multiGet(keys);

        if (models != null && !models.isEmpty()) {
            Set<ShardNodeModel> result = models.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Populate L1 node cache
            result.forEach(this::putNodeInLocalCaches);

            if (result.size() == nodeIds.size()) {
                return result;
            }
        }

        return Set.of();
    }

    private void  removeFromShardIndexCache(Integer shardId, Long nodeId){
        shardIndexCache.asMap().computeIfPresent(
                shardId, (id, nodeIds) -> {
                    Set<Long> valueSet = new HashSet<>(nodeIds);
                    valueSet.remove(nodeId);

                    return valueSet.isEmpty() ? null : Set.copyOf(valueSet);
                });
    }

    private ShardNodeModel fetchFromDbAndUpdateCache(Long nodeId){
        ShardNode node = repository.findById(nodeId).orElse(null);

        if(node == null){
            return null;
        }

        ShardNodeModel model = mapToShardNodeModel(node);
        applyToCaffeineCaches(Set.of(model));
        applyToSharedRedisCaches(model);
        return model;
    }

    private Set<ShardNodeModel> fetchNodesFromDbAndUpdateCache(Integer shardId) {
        Set<ShardNodeModel> models = repository
                .findByNodeShardMap_ShardId(shardId)
                .stream()
                .map(CacheUtils::mapToShardNodeModel)
                .collect(Collectors.toSet());

        if(models.isEmpty()){
            return Set.of();
        }

        applyToCaffeineCaches(models);
        applyToSharedRedisCaches(models);
        return  models;
    }
}
