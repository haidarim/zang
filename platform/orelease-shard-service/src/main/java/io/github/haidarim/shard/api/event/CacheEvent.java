package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import lombok.Getter;

@Getter
public class CacheEvent {
    private final CacheProperty.CacheEntity entity;
    private final CacheProperty.CacheEventType eventType;

    public CacheEvent(CacheProperty.CacheEntity entity, CacheProperty.CacheEventType eventType ){
        this.entity = entity;
        this.eventType = eventType;
    }
}
