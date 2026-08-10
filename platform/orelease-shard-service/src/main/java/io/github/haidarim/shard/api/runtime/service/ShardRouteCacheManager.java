package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;

import java.util.Map;
import java.util.Set;

public interface ShardRouteCacheManager {

    ShardRouteModel getRoute(Integer shardId, RouteIntent routeIntent);

    void put(Integer shardId, ShardRouteModel model);

    void putAll(Set<ShardRouteModel> models);

    void remove(Integer shardId, RouteIntent routeIntent);

    void clear();

    Map<String, ShardRouteModel> getAll();

    void refresh();
}
