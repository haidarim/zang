package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import io.github.haidarim.shard.api.common.type.ShardDomain;

import java.util.UUID;

public interface ShardResolver {

    ShardRouteModel resolve(ShardDomain domain, UUID entityId, RouteIntent routeIntent);
}
