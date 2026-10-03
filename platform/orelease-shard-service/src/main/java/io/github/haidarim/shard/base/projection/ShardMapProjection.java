package io.github.haidarim.shard.base.projection;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;

/**
 * ShardMapProjection
 */
public interface ShardMapProjection {

    Integer getShardId();
    String getShardName();
    String getDatabaseName();
    ShardDomain getDomain();
    ShardStatus getStatus();
    Long getVersion();
}
