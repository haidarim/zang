package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.event.ShardMapCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static io.github.haidarim.shard.impl.control.cache.Cache.CacheEventType.UPDATED;

@Component
@RequiredArgsConstructor
public class ShardMapEventListener {
    private final RedisCachePublisher cachePublisher;
    private final ShardNodeCacheManager nodeCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(ShardMapCacheEvent event){
        ShardMapModel model = event.getModel();

        if(UPDATED.equals(event.getEventType())) {
            // Nodes
            updateLocalRedisNodesAndUpdateOthers(model);
            // Routes
            updateLocalRedisRoutesAndUpdateOthers(model);
            // VirtualShards
            updateLocalRedisVirtualShardsAndUpdateOthers(model);
        }
    }

    private void updateLocalRedisNodesAndUpdateOthers(ShardMapModel model){
        nodeCacheManager.
    }

    private void updateLocalRedisRoutesAndUpdateOthers(ShardMapModel model){

    }

    private void updateLocalRedisVirtualShardsAndUpdateOthers(ShardMapModel model){

    }

}
