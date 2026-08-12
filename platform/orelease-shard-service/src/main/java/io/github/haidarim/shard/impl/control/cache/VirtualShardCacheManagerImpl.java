package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.ALL_VIRTUAL_SHARD_KEYS;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.virtualShard;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;

@Service
@RequiredArgsConstructor
public class VirtualShardCacheManagerImpl implements VirtualShardCacheManager {

    private final Map<VirtualShardMapId, VirtualShardModel> localCache = new ConcurrentHashMap<>();
    private final RedisTemplate<String, VirtualShardModel> redisCache;
    private final VirtualShardMapRepository repository;
    private final Map<VirtualShardMapId, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public VirtualShardModel getVirtualShard(Integer virtualId, ShardDomain domain) {
        VirtualShardMapId id = new VirtualShardMapId(domain, virtualId);
        VirtualShardModel model = localCache.get(id);

        if (model != null){
            return model;
        }

        ReentrantLock lock = locks.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();
        try{
            model = localCache.get(id);
            if (model != null){
                return model;
            }

            model = redisCache.opsForValue().get(virtualShard(domain.name(), virtualId));
            if (model != null) {
                localCache.put(id, model);
                return model;
            }

            return fetchAndUpdateCache(id);
        }finally {
            lock.unlock();
            removeLock(locks, id, lock);
        }
    }

    @Override
    public void put(Integer virtualId, ShardDomain domain, VirtualShardModel model) {
        VirtualShardMapId id = new VirtualShardMapId(domain, virtualId);
        localCache.put(id, model);
        redisCache.opsForValue().set(virtualShard(domain.name(), virtualId), model);
    }

    @Override
    public void remove(VirtualShardMapId virtualShardId) {
        localCache.remove(virtualShardId);
        redisCache.delete(virtualShard(virtualShardId.getDomain().name(), virtualShardId.getVirtualShardId()));
    }

    @Override
    public void clear() {
        localCache.clear();
        Set<String> keys = getRedisKeys(redisCache, ALL_VIRTUAL_SHARD_KEYS);
        if (!keys.isEmpty()){
            redisCache.delete(keys);
        }
    }

    @Override
    public Map<VirtualShardMapId, VirtualShardModel> getAll() {
        return Map.copyOf(localCache);
    }

    private VirtualShardModel fetchAndUpdateCache(VirtualShardMapId id){
        VirtualShardMap map = repository.findById(id).orElseThrow(() -> new RuntimeException("No such entity found"));
        VirtualShardModel model = VirtualShardModel.builder()
                .shardId(map.getPhysicalShardMap().getShardId())
                .virtualShardId(map.getId().getVirtualShardId())
                .domain(map.getId().getDomain().name())
                .virtualVersion(map.getVersion())
                .shardVersion(map.getPhysicalShardMap().getVersion())
                .build();

        put(id.getVirtualShardId(), id.getDomain(), model);
        return model;
    }

    @Override
    public void refresh(){
        Set<VirtualShardModel> models = repository.findAllActiveMappings()
                .stream()
                .map(virtualShardMap -> VirtualShardModel.builder()
                        .shardId(virtualShardMap.getPhysicalShardMap().getShardId())
                        .virtualShardId(virtualShardMap.getId().getVirtualShardId())
                        .domain(virtualShardMap.getId().getDomain().name())
                        .virtualVersion(virtualShardMap.getVersion())
                        .shardVersion(virtualShardMap.getPhysicalShardMap().getVersion())
                        .build()
                ).collect(Collectors.toSet());

        applyForSharedRedisCache(models);
    }

    @Override
    public void applyForLocalCache(Set<VirtualShardModel> models){
        models.forEach(model ->
            localCache.put(
                    new VirtualShardMapId(ShardDomain.valueOf(model.domain()), model.virtualShardId()),
                    model
            )
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public void applyForSharedRedisCache(Set<VirtualShardModel> models){
        RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) redisCache.getKeySerializer();
        RedisSerializer<@NonNull VirtualShardModel> valueSerializer = (RedisSerializer<@NonNull VirtualShardModel>) redisCache.getValueSerializer();
        redisCache.executePipelined((RedisCallback<Object>) connection -> {
            models.forEach(model -> {
                byte[] key = keySerializer.serialize(virtualShard(model.domain(),  model.virtualShardId()));

                byte[] value = valueSerializer.serialize(model);

                connection.stringCommands().set(key, value);
            });
            return null;
        });
    }
}
