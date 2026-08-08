package io.github.haidarim.shard.cache;

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
import java.util.stream.Collectors;

import static io.github.haidarim.shard.cache.CacheConstants.*;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;

@Service
@RequiredArgsConstructor
public class ShardNodeCacheManagerImpl implements ShardNodeCacheManager {

    private final Map<Long, ShardNodeModel> localCache = new ConcurrentHashMap<>();
    private final RedisTemplate<String, ShardNodeModel> redisCache;
    private final Map<Long, Object> locks = new ConcurrentHashMap<>();
    private final RedisCachePublisher redisPublisher;
    private final ShardNodeRepository repository;

    @Override
    public ShardNodeModel getNode(Long nodeId) {
        ShardNodeModel model = localCache.get(nodeId);
        if(model != null){
            return model;
        }

        Object lock = locks.computeIfAbsent(nodeId, key -> new Object());
        synchronized (lock) {
            model = localCache.get(nodeId);
            if(model != null){
                return model;
            }

            model = redisCache.opsForValue().get(shardNode(nodeId));
            if (model != null) {
                localCache.put(nodeId, model);
                return model;
            }

            return fetchFromDbAndUpdateCache(nodeId);
        }
    }

    @Override
    public void put(Long nodeId, ShardNodeModel model) {
        localCache.put(nodeId, model);
        redisCache.opsForValue().set(shardNode(nodeId), model);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Set<ShardNodeModel> models) {
        models.forEach(model -> localCache.put(model.nodeId(), model));

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
    public void remove(Long nodeId) {
        localCache.remove(nodeId);
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

        putAll(nodes);
    }

    @Override
    public void refreshLocal(Long nodeId) {

    }

    @Override
    public void removeLocal(Long nodeId) {

    }
}
