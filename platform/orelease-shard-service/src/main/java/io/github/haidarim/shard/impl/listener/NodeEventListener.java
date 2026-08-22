package io.github.haidarim.shard.impl.listener;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.event.NodeCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
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
    private final ShardNodeCacheManager nodeCacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEvent(NodeCacheEvent event){
        // update shared redis cache for route cache
        ShardNodeModel nodeModel = event.getModel();
        if(nodeModel == null){
            return;
        }

        CacheProperty.CacheEventType eventType = event.getEventType();
        switch (eventType){
            case UPDATED -> {
                updateCachesForNodeModel(nodeModel);
                break;
            }
            case DELETED -> {
                deleteNodeModelFromCaches(nodeModel);
                break;
            }
        }
    }

    private void updateCachesForNodeModel(ShardNodeModel nodeModel){
        nodeCacheManager.applyToSharedRedisCaches(nodeModel);
        routeCacheManager.applyPrimaryRouteToRedisCache(nodeModel.getShardId(), nodeModel.getNodeId());
        routeCacheManager.applyReplicaRoutesToRedisCache(nodeModel.getShardId(), Set.of(nodeModel.getNodeId()));
        sendMessageToInvalidate(nodeModel);
    }

    private void deleteNodeModelFromCaches(ShardNodeModel nodeModel){
        sendMessageToInvalidate(nodeModel);
    }

    private void sendMessageToInvalidate(ShardNodeModel nodeModel){

    }
}
