package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.event.NodeCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import io.github.haidarim.shard.impl.control.cache.RedisCachePublisher;
import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
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
    private final ShardNodeCacheManager nodeCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(NodeCacheEvent event){
        // update shared redis cache for route cache
        if(event.getModel() == null){
            return;
        }

        CacheProperty.CacheEventType eventType = event.getEventType();
        switch (eventType){
            case UPDATED -> {
                updateCachesForNodeModel(event);
                break;
            }
            case DELETED -> {
                deleteNodeModelFromCaches(event);
                break;
            }
            default -> {
                throw new IllegalArgumentException("Unsupported event type");
            }
        }
    }

    private void updateCachesForNodeModel(NodeCacheEvent event){
        ShardNodeModel nodeModel = event.getModel();
        nodeCacheManager.applyToSharedRedisCaches(nodeModel);

        routeCacheManager.applyPrimaryRouteToRedisCache(nodeModel.getShardId(), nodeModel.getNodeId());
        routeCacheManager.applyReplicaRoutesToRedisCache(nodeModel.getShardId(), Set.of(nodeModel.getNodeId()));
        sendMessageToInvalidate(event);
    }

    private void deleteNodeModelFromCaches(NodeCacheEvent event){
        ShardNodeModel nodeModel = event.getModel();
        nodeCacheManager.removeFromRedisCache(nodeModel.getNodeId());

        routeCacheManager.removeFromRedisCache(nodeModel.getShardId());

        sendMessageToInvalidate(event);
    }

    private void sendMessageToInvalidate(NodeCacheEvent event){
        cachePublisher.publish(
                new CacheMessage(event)
        );
    }
}
