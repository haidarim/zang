package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;

import java.util.Map;
import java.util.Set;

public interface ShardRouteCacheManager {

    Set<ShardRouteModel> getRoutes(Integer shardId);

    ShardRouteModel getPrimaryRoute(Integer shardId);

    Set<ShardRouteModel> getReplicaRoutes(Integer shardId);

    void clear();

    void clearCaffeineCache();

    void clearRedisCache();

    void refresh();

    void applyToSharedRedisCaches(ShardRouteModel model);
    void applyToSharedRedisCaches(Set<ShardRouteModel> models);

    void applyToCaffeineCaches(Set<ShardRouteModel> models);

    void removeFromRedisCache(Integer shardId);

    void removeFromCaffeine(Integer shardId);
}
