package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;

import java.util.Map;
import java.util.Set;

public interface VirtualShardCacheManager {

    VirtualShardModel getVirtualShard(Integer virtualId, ShardDomain domain);
    Set<VirtualShardModel> getVirtualShardIds(Integer shardId);

    void removeFromCaffeineCaches(Integer shardId, VirtualShardMapId id);

    void removeFromRedisCaches(Integer shardId, VirtualShardMapId id);

    void clear();

    void clearCaffeineCache();

    void refresh();

    void applyToRedisCaches(Set<VirtualShardModel> models);

    void applyToVirtualShardCache(VirtualShardModel model);
    void applyToShardIndexCache(VirtualShardModel model);

    void applyToVirtualShardRedisCache(Set<VirtualShardModel> models);

    void applyToVirtualShardRedisCache(VirtualShardModel model);

    void applyToShardIndexRedisCache(Set<VirtualShardModel> models);

    Map<VirtualShardMapId, VirtualShardModel> getAll();
}
