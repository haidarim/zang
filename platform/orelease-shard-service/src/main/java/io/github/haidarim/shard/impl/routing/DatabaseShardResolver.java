package io.github.haidarim.shard.impl.routing;

import io.github.haidarim.shard.api.model.ShardRoute;
import io.github.haidarim.shard.api.service.ShardResolver;
import io.github.haidarim.shard.api.service.VirtualSharCalculator;
import io.github.haidarim.shard.api.type.ShardDomain;
import io.github.haidarim.shard.impl.entity.VirtualShardMap;
import io.github.haidarim.shard.impl.entity.VirtualShardMapId;
import io.github.haidarim.shard.impl.repository.VirtualShardMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.github.haidarim.shard.api.constants.ShardConstants.NO_SHARD_FOUND;

@Service
@RequiredArgsConstructor
public class DatabaseShardResolver implements ShardResolver {

    private final VirtualSharCalculator calculator;

    private final VirtualShardMapRepository repository;

    @Override
    public ShardRoute resolve(ShardDomain domain, UUID entityId){
        int virtualShard = calculator.calculate(entityId);

        VirtualShardMapId virtualShardMapId = new VirtualShardMapId(domain, virtualShard);

        VirtualShardMap virtualShardMap = repository.findById(virtualShardMapId).orElseThrow(() -> new IllegalStateException(NO_SHARD_FOUND));

        return ShardRoute.builder()
                .virtualShardId(virtualShardMap.getVirtualShardMap().getShardId())
                .databaseName(virtualShardMap.getVirtualShardMap().getDatabaseName())
                .build();
    }
}
