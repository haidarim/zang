package io.github.haidarim.shard.api.common.model;

import lombok.Builder;

import java.io.Serializable;


@Builder
public record ShardRouteModel(
        int shardId,
        String shardName,
        String databaseName,
        String hostName,
        int port,
        long topologyVersion
) implements Serializable {

}
