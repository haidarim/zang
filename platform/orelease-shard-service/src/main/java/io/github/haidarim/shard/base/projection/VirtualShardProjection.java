package io.github.haidarim.shard.base.projection;

import io.github.haidarim.shard.api.common.type.ShardDomain;

public interface VirtualShardProjection {
    Integer getVirtualShardId();
    ShardDomain getDomain();
    Integer getShardId();
    Long getVirtualVersion();
    Long getShardVersion();
}
