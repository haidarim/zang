package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ShardMapModel extends CacheModel<Integer>{
    private final Integer shardId;
    private final String shardName;
    private final String databaseName;
    private final ShardDomain domain;
    private final ShardStatus status;
    private final Long version;

    @Override
    public Integer getIdentifier() {
        return shardId;
    }
}
