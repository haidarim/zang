package io.github.haidarim.shard.api.common.model;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record VirtualShardModel(
        Integer virtualShardId,
        String domain,
        Integer shardId,
        Long version
) implements Serializable {
}
