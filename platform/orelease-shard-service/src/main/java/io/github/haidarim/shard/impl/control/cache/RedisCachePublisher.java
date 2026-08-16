package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;

@Component
@RequiredArgsConstructor
public class RedisCachePublisher {

    private final RedisTemplate<String, CacheMessage> redisTemplate;

    public void publish(CacheMessage message){
        redisTemplate.convertAndSend(
                getChannel(message),
                message
        );
    }

    private String getChannel(CacheMessage message){
        if (message != null && message.getEvent() != null){
            switch (message.getEvent().getEntity()){
                case SHARD_MAP -> {
                    return SHARD_MAP_CHANNEL;
                }
                case SHARD_NODE -> {
                    return SHARD_NODE_CHANNEL;
                }
                case VIRTUAL_SHARD -> {
                    return VIRTUAL_SHARD_CHANNEL;
                }
                default -> {
                    throw new RuntimeException("Invalid event entity type");
                }
            }
        }
        throw new RuntimeException("message and event cannot be null");
    }
}
