package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;

import java.util.Set;

public interface ShardRouteCacheManager {

    Set<ShardRouteModel> getRoutes(Integer shardId);

    ShardRouteModel getPrimaryRoute(Integer shardId);

    Set<ShardRouteModel> getReplicaRoutes(Integer shardId);

    void clear();

    void clearCaffeineCache();

    void clearRedisCache();

    void refresh();

    void applyPrimaryRouteToRedisCache(Integer shardId, Long nodeId);
    void applyReplicaRoutesToRedisCache(Integer shardId, Set<Long> nodeIds);

    void applyToPrimaryToCaffeineCaches(Integer shardId, Long nodeId);

    void applyToReplicaToCaffeineCaches(Integer shardId, Set<Long> nodeIds);

    void removeFromRedisCache(Integer shardId);

    void removeFromCaffeine(Integer shardId);
}
