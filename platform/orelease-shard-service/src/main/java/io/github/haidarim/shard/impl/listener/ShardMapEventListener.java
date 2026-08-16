package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.event.ShardMapCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.DELETED;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.UPDATED;

@Component
@RequiredArgsConstructor
public class ShardMapEventListener {
    private final RedisCachePublisher cachePublisher;
    private final ShardNodeCacheManager nodeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(ShardMapCacheEvent event){
        ShardMapModel model = event.getModel();
        if (model == null){
            return;
        }
        
        if(UPDATED.equals(event.getEventType())) {
            // Nodes
            updateNodeCacheAndUpdateOthers(model);

            // VirtualShards
            updateVirtualRedisCacheAndUpdateOthers(model);

            cachePublisher.publish(
                    new CacheMessage(event)
            );
        } else if (DELETED.equals(event.getEventType())) {
            removeFromNodeCacheAndUpdateOthers(model);
            removeFromVirtualCacheAndUpdateOthers(model);

            cachePublisher.publish(
                    new CacheMessage(event)
            );
        }

    }

    private void updateNodeCacheAndUpdateOthers(ShardMapModel model){
        Set<ShardNodeModel> nodeModels =  nodeCacheManager.getNodes(model.getShardId());
        if (nodeModels.isEmpty()){
            return;
        }

        nodeModels.forEach(m -> {
            m.setShardVersion(model.getVersion());
            m.setDatabaseName(model.getDatabaseName());
            m.setDomain(model.getDomain());
        });
        nodeCacheManager.applyToSharedRedisCaches(nodeModels);
    }

    private void updateVirtualRedisCacheAndUpdateOthers(ShardMapModel model){
        Set<VirtualShardModel> virtualShardModels = virtualShardCacheManager.getVirtualShardIds(model.getShardId());
        virtualShardModels.forEach(m -> {
            virtualShardCacheManager.applyToVirtualShardRedisCache(
                    VirtualShardModel.builder()
                            .shardId(model.getShardId())
                            .virtualShardId(m.getVirtualShardId())
                            .domain(model.getDomain().name())
                            .virtualVersion(m.getVirtualVersion())
                            .shardVersion(model.getVersion())
                            .build()
            );
        });
    }

    private void removeFromNodeCacheAndUpdateOthers(ShardMapModel model){
        Set<ShardNodeModel> nodeModels =  nodeCacheManager.getNodes(model.getShardId());
        if (nodeModels.isEmpty()){
            return;
        }

        nodeModels.forEach(m -> nodeCacheManager.removeFromRedisCache(m.getNodeId()));
    }

    private void removeFromVirtualCacheAndUpdateOthers(ShardMapModel model){
        Set<VirtualShardModel> virtualShardModels = virtualShardCacheManager.getVirtualShardIds(model.getShardId());
        virtualShardModels.forEach(m -> virtualShardCacheManager.removeFromRedisCaches(model.getShardId(), m.getIdentifier()));
    }
}
