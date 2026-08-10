package io.github.haidarim.shard.impl.runtime.routing;

import io.github.haidarim.shard.api.runtime.service.VirtualSharCalculator;
import org.springframework.stereotype.Component;
import java.util.UUID;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.ENTITY_ID_CANNOT_BE_NULL;
import static io.github.haidarim.shard.api.common.constants.ShardConstants.VIRTUAL_SHARDS;

@Component
public class HashVirtualShardCalculator implements VirtualSharCalculator {


    @Override
    public int calculate(UUID entityId){
        if (entityId == null){
            throw new IllegalArgumentException(ENTITY_ID_CANNOT_BE_NULL);
        }

        long hash = entityId.getLeastSignificantBits()
                ^ entityId.getMostSignificantBits();

        return Math.floorMod(hash, VIRTUAL_SHARDS);
    }
}
