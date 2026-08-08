package io.github.haidarim.shard.api.control.command;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import lombok.Builder;

@Builder
public record NodeCommand(
        Integer shardId,
        String hostName,
        Integer port,
        String region,
        NodeRole nodeRole,
        String connectionSecret,
        int maxConnections,
        int weight,
        NodeStatus status
) {
}
