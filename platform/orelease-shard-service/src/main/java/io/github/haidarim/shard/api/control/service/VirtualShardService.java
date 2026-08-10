package io.github.haidarim.shard.api.control.service;

import io.github.haidarim.shard.base.entity.ShardMap;

/**
 * VirtualShardService
 */
public interface VirtualShardService {

    void initializeMappingForShard(ShardMap shard);
}
