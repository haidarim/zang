package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.Builder;

// TODO DTO
@Builder
public record ShardMapModel (
        Integer shardId,
        String shardName,
        String databaseName,
        ShardDomain domain,
        ShardStatus status
){
}
