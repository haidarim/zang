package io.github.haidarim.shard.config;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.impl.control.cache.RedisCacheSubscriber;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.*;


@Configuration
public class RedisConfig {

    @Bean
    public StringRedisSerializer redisStringSerializer(){
        return new StringRedisSerializer();
    }

    @Bean
    public RedisTemplate<String, ShardNodeModel> shardNodeRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer redisStringSerializer
    ){
        return createTemplate(
                redisConnectionFactory,
                redisStringSerializer,
                new JacksonJsonRedisSerializer<>(ShardNodeModel.class)
        );
    }

    @Bean
    public RedisTemplate<String, Long> primaryRoutesRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer redisStringSerializer
    ){
        return createTemplate(
                redisConnectionFactory,
                redisStringSerializer,
                new JacksonJsonRedisSerializer<>(Long.class)
        );
    }

    @Bean
    public RedisTemplate<String, String> redisStringTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer redisStringSerializer
    ){
        return createTemplate(
                redisConnectionFactory,
                redisStringSerializer,
                redisStringSerializer
        );
    }

    @Bean
    public RedisTemplate<String, VirtualShardModel> virtualShardRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer redisStringSerializer
    ){
        return createTemplate(
                redisConnectionFactory,
                redisStringSerializer,
                new JacksonJsonRedisSerializer<>(VirtualShardModel.class)
        );
    }

    @Bean
    public RedisTemplate<String, Set<VirtualShardModel>> virtualSharIndexdRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer redisStringSerializer,
            JacksonJsonRedisSerializer<@NonNull Set<VirtualShardModel>> valueSerializer
    ){
        return createTemplate(
                redisConnectionFactory,
                redisStringSerializer,
                valueSerializer
        );
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisCacheSubscriber cacheSubscriber
    ){
        Collection<PatternTopic> topics = new ArrayList<>();
        topics.add(new PatternTopic(SHARD_NODE_CHANNEL));
        topics.add(new PatternTopic(SHARD_MAP_CHANNEL));
        topics.add(new PatternTopic(VIRTUAL_SHARD_CHANNEL));

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                cacheSubscriber,
                topics
        );

        return container;
    }

    private <V> RedisTemplate<String, V> createTemplate(
            RedisConnectionFactory connectionFactory,
            StringRedisSerializer keySerializer,
            RedisSerializer<@NonNull V> valueSerializer
    ){
        RedisTemplate<String, V> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);

        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
