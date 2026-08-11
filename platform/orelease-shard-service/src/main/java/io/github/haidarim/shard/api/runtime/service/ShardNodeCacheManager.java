package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;

import java.util.Set;

public interface ShardNodeCacheManager {

    ShardNodeModel getNode(Long nodeId);

    Set<ShardNodeModel> getNodes(Integer shardId);

    void removeFromCaffeine(Long nodeId);

    void clear();

    void clearCaffeineCache();

    void clearRedisCache();

    void refresh();

    void applyToSharedRedisCaches(ShardNodeModel model);
    void applyToSharedRedisCaches(Set<ShardNodeModel> nodeModels);

    void applyToCaffeineCaches(Set<ShardNodeModel> nodeModels);

    void removeFromRedisCache(Long nodeId);
}

