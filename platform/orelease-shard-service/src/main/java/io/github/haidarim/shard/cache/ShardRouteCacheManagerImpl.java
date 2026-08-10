package io.github.haidarim.shard.cache;

import io.github.haidarim.shard.api.common.constants.ShardConstants;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.type.NodeRole.PRIMARY;
import static io.github.haidarim.shard.api.common.type.NodeStatus.ONLINE;
import static io.github.haidarim.shard.cache.Cache.ALL_ROUTE_KEYS;
import static io.github.haidarim.shard.cache.Cache.shardRoute;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;

/**
 * Cache manager for {@link ShardRouteModel}
 */
@Service
@RequiredArgsConstructor
public class ShardRouteCacheManagerImpl implements ShardRouteCacheManager {

    private final Map<Integer, ShardRouteModel> localCache = new ConcurrentHashMap<>();
    private final RedisTemplate<String, ShardRouteModel> redisCache;

    private final ShardNodeRepository shardNodeRepository;
    private final Map<Integer, Object> locks = new ConcurrentHashMap<>();

    @Override
    public ShardRouteModel getRoute(Integer shardId) {
        // try with local cache
        ShardRouteModel model = localCache.get(shardId);
        if (model != null){
            return model;
        }

        Object lock = locks.computeIfAbsent(shardId, key -> new Object());
        // else try with redis cache, else db
        synchronized (lock) {
            model = localCache.get(shardId);
            if (model != null){
                return model;
            }

            model = redisCache.opsForValue().get(shardRoute(shardId));
            if (model != null) {
                localCache.put(shardId, model);
                return model;
            }

            return fetchAndUpdateCache(shardId);
        }
    }

    @Override
    public void put(Integer shardId, ShardRouteModel route) {
        localCache.put(shardId, route);
        redisCache.opsForValue().set(shardRoute(shardId), route);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Set<ShardRouteModel> models) {
        models.forEach(model -> localCache.put(model.shardId(), model));

        RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) redisCache.getKeySerializer();
        RedisSerializer<@NonNull ShardRouteModel> valueSerializer = (RedisSerializer<@NonNull ShardRouteModel>) redisCache.getValueSerializer();

        redisCache.executePipelined((RedisCallback<Object>) connection ->{
            models.forEach(model -> {
                byte[] key = keySerializer.serialize(shardRoute(model.shardId()));
                byte[] value = valueSerializer.serialize(model);

                connection.stringCommands().set(key, value);
            });
            return null;
        });
    }

    @Override
    public void remove(Integer shardId) {
        localCache.remove(shardId);
        redisCache.delete(shardRoute(shardId));
    }

    @Override
    public void clear() {
        localCache.clear();
        Set<String> keys = getRedisKeys(redisCache, ALL_ROUTE_KEYS);
        if (!keys.isEmpty()){
            redisCache.delete(keys);
        }
    }

    @Override
    public Map<Integer, ShardRouteModel> getAll() {
        return Map.copyOf(localCache);
    }

    private ShardRouteModel fetchAndUpdateCache(Integer shardId){
        ShardNode primaryNode = shardNodeRepository
                .fetchByShardIdAndStatusAndRole(shardId, ONLINE, PRIMARY)
                .orElseThrow(()-> new IllegalArgumentException(ShardConstants.NO_SHARD_NODE_FOUND));

        ShardRouteModel model = ShardRouteModel.builder()
                .shardId(shardId)
                .shardName(primaryNode.getNodeShardMap().getShardName())
                .databaseName(primaryNode.getNodeShardMap().getDatabaseName())
                .hostName(primaryNode.getHostName())
                .port(primaryNode.getPort())
                .topologyVersion(primaryNode.getNodeShardMap().getVersion())
                .build();
        put(shardId, model);

        return model;
    }

    @Override
    public void refresh(){
        Set<ShardRouteModel> models = shardNodeRepository.findAllOnlineAndPrimaryNodes()
                .stream()
                .map(node -> ShardRouteModel.builder()
                        .shardId(node.getNodeShardMap().getShardId())
                        .shardName(node.getNodeShardMap().getShardName())
                        .databaseName(node.getNodeShardMap().getDatabaseName())
                        .hostName(node.getHostName())
                        .port(node.getPort())
                        .topologyVersion(node.getNodeShardMap().getVersion())
                        .build()
                ).collect(Collectors.toSet());

        putAll(models);
    }
}
