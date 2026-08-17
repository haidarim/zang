package io.github.haidarim.shard.api.control.service;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.ShardMap;

/**
 * VirtualShardService
 */
public interface VirtualShardService {

    void initializeOrRebalanceVirtualShards(ShardMap shard);

    void rebalanceBeforeShardDeletion(ShardMap shard);
}
