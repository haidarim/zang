package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.impl.control.cache.Cache;
import lombok.Getter;

import static io.github.haidarim.shard.impl.control.cache.Cache.CacheEntity.SHARD_MAP;

@Getter
public class ShardMapCacheEvent extends CacheEvent{
    private final ShardMapModel model;

    public ShardMapCacheEvent(ShardMapModel model, Cache.CacheEventType eventType){
        super(SHARD_MAP, eventType);
        this.model = model;
    }
}
