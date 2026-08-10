package io.github.haidarim.shard.cache;

import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.cache.message.CacheMessage;
import io.github.haidarim.shard.cache.message.VirtualShardCreatedCacheMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import static io.github.haidarim.shard.cache.Cache.CacheEventType.CREATED;

@Component
@RequiredArgsConstructor
public class RedisCacheSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final ShardNodeCacheManager nodeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;
    private final ShardRouteCacheManager routeCacheManager;

    @Override
    public void onMessage(@NonNull Message message, byte @Nullable [] pattern) {

        CacheMessage cacheMessage = mapper.readValue(
                message.getBody(),
                CacheMessage.class
        );

        switch (cacheMessage.getEntity()) {
            case VIRTUAL_SHARD -> {
                handleVirtualShardCacheMessage(cacheMessage);
                break;
            }
            case SHARD_MAP -> {

                break;
            }
            case SHARD_NODE -> {

                break;
            }
            default -> {
                throw new IllegalArgumentException("Unknown Message Entity: " + cacheMessage.getEntity());
            }
        }
    }

    private void handleVirtualShardCacheMessage(CacheMessage cacheMessage){
        if (CREATED.equals(cacheMessage.getEventType())){
            handleVirtualShardCreatedMessage(cacheMessage);
        }

    }

    private void handleVirtualShardCreatedMessage(CacheMessage cacheMessage){
        if (cacheMessage instanceof VirtualShardCreatedCacheMessage){
            virtualShardCacheManager.applyForLocalCache(
                    ((VirtualShardCreatedCacheMessage) cacheMessage).getModels()
            );

            return;
        }
        throw new IllegalArgumentException("Message not instance of handleVirtualShardCreatedMessage")
    }
}
