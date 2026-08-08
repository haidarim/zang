package io.github.haidarim.shard.cache;

import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RedisCacheSubscriber implements MessageListener {

    private final ObjectMapper mapper;
    private final ShardNodeCacheManager nodeCacheManager;

    @Override
    public void onMessage(@NonNull Message message, byte @Nullable [] pattern) {
        try{
            CacheMessage event = mapper.readValue(
                    message.getBody(),
                    CacheMessage.class
            );

            switch (event.eventType()){
                case CREATED, UPDATED ->{
                    // local cahe only
                    break;
                }
                case DELETED -> {
                    // local cache only
                    break;
                }

                case REFRESH -> {
                    // cache refresh
                    break;
                }
            }
        }catch (Exception e){
            throw new RuntimeException("Cache Synchronization failed, error", e);
        }
    }
}
