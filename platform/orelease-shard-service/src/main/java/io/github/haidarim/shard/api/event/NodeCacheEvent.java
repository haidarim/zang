package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class NodeCacheEvent extends CacheEvent{

    private final Set<ShardNodeModel> models;

    public NodeCacheEvent(Set<ShardNodeModel> models, CacheProperty.CacheEventType eventType){
        super(CacheProperty.CacheEntity.SHARD_NODE, eventType);
        this.models = new HashSet<>(models);
    }
}
