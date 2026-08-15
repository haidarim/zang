package io.github.haidarim.shard.impl.control.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.utils.CacheUtils;
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
import static io.github.haidarim.shard.utils.CacheUtils.*;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;

/**
 * Cache manager for {@link ShardRouteModel}
 */
@Service
@RequiredArgsConstructor
public class ShardRouteCacheManagerImpl implements ShardRouteCacheManager {

    // L1
    private final Cache<@NonNull Integer, Long> primaryRouteCache;
    private final Cache<@NonNull Integer, Set<Long>> replicaRouteCache;

    // L2
    private final RedisTemplate<String, Long> primaryRedisCache;
    private final RedisTemplate<String, String> replicaRedisCache;

    // Synchronization locks
    private final Map<Integer, ReentrantLock> primaryLocks = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> replicaLocks = new ConcurrentHashMap<>();

    // Helper cache to build replica routes
    private final ShardNodeCacheManager nodeCacheManager;


    @Override
    public Set<ShardRouteModel> getRoutes(Integer shardId){
        Set<ShardRouteModel> models = new HashSet<>(getReplicaRoutes(shardId));

        ShardRouteModel primaryRoute = getPrimaryRoute(shardId);
        if (primaryRoute != null){
            models.add(primaryRoute);
        }

        return models;
    }

    @Override
    public ShardRouteModel getPrimaryRoute(Integer shardId) {
        ReentrantLock lock = primaryLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        lock.lock();
        // try with local cache
        Long nodeId = primaryRouteCache.getIfPresent(shardId);

        if (nodeId == null) {
            try {
                nodeId = resolvePrimaryNodeId(shardId);
            }finally {
                lock.unlock();
                removeLock(primaryLocks, shardId, lock);
            }
        }

        if (nodeId == null){
            return null;
        }

        ShardNodeModel node = nodeCacheManager.getNode(nodeId);
        return node == null ? null : CacheUtils.mapToRouteModel(node);
    }

    @Override
    public Set<ShardRouteModel> getReplicaRoutes(Integer shardId){
        ReentrantLock lock = replicaLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        lock.lock();

        Set<Long> nodeIds = replicaRouteCache.getIfPresent(shardId);

        if(nodeIds == null || nodeIds.isEmpty()) {
            try {
                nodeIds = resolveReplicaNodeIds(shardId);
            } finally {
                lock.unlock();
                removeLock(primaryLocks, shardId, lock);
            }
        }

        if (nodeIds == null || nodeIds.isEmpty()){
            return Set.of();
        }

        return nodeIds
                .stream()
                .map(nodeCacheManager::getNode)
                .filter(Objects::nonNull)
                .map(CacheUtils::mapToRouteModel)
                .collect(Collectors.toSet());
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

    private void clearRedisCache(){
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
        Map<Long, ShardNodeModel> nodes = nodeCacheManager.getAll();

        Map<Integer, Long> primaryNodes = new HashMap<>();
        Map<Integer, Set<Long>> replicaNodes = new HashMap<>();
        nodes.values()
                .stream()
                .filter(node -> ONLINE.equals(node.getStatus()))
                .forEach(node -> {
                    if(PRIMARY.equals(node.getRole())){
                        primaryNodes.put(node.getShardId(), node.getNodeId());
                    }else {
                        replicaNodes.computeIfAbsent(
                                node.getShardId(),
                                ignored -> new HashSet<>()
                        ).add(node.getNodeId());
                    }
                });
        clear();
        primaryNodes.forEach(this::applyPrimaryRouteToRedisCache);
        replicaNodes.forEach(this::applyReplicaRoutesToRedisCache);
    }

    @Override
    public void applyPrimaryRouteToRedisCache(Integer shardId, Long nodeId) {
        if (nodeId == null) {
            return;
        }

        primaryRedisCache.opsForValue().set(
                shardRoute(shardId),
                nodeId
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyReplicaRoutesToRedisCache(Integer shardId, Set<Long> nodeIds){
        if (nodeIds == null || nodeIds.isEmpty()){
            return;
        }

        Set<String> nodeIdsString = nodeIds.stream().map(String::valueOf).collect(Collectors.toSet());
        Map<Integer, Set<String>> replicaRoutes = new HashMap<>();
        replicaRoutes.put(shardId, nodeIdsString);

        replicaRedisCache.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) replicaRedisCache.getKeySerializer();

            replicaRoutes.forEach((id, nodeIdSet) -> {
                byte[] rawKey = keySerializer.serialize(shardRoute(id));

                byte[][] rawMembers = nodeIdSet.stream()
                        .map(i -> i.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                        .toArray(byte[][]::new);

                connection.setCommands().sAdd(rawKey, rawMembers);
            });
            return null;
        });
    }

    @Override
    public void applyToPrimaryToCaffeineCaches(Integer shardId, Long nodeId){
        if (nodeId == null){
            return;
        }

        primaryRouteCache.put(shardId, nodeId);
    }

    @Override
    public void applyToReplicaToCaffeineCaches(Integer shardId, Set<Long> nodeIds){
        if (nodeIds == null || nodeIds.isEmpty()){
            return;
        }

        replicaRouteCache.put(shardId, nodeIds);
    }

    @Override
    public void removeFromRedisCache(Integer shardId){
        primaryRedisCache.delete(shardRoute(shardId));
        replicaRedisCache.delete(shardRoute(shardId));
    }

    @Override
    public void removeFromCaffeine(Integer shardId){
        primaryRouteCache.invalidate(shardId);
        replicaRouteCache.invalidate(shardId);
    }

    private Long resolvePrimaryNodeId(Integer shardId){
        Long nodeId = primaryRedisCache.opsForValue().get(shardRoute(shardId));
        if (nodeId != null) {
            primaryRouteCache.put(shardId, nodeId);
            return nodeId;
        }

        return resolveAndCachePrimaryNodeId(shardId);
    }

    private Set<Long> resolveReplicaNodeIds(Integer shardId){
        Set<String> redisStringNodeIds = replicaRedisCache.opsForSet().members(shardRoute(shardId));
        if (redisStringNodeIds != null && !redisStringNodeIds.isEmpty()) {

            Set<Long> nodeIds = redisStringNodeIds.stream().map(Long::valueOf).collect(Collectors.toSet());

            if (!nodeIds.isEmpty()) {
                replicaRouteCache.put(shardId, nodeIds);
                return nodeIds;
            }
        }

        return resolveAndCacheReplicaNodeIds(shardId);
    }

    private Long resolveAndCachePrimaryNodeId(Integer shardId){
        Set<ShardNodeModel> nodeModels = nodeCacheManager.getNodes(shardId);

        if (nodeModels.isEmpty()){
            return null;
        }

        Long nodeId = nodeModels
                .stream()
                .filter(node -> PRIMARY.equals(node.getRole()) && ONLINE.equals(node.getStatus()))
                .map(ShardNodeModel::getNodeId)
                .findFirst()
                .orElse(null);

        if(nodeId != null) {
            applyToPrimaryToCaffeineCaches(shardId, nodeId);
            applyPrimaryRouteToRedisCache(shardId, nodeId);
        }

        return nodeId;
    }

    private Set<Long> resolveAndCacheReplicaNodeIds(Integer shardId){
        Set<ShardNodeModel> nodes = nodeCacheManager.getNodes(shardId);

        if (nodes.isEmpty()){
            return null;
        }

        Set<Long> nodeIds = nodes.stream()
                .filter(node -> REPLICA.equals(node.getRole()) && ONLINE.equals(node.getStatus()))
                .map(ShardNodeModel::getNodeId)
                .collect(Collectors.toSet());

        if(!nodeIds.isEmpty()){
            applyToReplicaToCaffeineCaches(shardId, nodeIds);
            applyReplicaRoutesToRedisCache(shardId, nodeIds);
        }
        return nodeIds;
    }
}
