package io.github.haidarim.shard.api.service;

import io.github.haidarim.shard.api.model.ShardRoute;
import io.github.haidarim.shard.api.type.ShardDomain;

import java.util.UUID;

public interface ShardResolver {

    ShardRoute resolve(ShardDomain domain, UUID entityId);
}
