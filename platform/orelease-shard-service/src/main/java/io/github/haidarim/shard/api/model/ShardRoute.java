package io.github.haidarim.shard.api.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShardRoute {

    int virtualShardId;

    int shardId;

    String databaseName;
}
