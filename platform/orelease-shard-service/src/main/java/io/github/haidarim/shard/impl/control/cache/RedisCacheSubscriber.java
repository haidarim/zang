package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.event.CacheEvent;
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

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.REPAIR;

@Component
@RequiredArgsConstructor
public class RedisCacheSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final ShardNodeCacheManager nodeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;
    private final ShardRouteCacheManager routeCacheManager;

    @Override
    public void onMessage(@NonNull Message message, byte @Nullable [] pattern) {

        CacheEvent event = mapper.readValue(message.getBody(), CacheMessage.class).getEvent();


        switch (event.getEntity()) {
            case VIRTUAL_SHARD -> {
                handleVirtualShardCacheMessage(event);
                break;
            }
            case SHARD_MAP -> {

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

    private void handleVirtualShardCacheMessage(CacheEvent event){
        if (REPAIR.equals(event.getEventType())){
            handleVirtualShardCreatedMessage(event);
        }

    }

    private void handleVirtualShardCreatedMessage(CacheEvent event){
        if (event instanceof VirtualShardCacheEvent){
            virtualShardCacheManager.applyToVirtualShardCache(
                    ((VirtualShardCacheEvent) event).getModels()
            );
            return;
        }
        throw new IllegalArgumentException("Message not instance of handleVirtualShardCreatedMessage");
    }
}
