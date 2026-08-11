package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.impl.control.cache.message.CacheMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.SHARD_NODE_CHANNEL;

@Component
@RequiredArgsConstructor
public class RedisCachePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(CacheMessage message){
        redisTemplate.convertAndSend(
                SHARD_NODE_CHANNEL,
                message
        );
    }
}
