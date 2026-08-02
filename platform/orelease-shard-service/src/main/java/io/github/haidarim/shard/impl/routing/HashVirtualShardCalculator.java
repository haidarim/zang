package io.github.haidarim.shard.impl.routing;

import io.github.haidarim.shard.api.service.VirtualSharCalculator;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.github.haidarim.shard.api.constants.ShardConstants.ENTITY_ID_CANNOT_BE_NULL;
import static io.github.haidarim.shard.api.constants.ShardConstants.VIRTUAL_SHARDS;

@Component
public class HashVirtualShardCalculator implements VirtualSharCalculator {


    @Override
    public int calculate(UUID entityId){
        if (entityId == null){
            throw new IllegalArgumentException(ENTITY_ID_CANNOT_BE_NULL);
        }

        String entityIdValue = entityId
                .toString()
                .replace("-", "")
                .substring(0,8);

        long hash = Long.parseLong(entityIdValue, 16); // Hexadecimal base

        return (int) (hash % VIRTUAL_SHARDS);
    }
}
