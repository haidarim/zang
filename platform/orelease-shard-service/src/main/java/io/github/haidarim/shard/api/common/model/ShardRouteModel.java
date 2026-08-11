package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.RouteIntent;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;


@Builder
@Getter
public class ShardRouteModel extends CacheModel<Integer> implements Serializable {
    private final int shardId;
    private final String shardName;
    private final String databaseName;
    private final String hostName;
    private final int port;
    private final long topologyVersion;
    private final RouteIntent routeIntent;

    @Override
    public Integer getIdentifier() {
        return shardId;
    }
}
