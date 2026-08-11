package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class ShardNodeModel extends CacheModel<Long> implements Serializable {
    private final Long nodeId;
    private final Integer shardId;
    private final String hostName;
    private final Integer port;
    private final String region;
    private final ShardDomain domain;
    private final NodeRole role;
    private final NodeStatus status;
    private final String connectionSecret;

    @Override
    public Long getIdentifier() {
        return nodeId;
    }
}
