package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.RouteIntent;
import lombok.Builder;

import java.io.Serializable;


@Builder
public record ShardRouteModel(
        int shardId,
        String shardName,
        String databaseName,
        String hostName,
        int port,
        long topologyVersion,
        RouteIntent routeIntent
) implements Serializable {

}
