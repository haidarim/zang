package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.event.VirtualShardCacheEvent;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.*;

@Component
@RequiredArgsConstructor
public class VirtualShardEventListener {

    private final RedisCachePublisher cachePublisher;
    private final VirtualShardCacheManager virtualShardCacheManager;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(VirtualShardCacheEvent event){
        Set<VirtualShardModel> models = event.getModels();
        if(models.isEmpty()){
           return;
        }

        CacheProperty.CacheEventType eventType = event.getEventType();

        if (CREATED.equals(eventType) || REPAIR.equals(eventType)) {
            virtualShardCacheManager.applyToVirtualShardRedisCache(models);
        }else if(DELETED.equals(eventType)){
                models.forEach(m -> virtualShardCacheManager.removeFromRedisCaches(m.getShardId(), m.getIdentifier()));
        }

        if (REPAIR.equals(eventType) || DELETED.equals(eventType)){
            cachePublisher.publish(new CacheMessage(event));
        }
    }
}
