package io.github.haidarim.shard.impl.runtime.routing;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import io.github.haidarim.shard.api.runtime.service.*;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DatabaseShardResolver implements ShardResolver {

    private final VirtualSharCalculator calculator;

    private final ShardRouteCacheManager routeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;

    @Override
    public ShardRouteModel resolve(ShardDomain domain, UUID entityId, RouteIntent routeIntent){
        int virtualShard = calculator.calculate(entityId);
        VirtualShardModel virtualShardModel = virtualShardCacheManager.getVirtualShard(virtualShard, domain);

        return routeCacheManager.getRoute(virtualShardModel.shardId(), routeIntent);
    }
}
