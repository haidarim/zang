package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.event.NodeShardIndexCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class NodeEventListener {

    private final RedisCachePublisher cachePublisher;
    private final ShardRouteCacheManager routeCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(NodeShardIndexCacheEvent event){
        // update shared redis cache for route cache
        Set<ShardNodeModel> nodeModels = event.getModels();
        if(nodeModels == null || nodeModels.isEmpty()){
            return;
        }


        // publish cache message to other instances to update L1 caches
    }
}
