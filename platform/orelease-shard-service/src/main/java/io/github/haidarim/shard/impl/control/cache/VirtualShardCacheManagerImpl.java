package io.github.haidarim.shard.impl.control.cache;

import com.github.benmanes.caffeine.cache.Cache;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;
import static io.github.haidarim.shard.utils.CacheUtils.getRedisKeys;
import static io.github.haidarim.shard.utils.LockUtils.removeLock;

@Service
@RequiredArgsConstructor
public class VirtualShardCacheManagerImpl implements VirtualShardCacheManager {

    private final Cache<@NonNull VirtualShardMapId, VirtualShardModel> virtualShardCache;
    private final RedisTemplate<String, VirtualShardModel> virtualShardRedisCache;
    private final Cache<@NonNull Integer, Set<VirtualShardModel>> shardIndexCache;
    private final RedisTemplate<String, Set<VirtualShardModel>> shardIndexRedisCache;

    private final VirtualShardMapRepository repository;

    private final Map<VirtualShardMapId, ReentrantLock> virtualShardLocks = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> shardIndexLocks = new ConcurrentHashMap<>();


    @Override
    public VirtualShardModel getVirtualShard(Integer virtualId, ShardDomain domain) {
        VirtualShardMapId id = new VirtualShardMapId(domain, virtualId);

        ReentrantLock lock = virtualShardLocks.computeIfAbsent(id, key -> new ReentrantLock());
        lock.lock();

        VirtualShardModel model = virtualShardCache.getIfPresent(id);

        if(model == null) {
            try {
                model = resolveVirtualShardModel(id);
            } finally {
                lock.unlock();
                removeLock(virtualShardLocks, id, lock);
            }
        }
        return model;
    }

    @Override
    public Set<VirtualShardModel> getVirtualShardIds(Integer shardId){
        ReentrantLock lock = shardIndexLocks.computeIfAbsent(shardId, key -> new ReentrantLock());
        lock.lock();

        Set<VirtualShardModel> virtualIds = shardIndexCache.getIfPresent(shardId);

        if(virtualIds == null || virtualIds.isEmpty()){
            try{
                virtualIds = resolveVirtualShardByShardId(shardId);
            }finally {
                lock.unlock();
                removeLock(shardIndexLocks, shardId, lock);
            }
        }

        return virtualIds;
    }


    @Override
    public void removeFromCaffeineCaches(Integer shardId, VirtualShardMapId id){
        virtualShardCache.invalidate(id);
        shardIndexCache.invalidate(shardId);
    }

    @Override
    public void removeFromRedisCaches(Integer shardId, VirtualShardMapId id){
        virtualShardRedisCache.delete(virtualShard(id.getDomain().name(), id.getVirtualShardId()));
        shardIndexRedisCache.delete(virtualShardIdByShardId(shardId));
    }

    @Override
    public void clear() {
        clearCaffeineCache();
        clearRedisCache();
    }

    @Override
    public void clearCaffeineCache(){
        virtualShardCache.invalidateAll();
        shardIndexCache.invalidateAll();
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
        clear();
        if(!models.isEmpty()) {
            models.forEach(this::applyToVirtualShardCache);
            models.forEach(this::applyToShardIndexCache);
            applyToRedisCaches(models);
        }
    }

    @Override
    public void applyToVirtualShardCache(VirtualShardModel model){
        virtualShardCache.put(model.getIdentifier(), model);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void applyToVirtualShardRedisCache(Set<VirtualShardModel> models){
        RedisSerializer<@NonNull String> keySerializer = (RedisSerializer<@NonNull String>) virtualShardRedisCache.getKeySerializer();
        RedisSerializer<@NonNull VirtualShardModel> valueSerializer = (RedisSerializer<@NonNull VirtualShardModel>) virtualShardRedisCache.getValueSerializer();
        virtualShardRedisCache.executePipelined((RedisCallback<Object>) connection -> {
            models.forEach(model -> {
                byte[] key = keySerializer.serialize(virtualShard(model.getDomain(), model.getVirtualShardId()));

                byte[] value = valueSerializer.serialize(model);

                connection.stringCommands().set(key, value);
            });
            return null;
        });
    }

    @Override
    public void applyToVirtualShardRedisCache(VirtualShardModel model) {
        virtualShardRedisCache.opsForValue().set(virtualShard(model.getDomain(), model.getVirtualShardId()), model);
    }


    @Override
    public void applyToRedisCaches(Set<VirtualShardModel> models){
        applyToVirtualShardRedisCache(models);
        applyToShardIndexRedisCache(models);
    }

    @Override
    public void applyToShardIndexRedisCache(Set<VirtualShardModel> models){
        if (models == null || models.isEmpty()){
            return;
        }

        Map<Integer, Set<VirtualShardModel>> shardIndexValues = models
                .stream()
                .collect(Collectors.groupingBy(
                        VirtualShardModel::getShardId,
                        Collectors.toSet()
                ));

        shardIndexRedisCache.opsForValue().multiSet(
                shardIndexValues.entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> virtualShardIdByShardId(entry.getKey()),
                                Map.Entry::getValue
                        ))
        );
    }

    @Override
    public void applyToShardIndexCache(VirtualShardModel model){
        shardIndexCache.asMap().compute(
                model.getShardId(), (sharId, current) ->{
                    Set<VirtualShardModel> updated = current == null ? new HashSet<>() : new HashSet<>(current);

                    updated.add(model);
                    return Set.copyOf(updated);
                }
        );
    }

    @Override
    public Map<VirtualShardMapId, VirtualShardModel> getAll() {
        return Map.copyOf(virtualShardCache.asMap());
    }

    private void clearRedisCache(){
        Set<String> virtualKeys = getRedisKeys(virtualShardRedisCache, ALL_VIRTUAL_SHARD_KEYS);
        if (!virtualKeys.isEmpty()){
            virtualShardRedisCache.delete(virtualKeys);
        }

        Set<String> shardIndexKeys = getRedisKeys(shardIndexRedisCache, ALL_VIRTUAL_SHARD_INDEX_KEYS);
        if (!shardIndexKeys.isEmpty()){
            shardIndexRedisCache.delete(shardIndexKeys);
        }
    }

    private VirtualShardModel resolveVirtualShardModel(VirtualShardMapId id){
        VirtualShardModel model = virtualShardRedisCache.opsForValue().get(virtualShard(id.getDomain().name(), id.getVirtualShardId()));
        if (model != null) {
            virtualShardCache.put(id, model);
            return model;
        }

        return fetchAndUpdateCache(id);
    }

    private Set<VirtualShardModel> resolveVirtualShardByShardId(Integer shardId){
        // L2
        Set<VirtualShardModel> models = shardIndexRedisCache.opsForValue().get(virtualShardIdByShardId(shardId));
        if(models != null && !models.isEmpty()){
            models.forEach(m -> {
                applyToVirtualShardCache(m);
                applyToShardIndexCache(m);
            });

            return models;
        }

        return fetchAndUpdateCache(shardId);
    }

    private VirtualShardModel fetchAndUpdateCache(VirtualShardMapId id){
        VirtualShardMap map = repository.findActiveVirtualShardMapById(id).orElseThrow(() -> new RuntimeException("No such entity found"));
        VirtualShardModel model = mapToVirtualShardModel(map);

        applyToVirtualShardCache(model);
        applyToShardIndexCache(model);
        applyToRedisCaches(Set.of(model));

        return model;
    }

    private Set<VirtualShardModel> fetchAndUpdateCache(Integer shardId){
        Set<VirtualShardModel> models = repository.findAllActiveVirtualIdsByShardId(shardId)
                .stream()
                .map(this::mapToVirtualShardModel)
                .collect(Collectors.toSet());

        if (models.isEmpty()){
            return Set.of();
        }

        models.forEach(this::applyToVirtualShardCache);
        models.forEach(this::applyToShardIndexCache);
        applyToRedisCaches(models);

        return models;
    }

    private VirtualShardModel mapToVirtualShardModel(VirtualShardMap map){
        return VirtualShardModel.builder()
                .shardId(map.getPhysicalShardMap().getShardId())
                .virtualShardId(map.getId().getVirtualShardId())
                .domain(map.getId().getDomain().name())
                .virtualVersion(map.getVersion())
                .shardVersion(map.getPhysicalShardMap().getVersion())
                .build();
    }
}
