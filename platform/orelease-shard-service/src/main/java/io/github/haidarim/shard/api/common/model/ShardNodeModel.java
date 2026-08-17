package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
public class ShardNodeModel extends CacheModel<Long> implements Serializable {
    private final Long nodeId;
    private final Integer shardId;
    private final String shardName;
    private final ShardStatus shardStatus;
    private final String hostName;
    private final Integer port;
    private final String region;
    private final NodeRole role;
    private final NodeStatus nodeStatus;
    private final String connectionSecret;
    private final Long nodeVersion;


    @Setter
    private ShardDomain domain;

    @Setter
    private String databaseName;

    @Setter
    private Long shardVersion;

    @Override
    public Long getIdentifier() {
        return nodeId;
    }

    @Override
    public Integer getShardId(){
        return shardId;
    }
}
