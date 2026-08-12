package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Builder
@Getter
@Setter
public class ShardRouteModel extends CacheModel<Integer> implements Serializable {
    private final int shardId;
    private final Long nodeId;
    private final NodeRole role;
    private final String shardName;
    private final String databaseName;
    private final String hostName;
    private final int port;

    private final Long shardVersion;
    private final Long nodeVersion;

    @Override
    public Integer getIdentifier() {
        return shardId;
    }
}
