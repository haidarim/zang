package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record ShardNodeModel(
        Long nodeId,
        Integer shardId,
        String hostName,
        Integer port,
        String region,
        ShardDomain domain,
        NodeRole role,
        NodeStatus status,
        String connectionSecret
) implements Serializable {
}
