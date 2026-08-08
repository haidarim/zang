package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.base.entity.ShardLock;
import io.github.haidarim.shard.base.entity.ShardMap;

public interface ShardLockService {

    ShardLock acquireLock(ShardMap shard, String owner);

    void releaseLock(Integer shardId);
}
