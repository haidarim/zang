package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.impl.control.cache.Cache;
import lombok.Getter;

@Getter
public class CacheEvent {
    private final Cache.CacheEntity entity;
    private final Cache.CacheEventType eventType;

    public CacheEvent(Cache.CacheEntity entity, Cache.CacheEventType eventType ){
        this.entity = entity;
        this.eventType = eventType;
    }
}
