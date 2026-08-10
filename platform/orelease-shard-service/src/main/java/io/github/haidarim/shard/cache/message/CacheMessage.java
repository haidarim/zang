package io.github.haidarim.shard.cache.message;


import io.github.haidarim.shard.cache.Cache;
import lombok.Getter;

@Getter
public abstract class CacheMessage{
    private final Cache.CacheEntity entity;
    private final Cache.CacheEventType eventType;

    public CacheMessage(Cache.CacheEntity entity, Cache.CacheEventType eventType){
        this.entity = entity;
        this.eventType = eventType;
    }
}
