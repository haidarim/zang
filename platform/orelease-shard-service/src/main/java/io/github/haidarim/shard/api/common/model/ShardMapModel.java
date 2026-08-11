package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// TODO DTO
@Builder
@Getter
public class ShardMapModel extends CacheModel<Integer>{
    private final Integer shardId;
    private final String shardName;
    private final String databaseName;
    private final ShardDomain domain;
    private final ShardStatus status;

    @Override
    public Integer getIdentifier() {
        return shardId;
    }
}
