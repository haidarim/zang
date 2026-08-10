package io.github.haidarim.shard.cache.message;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.cache.Cache;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class VirtualShardCreatedCacheMessage extends CacheMessage{

    private final Set<VirtualShardModel> models;

    public VirtualShardCreatedCacheMessage(
            Cache.CacheEntity entity,
            Cache.CacheEventType eventType,
            Set<VirtualShardModel> models
    ){
        super(entity, eventType);
        this.models = new HashSet<>(models);
    }
}
