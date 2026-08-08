package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;

import java.util.Map;
import java.util.Set;

public interface ShardRouteCacheManager {

    ShardRouteModel getRoute(Integer shardId);

    void put(Integer shardId, ShardRouteModel model);

    void putAll(Set<ShardRouteModel> models);

    void remove(Integer shardId);

    void clear();

    Map<Integer, ShardRouteModel> getAll();

    void refresh();
}
