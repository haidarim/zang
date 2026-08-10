package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.impl.control.cache.Cache;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class VirtualShardCacheEvent extends CacheEvent{

    private final Set<VirtualShardModel> models;

    public VirtualShardCacheEvent(Set<VirtualShardModel> models, Cache.CacheEventType eventType){
        super(Cache.CacheEntity.VIRTUAL_SHARD, eventType);
        this.models = new HashSet<>(models);
    }
}
