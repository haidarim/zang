package io.github.haidarim.shard.impl.runtime.routing;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import io.github.haidarim.shard.api.runtime.service.*;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.github.haidarim.shard.api.common.type.RouteIntent.WRITE;


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

        if (virtualShardModel == null) {
            throw new IllegalStateException(
                    "No virtual shard mapping for domain=" + domain
                            + ", virtualShardId=" + virtualShard
            );
        }


        Integer shardId = virtualShardModel.getShardId();

        if (WRITE.equals(routeIntent)) {
            return requirePrimaryRoute(shardId);
        }

        return requireReplicaRoute(shardId);
    }

    private ShardRouteModel requirePrimaryRoute(Integer shardId) {
        ShardRouteModel route = routeCacheManager.getPrimaryRoute(shardId);

        if (route == null) {
            throw new IllegalStateException(
                    "No active primary route available for shardId=" + shardId
            );
        }

        return route;
    }

    private ShardRouteModel requireReplicaRoute(Integer shardId) {
        return routeCacheManager.getReplicaRoutes(shardId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No active replica route available for shardId=" + shardId
                        )
                );
    }
}
