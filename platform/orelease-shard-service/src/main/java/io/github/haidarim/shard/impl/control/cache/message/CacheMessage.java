package io.github.haidarim.shard.impl.control.cache.message;


import io.github.haidarim.shard.api.event.CacheEvent;
import io.github.haidarim.shard.impl.control.cache.Cache;
import lombok.Getter;

@Getter
public class CacheMessage{
    private final CacheEvent event;

    public CacheMessage(CacheEvent event){
        this.event = event;
    }
}
