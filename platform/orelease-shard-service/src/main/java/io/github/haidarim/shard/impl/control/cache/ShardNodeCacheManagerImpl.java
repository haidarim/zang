package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.model.CacheModel;
import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.event.NodeCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;
import static io.github.haidarim.shard.utils.CacheUtils.toShardIndexMap;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;
import com.github.benmanes.caffeine.cache.Cache;

@Service
@RequiredArgsConstructor
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


    private final ApplicationEventPublisher eventPublisher;


    @Override
    public ShardNodeModel getNode(Long nodeId) {
        ShardNodeModel model = nodeCache.getIfPresent(nodeId);
        if(model != null){
            return model;
        }

        ReentrantLock lock = nodeLocks.computeIfAbsent(nodeId, key -> new ReentrantLock());
        lock.lock();
        try {
            model = nodeCache.getIfPresent(nodeId);
            if(model != null){
                return model;
            }

            // L2
            model = nodeRedisCache.opsForValue().get(shardNode(nodeId));
            if (model != null) {
                putNodeInLocalCaches(model);
                return model;
            }
            // L3
            return fetchFromDbAndUpdateCache(nodeId);
        }finally {
            lock.unlock();
            removeLock(nodeLocks, nodeId, lock);
        }
    }

    public Set<ShardNodeModel> getNodes(Integer shardId){
        // check L1
        Set<Long> nodeIds = shardIndexCache.getIfPresent(shardId);
        if (nodeIds != null){
            Set<ShardNodeModel> models = resolveLocalNodes(nodeIds);

            if (models.size() == nodeIds.size()) {
                return models;
            }
        }

        ReentrantLock lock = shardLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        lock.lock();
        try{
            // try L1
            nodeIds = shardIndexCache.getIfPresent(shardId);
            if (nodeIds != null){
                Set<ShardNodeModel> models = resolveLocalNodes(nodeIds);

                if (models.size() == nodeIds.size()) {
                    return models;
                }
            }
            //L2
            Set<String> redisNodeIds = shardIndexRedisCache.opsForSet()
                    .members(shardNodeByShardId(shardId));
            if (redisNodeIds != null && !redisNodeIds.isEmpty()) {

                nodeIds = redisNodeIds.stream()
                        .map(Long::valueOf)
                        .collect(Collectors.toUnmodifiableSet());

                shardIndexCache.put(shardId, nodeIds);

                // L2 - resolve actual node models
                Set<ShardNodeModel> models = getModelsFromRedisCache(nodeIds);
                if (!models.isEmpty()){
                    return models;
                }
            }

            // L3
            return fetchNodesFromDbAndUpdateCache(shardId);
        } finally {
            lock.unlock();
            removeLock(shardLocks, shardId, lock);
        }
    }


    @Override
    public void removeFromCaffeine(Long nodeId) {
        ShardNodeModel model = nodeCache.getIfPresent(nodeId);
        nodeCache.invalidate(nodeId);

        if (model != null){
            removeFromShardIndexCache(model.getShardId(), nodeId);
        }
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

    @Override
    public void clearRedisCache(){
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
                .map(this::mapToShardNodeModel).collect(Collectors.toSet());

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

        eventPublisher.publishEvent(
                new NodeCacheEvent(Set.of(model), CacheEventType.CREATED)
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

                connection.keyCommands().del(rawKey);

                byte[][] rawMembers = nodeIds.stream()
                        .map(id -> id.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toArray(byte[][]::new);

                connection.setCommands().sAdd(rawKey, rawMembers);
            });
            return null;
        });

        eventPublisher.publishEvent(
                new NodeCacheEvent(
                        models,
                        CacheEventType.CREATED
                )
        );
    }

    @Override
    public void applyToCaffeineCaches(Set<ShardNodeModel> nodeModels){
        if (nodeModels == null || nodeModels.isEmpty()){
            return;
        }

        nodeModels.forEach(this::putNodeInLocalCaches);

        Map<Integer, Set<Long>> shardIndexValues = toShardIndexMap(nodeModels);
        shardIndexValues.forEach((shardId, nodeIds) -> shardIndexCache.put(shardId, Set.copyOf(nodeIds)));
    }

    @Override
    public void removeFromRedisCache(Long nodeId) {
        ShardNodeModel model = nodeRedisCache.opsForValue().get(shardNode(nodeId));
        if (model == null){
            model = nodeCache.getIfPresent(nodeId);
        }

        nodeRedisCache.delete(shardNode(nodeId));

        if (model != null) {
            shardIndexRedisCache
                    .opsForSet()
                    .remove(
                            shardNodeByShardId(model.getShardId()),
                            nodeId.toString()
                    );
        }
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
        return nodeIds.stream()
                .map(nodeCache::getIfPresent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
        ShardNode node = repository.findById(nodeId).orElseThrow(()-> new RuntimeException("No such entity found"));

        ShardNodeModel model = mapToShardNodeModel(node);

        applyToSharedRedisCaches(model);
        return model;
    }

    private Set<ShardNodeModel> fetchNodesFromDbAndUpdateCache(Integer shardId) {
        Set<ShardNodeModel> models = repository
                .findByNodeShardMap_ShardId(shardId)
                .stream()
                .map(this::mapToShardNodeModel)
                .collect(Collectors.toSet());

        if(models.isEmpty()){
            throw new RuntimeException("No Node exists for shard: " + shardId);
        }

        applyToSharedRedisCaches(models);
        return  models;
    }

    private ShardNodeModel mapToShardNodeModel(ShardNode node){
        return ShardNodeModel.builder()
                .nodeId(node.getNodeId())
                .shardId(node.getNodeShardMap().getShardId())
                .hostName(node.getHostName())
                .port(node.getPort())
                .region(node.getRegion())
                .domain(node.getNodeShardMap().getDomain())
                .role(node.getNodeRole())
                .status(node.getNodeStatus())
                .connectionSecret(node.getConnectionSecret())
                .build();
    }

}
