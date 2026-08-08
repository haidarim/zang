package io.github.haidarim.shard.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.HashSet;
import java.util.Set;

import static io.github.haidarim.shard.cache.CacheConstants.BATCH_SIZE;

@Slf4j
public class CacheUtils {

    public static <T> Set<String> getRedisKeys(RedisTemplate<String, T> redisTemplate, String keyPrefix){
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(keyPrefix)
                .count(BATCH_SIZE)
                .build();

        try(Cursor<@NonNull String> cursor = redisTemplate.scan(options)){
            while (cursor.hasNext()){
                keys.add(cursor.next());
            }
        }catch (Exception e){
            log.error("Exception while iterating over redis options, exception: {}", e.getMessage());
        }

        return keys;
    }
}
