package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VirtualShardEventListener {

    private final VirtualShardCacheManager virtualShardCacheManager;
    private final


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(VirtualShardCreatedEvent event){
        log.info("Going to add {} new virtual shard models to cache", event.getModels().size());
        event.getModels()
                .forEach(model ->
                        virtualShardCacheManager.put(
                                model.virtualShardId(),
                                ShardDomain.valueOf(model.domain()),
                                model
                        )
                );
        log.info("Added {} new virtual shard models to cache", event.getModels().size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(){

    }
}
