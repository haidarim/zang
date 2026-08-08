package io.github.haidarim.shard.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCachePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(CacheMessage message){
        redisTemplate.convertAndSend(
                CacheConstants.SHARD_NODE_CHANNEL,
                message
        );
    }
}
