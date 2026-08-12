package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.event.NodeCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NodeEventListener {

    private final RedisCachePublisher cachePublisher;
    private final ShardRouteCacheManager routeCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(NodeCacheEvent event){
        // update shared redis cache for route cache

        // publish cache message to other instances to update L1 caches
    }
}
