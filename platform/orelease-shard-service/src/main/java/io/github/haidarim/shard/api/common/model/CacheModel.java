package io.github.haidarim.shard.api.common.model;

import io.github.haidarim.shard.base.entity.VirtualShardMapId;

public abstract class CacheModel<I> {

    public I getModelIdentifier(){
        I identifier = getIdentifier();
        if(identifier instanceof Long){
            return identifier;
        }

        if(identifier instanceof Integer){
            return identifier;
        }

        if (identifier instanceof VirtualShardMapId){
            return identifier;
        }

        throw new RuntimeException("Invalid identifier type: " + identifier.getClass().getSimpleName());
    }

    public abstract I getIdentifier();
    public abstract Integer getShardId();
}
