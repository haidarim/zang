package io.github.haidarim.shard.base.projection;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.Setter;

public interface ShardNodeProjection {
    Long getNodeId();
    Integer getShardId();
    String getShardName();
    ShardStatus getShardStatus();
    String getHostName();
    Integer getPort();
    String getRegion();
    NodeRole getRole();
    NodeStatus getNodeStatus();
    Long getNodeVersion();
    ShardDomain getDomain();
    String getDatabaseName();
    Long getShardVersion();
}
