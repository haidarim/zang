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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.type.NodeRole.PRIMARY;
import static io.github.haidarim.shard.api.common.type.NodeRole.REPLICA;
import static io.github.haidarim.shard.api.common.type.NodeStatus.ONLINE;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.ALL_ROUTE_KEYS;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.shardRoute;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;
import static io.github.haidarim.shard.utils.CacheUtils.mapToRouteModel;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;

/**
 * Cache manager for {@link ShardRouteModel}
 */
@Service
@RequiredArgsConstructor
public class ShardRouteCacheManagerImpl implements ShardRouteCacheManager {

    // L1
    private final Cache<@NonNull Integer, ShardRouteModel> primaryRouteCache;
    private final Cache<@NonNull Integer, Set<ShardRouteModel>> replicaRouteCache;

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
        Set<ShardRouteModel> models = replicaRouteCache.getIfPresent(shardId);
        if (models != null) {
            return models;
        }

        ReentrantLock lock = replicaLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        // else try with redis cache, else db
        lock.lock();
        try {
            models = replicaRouteCache.getIfPresent(shardId);
            if (models != null) {
                return models;
            }

            Set<String> nodeIds = replicaRedisCache.opsForSet().members(shardRoute(shardId));
            if (nodeIds != null && !nodeIds.isEmpty()) {
                models = nodeIds
                        .stream()
                        .map(Long::valueOf)
                        .map(nodeCacheManager::getNode)
                        .map(CacheUtils::mapToRouteModel)
                        .collect(Collectors.toSet());

                if (nodeIds.size() == models.size()){
                    replicaRouteCache.put(shardId, models);
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
        localCache.clear();
        Set<String> keys = getRedisKeys(redisCache, ALL_ROUTE_KEYS);
        if (!keys.isEmpty()){
            redisCache.delete(keys);
        }
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

    @Override
    public void refresh(){
        Set<ShardRouteModel> models = shardNodeRepository.findAllOnlineAndPrimaryNodes()
                .stream()
                .map(CacheUtils::mapToRouteModel)
                .collect(Collectors.toSet());

        // TODO
    }
}
