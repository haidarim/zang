package io.github.haidarim.shard.impl.control.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import io.github.haidarim.shard.utils.CacheUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.type.NodeRole.PRIMARY;
import static io.github.haidarim.shard.api.common.type.NodeRole.REPLICA;
import static io.github.haidarim.shard.api.common.type.NodeStatus.ONLINE;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.ALL_SHARD_NODE_INDEX_KEYS;
import static io.github.haidarim.shard.utils.CacheUtils.*;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;

/**
 * Cache manager for {@link ShardRouteModel}
 */
@Service
@RequiredArgsConstructor
public class ShardRouteCacheManagerImpl implements ShardRouteCacheManager {

    // L1
    private final Cache<@NonNull Integer, ShardRouteModel> primaryRouteCache;
    private final Cache<@NonNull Integer, Set<Long>> replicaRouteCache;

    // L2
    private final RedisTemplate<String, ShardRouteModel> primaryRedisCache;
    private final RedisTemplate<String, String> replicaRedisCache;

    // L3
    private final ShardNodeRepository shardNodeRepository;

    // Synchronization locks
    private final Map<Integer, ReentrantLock> primaryLocks = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> replicaLocks = new ConcurrentHashMap<>();

    // Helper cache to build replica routes
    private final ShardNodeCacheManager nodeCacheManager;


    @Override
    public Set<ShardRouteModel> getRoutes(Integer shardId){
        Set<ShardRouteModel> models = new HashSet<>(getReplicaRoutes(shardId));
        models.add(getPrimaryRoute(shardId));
        return models;
    }

    @Override
    public ShardRouteModel getPrimaryRoute(Integer shardId) {
        // try with local cache
        ShardRouteModel model = primaryRouteCache.getIfPresent(shardId);
        if (model != null) {
            return model;
        }

        ReentrantLock lock = primaryLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        // else try with redis cache, else db
        lock.lock();
        try {
            model = primaryRouteCache.getIfPresent(shardId);
            if (model != null) {
                return model;
            }

            model = primaryRedisCache.opsForValue().get(shardRoute(shardId));
            if (model != null) {
                primaryRouteCache.put(shardId, model);
                return model;
            }

            return fetchPrimaryRouteAndUpdateCache(shardId);
        }finally {
            lock.unlock();
            removeLock(primaryLocks, shardId, lock);
        }
    }

    @Override
    public Set<ShardRouteModel> getReplicaRoutes(Integer shardId){
        Set<Long> nodeIds = replicaRouteCache.getIfPresent(shardId);
        if (nodeIds != null) {
            Set<ShardRouteModel> models = resolveLocalRoutes(nodeIds);

            if(models.size() == nodeIds.size()) {
                return models;
            }
        }

        ReentrantLock lock = replicaLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        // else try with redis cache, else db
        lock.lock();
        try {
            nodeIds = replicaRouteCache.getIfPresent(shardId);
            if (nodeIds != null) {
                Set<ShardRouteModel> models = resolveLocalRoutes(nodeIds);

                if(models.size() == nodeIds.size()) {
                    return models;
                }
            }

            Set<String> redisStringNodeIds = replicaRedisCache.opsForSet().members(shardRoute(shardId));
            if (redisStringNodeIds != null && !redisStringNodeIds.isEmpty()) {

                nodeIds = redisStringNodeIds.stream().map(Long::valueOf).collect(Collectors.toSet());
                Set<ShardRouteModel> models = nodeIds
                        .stream()
                        .map(nodeCacheManager::getNode)
                        .map(CacheUtils::mapToRouteModel)
                        .collect(Collectors.toSet());

                if (nodeIds.size() == models.size()){
                    replicaRouteCache.put(shardId, nodeIds);
                    return models;
                }
            }

            return fetchReplicaRoutesAndUpdateCache(shardId);
        }finally {
            lock.unlock();
            removeLock(primaryLocks, shardId, lock);
        }
    }


    @Override
    public void clear() {
        clearCaffeineCache();
        clearRedisCache();
    }

    @Override
    public void clearCaffeineCache(){
        primaryRouteCache.invalidateAll();
        replicaRouteCache.invalidateAll();
    }

    public void clearRedisCache(){
        Set<String> primaryKeys = getRedisKeys(primaryRedisCache, ALL_ROUTE_KEYS);
        if(!primaryKeys.isEmpty()){
            primaryRedisCache.delete(primaryKeys);
        }

        Set<String> replicaKeys = getRedisKeys(replicaRedisCache, ALL_ROUTE_KEYS);
        if (!replicaKeys.isEmpty()) {
            replicaRedisCache.delete(replicaKeys);
        }
    }

    @Override
    public void refresh(){
        Set<ShardRouteModel> models = shardNodeRepository.findAllOnlineAndPrimaryNodes()
                .stream()
                .map(CacheUtils::mapToRouteModel)
                .collect(Collectors.toSet());

        // TODO
    }

    @Override
    public void applyToSharedRedisCaches(ShardRouteModel model){
        if (model == null){
            return;
        }

        primaryRedisCache.opsForValue().set(
                shardRoute(model.getShardId()),
                model
        );

        replicaRedisCache.opsForSet().add(
                shardRoute(model.getShardId()),
                model.getNodeId().toString()
        );

        // TODO event publisher
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyToSharedRedisCaches(Set<ShardRouteModel> models){
        if (models == null || models.isEmpty()){
            return;
        }

        Map<String, ShardRouteModel> primaryRoutes = new HashMap<>();
        Map<Integer, Set<String>> replicaRoutes = new HashMap<>();

        models.forEach( model -> {
            if(PRIMARY.equals(model.getRole())){
                primaryRoutes.put(shardRoute(model.getShardId()), model);
            }else if (REPLICA.equals(model.getRole())){
                replicaRoutes.computeIfAbsent(
                        model.getShardId(),
                        ignored -> new HashSet<>()
                ).add(model.getNodeId().toString());
            }
            throw new RuntimeException("Invalid NODE_ROLE");
        });

        primaryRedisCache.opsForValue().multiSet(primaryRoutes);

        replicaRedisCache.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) replicaRedisCache.getKeySerializer();

            replicaRoutes.forEach((shardId, nodeIds) -> {
                byte[] rawKey = keySerializer.serialize(shardRoute(shardId));

                byte[][] rawMembers = nodeIds.stream()
                        .map(id -> id.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toArray(byte[][]::new);

                connection.setCommands().sAdd(rawKey, rawMembers);
            });
            return null;
        });

        // TODO event
    }

    @Override
    public void applyToCaffeineCaches(Set<ShardRouteModel> models){
        if (models == null || models.isEmpty()){
            return;
        }

        models.forEach(model -> primaryRouteCache.put(model.getShardId(), model));
        Map<Integer, Set<Long>> shardIndexValues = toShardIndexMap(models);

        // TODO continue ++
    }
    @Override
    public void removeFromRedisCache(Integer shardId){

    }
    @Override
    public void removeFromCaffeine(Integer shardId){

    }

    private Set<ShardRouteModel> resolveLocalRoutes(Set<Long> nodeIds) {
        return nodeIds.stream()
                .map(nodeCacheManager::getNode)
                .filter(Objects::nonNull)
                .map(CacheUtils::mapToRouteModel)
                .collect(Collectors.toSet());
    }

    private ShardRouteModel fetchPrimaryRouteAndUpdateCache(Integer shardId){
        List<ShardNode> nodes = shardNodeRepository
                .fetchByShardIdAndStatusAndRole(shardId, ONLINE, PRIMARY);

        if (nodes.isEmpty()){
            return null;
        }

        ShardNode primaryNode = nodes.get(0);

        ShardRouteModel model = mapToRouteModel(primaryNode);

        // TODO applyToPrimaryRedisCache
        return model;
    }

    private Set<ShardRouteModel> fetchReplicaRoutesAndUpdateCache(Integer shardId){
        List<ShardNode> nodes = shardNodeRepository
                .fetchByShardIdAndStatusAndRole(shardId, ONLINE, REPLICA);

        if (nodes.isEmpty()){
            return null;
        }
        Set<ShardRouteModel> models = nodes.stream()
                .map(CacheUtils::mapToRouteModel)
                .collect(Collectors.toSet());

        // TODO applyToReplicaRedisCache
        return models;
    }


}
