package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.event.VirtualShardCacheEvent;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VirtualShardEventListener {

    private final RedisCachePublisher cachePublisher;
    private final VirtualShardCacheManager virtualShardCacheManager;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(VirtualShardCacheEvent event){
        virtualShardCacheManager.applyForSharedRedisCache(event.getModels());

        cachePublisher.publish(
                new CacheMessage(event)
        );
    }
}
