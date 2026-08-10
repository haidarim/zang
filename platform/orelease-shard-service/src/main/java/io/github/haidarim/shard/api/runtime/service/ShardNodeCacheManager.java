package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;

import java.util.Map;
import java.util.Set;

public interface ShardNodeCacheManager {

    ShardNodeModel getNode(Long nodeId);

    void put(Long nodeId, ShardNodeModel model);

    void putAll(Set<ShardNodeModel> nodeModels);

    void remove(Long nodeId);

    void clear();

    Map<Long, ShardNodeModel> getAll();

    void refresh();

    void applyForSharedRedisCache(ShardMapModel model);

    void applyForSharedRedisCache(Set<ShardNodeModel> nodeModels);

    void applyToLocalCache(ShardMapModel model);

    void removeFromLocalCache(Long nodeId);
}
