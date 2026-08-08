package io.github.haidarim.shard.api.control.command;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.Builder;

@Builder
public record ShardCommand(
        String shardName,
        String databaseName,
        ShardDomain domain,
        ShardStatus status,
        Long version
) {
}
