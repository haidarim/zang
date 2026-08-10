package io.github.haidarim.shard.cache.message;

import io.github.haidarim.shard.cache.Cache;

public class ShardUpdatedCacheMessage extends CacheMessage {

    public ShardUpdatedCacheMessage(Cache.CacheEntity entity, Cache.CacheEventType eventType){
        super(entity, eventType);
    }
}
