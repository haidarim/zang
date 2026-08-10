package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;

import java.util.Set;

public interface ShardNodeCacheManager {

    ShardNodeModel getNode(Long nodeId);

    Set<ShardNodeModel> getNodes(Integer shardId, String hostName, int port);

    void removeFromCaffeine(Long nodeId);

    void clear();

    void clearCaffeineCache();

    void clearRedisCache();

    void refresh();

    void applyToSharedRedisCache(ShardMapModel model);

    void applyToSharedRedisCache(Set<ShardNodeModel> nodeModels);

    void applyToCaffeineCache(ShardMapModel model);

    void applyToCaffeineCache(Set<ShardNodeModel> nodeModels);

    void removeFromRedisCache(Long nodeId);
}

