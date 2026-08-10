package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
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

import static io.github.haidarim.shard.impl.control.cache.Cache.ALL_SHARD_NODE_KEYS;
import static io.github.haidarim.shard.impl.control.cache.Cache.shardNode;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;
import com.github.benmanes.caffeine.cache.Cache;

@Service
@RequiredArgsConstructor
public class ShardNodeCacheManagerImpl implements ShardNodeCacheManager {

    private final Cache<@NonNull Long, ShardNodeModel> nodesByIdCache;
    private final Cache<@NonNull Integer, Set<Long>> nodeIdsByShardId;

    private final RedisTemplate<String, ShardNodeModel> redisCache;
    private final Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final RedisCachePublisher redisPublisher;
    private final ShardNodeRepository repository;

    @Override
    public ShardNodeModel getNode(Long nodeId) {
        ShardNodeModel model = nodesByIdCache.getIfPresent(nodeId);
        if(model != null){
            return model;
        }

        ReentrantLock lock = locks.computeIfAbsent(nodeId, key -> new ReentrantLock());
        lock.lock();
        try {
            model = nodesByIdCache.getIfPresent(nodeId);
            if(model != null){
                return model;
            }

            model = redisCache.opsForValue().get(shardNode(nodeId));
            if (model != null) {
                nodesByIdCache.put(nodeId, model);
                return model;
            }

            return fetchFromDbAndUpdateCache(nodeId);
        }finally {
            lock.unlock();
            removeLock(locks, nodeId, lock);
        }
    }

    @Override
    public void put(Long nodeId, ShardNodeModel model) {
        nodesByIdCache.put(nodeId, model);
    }

    @Override
    public void putAll(Set<ShardNodeModel> models) {
        models.forEach(model -> nodesByIdCache.put(model.nodeId(), model));
    }

    @Override
    public void remove(Long nodeId) {
        nodesByIdCache.(nodeId);
        redisCache.delete(shardNode(nodeId));
    }

    @Override
    public void clear() {
        localCache.clear();
        Set<String> keys = getRedisKeys(redisCache, ALL_SHARD_NODE_KEYS);
        if(!keys.isEmpty()){
            redisCache.delete(keys);
        }
    }

    @Override
    public Map<Long, ShardNodeModel> getAll() {
        return Map.copyOf(localCache);
    }

    private ShardNodeModel fetchFromDbAndUpdateCache(Long nodeId){
        ShardNode node = repository.findById(nodeId).orElseThrow(()-> new RuntimeException("No such entity found"));

        ShardNodeModel model = ShardNodeModel.builder()
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

        put(nodeId, model);
        return model;
    }


    @Override
    public void refresh(){
        Set<ShardNodeModel> nodes = repository.findAll()
                .stream()
                .map(node -> ShardNodeModel.builder()
                        .nodeId(node.getNodeId())
                        .shardId(node.getNodeShardMap().getShardId())
                        .hostName(node.getHostName())
                        .port(node.getPort())
                        .region(node.getRegion())
                        .domain(node.getNodeShardMap().getDomain())
                        .role(node.getNodeRole())
                        .status(node.getNodeStatus())
                        .connectionSecret(node.getConnectionSecret())
                        .build()
                ).collect(Collectors.toSet());

        applyForSharedRedisCache(nodes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void applyForSharedRedisCache(Set<ShardNodeModel> models){
        RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) redisCache.getKeySerializer();
        RedisSerializer<@NonNull ShardNodeModel> valueSerializer = (RedisSerializer<@NonNull ShardNodeModel>) redisCache.getValueSerializer();

        redisCache.executePipelined((RedisCallback<Object>) connection -> {
            models.forEach(model -> {
                byte[] key = keySerializer.serialize(shardNode(model.nodeId()));
                byte[] value= valueSerializer.serialize(model);

                connection.stringCommands().set(key, value);
            });
            return null;
        });
    }

    @Override
    public void applyForSharedRedisCache(ShardMapModel model){

    }

    @Override
    public void applyToLocalCache(ShardMapModel model){

    }

    @Override
    public void removeFromLocalCache(Long nodeId) {

    }
}
