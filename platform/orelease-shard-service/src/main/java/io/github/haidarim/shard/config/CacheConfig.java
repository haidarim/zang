package io.github.haidarim.shard.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.Set;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<@NonNull Long, ShardNodeModel> shardNodeByIdCache(
            @Value("${caffeine.node-cache.max-size}") long  maxsize
    ){
        return Caffeine.newBuilder()
                .maximumSize(maxsize)
                .build();
    }

    @Bean
    public Cache<@NonNull Integer, Set<Long>> shardNodeIdByShardIdCache(
            @Value("${caffeine.node-cache-by-id.max-size}") long  maxsize
    ){
        return Caffeine.newBuilder()
                .maximumSize(maxsize)
                .build();
    }
}
