package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.event.CacheEvent;
import io.github.haidarim.shard.api.event.ShardMapCacheEvent;
import io.github.haidarim.shard.api.event.VirtualShardCacheEvent;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.*;

@Component
@RequiredArgsConstructor
public class RedisCacheSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final ShardNodeCacheManager nodeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;

    @Override
    public void onMessage(@NonNull Message message, byte @Nullable [] pattern) {

        CacheEvent event = mapper.readValue(message.getBody(), CacheMessage.class).getEvent();
        if(event == null){
            throw new RuntimeException("Event cannot be null, error");
        }

        switch (event.getEntity()) {
            case VIRTUAL_SHARD -> {
                invalidateLocalCachesForVirtualShard(event);
                break;
            }
            case SHARD_MAP -> {
                invalidateLocalCachesForShard(event);
                break;
            }
            case SHARD_NODE -> {

                break;
            }
            default -> {
                throw new IllegalArgumentException("Unknown Message Entity: " + event.getEntity());
            }
        }
    }

    private void invalidateLocalCachesForShard(CacheEvent event){
        boolean shouldInvalidateShard = (event instanceof ShardMapCacheEvent) &&
                (UPDATED.equals(event.getEventType()) || DELETED.equals(event.getEventType())|| REPAIR.equals(event.getEventType()));
        if(shouldInvalidateShard){
            ShardMapModel model = ((ShardMapCacheEvent) event).getModel();

            Set<ShardNodeModel> nodeModels = nodeCacheManager.getNodes(model.getShardId())
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            nodeModels.forEach(n -> nodeCacheManager.removeFromCaffeine(n.getNodeId()));

            Set<VirtualShardModel> virtualModels = virtualShardCacheManager.getVirtualShardIds(model.getShardId())
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            virtualModels.forEach(v -> virtualShardCacheManager.removeFromCaffeineCaches(v.getShardId(), v.getIdentifier()));
        }
    }

    private void invalidateLocalCachesForVirtualShard(CacheEvent event){
        boolean shouldInvalidateVirtualShards = (event instanceof VirtualShardCacheEvent) &&
                (UPDATED.equals(event.getEventType()) || DELETED.equals(event.getEventType()) || REPAIR.equals(event.getEventType()));
        if(shouldInvalidateVirtualShards){
            Set<VirtualShardModel> models  = ((VirtualShardCacheEvent) event).getModels();
            models.forEach(m ->
                    virtualShardCacheManager.removeFromCaffeineCaches(m.getShardId(), m.getIdentifier())
            );
        }
    }
}
