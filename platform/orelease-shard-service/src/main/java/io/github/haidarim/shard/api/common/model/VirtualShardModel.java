package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
public class VirtualShardModel extends CacheModel<VirtualShardMapId> implements Serializable {
    private final Integer virtualShardId;
    private final String domain;
    private final Integer shardId;

    private final Long virtualVersion;
    private final Long shardVersion;

    @Override
    public VirtualShardMapId getIdentifier() {
        return new VirtualShardMapId(ShardDomain.valueOf(domain), virtualShardId);
    }

    @Override
    public Integer getShardId(){
        return shardId;
    }
}
