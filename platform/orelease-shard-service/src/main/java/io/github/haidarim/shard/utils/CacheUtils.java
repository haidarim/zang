package io.github.haidarim.shard.utils;

import io.github.haidarim.shard.api.common.model.CacheModel;
import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.RouteIntent;
import io.github.haidarim.shard.base.entity.ShardNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.impl.control.cache.CacheProperty.BATCH_SIZE;

@Slf4j
public final class CacheUtils {

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

    public static <I, M extends CacheModel<I>> Map<Integer, Set<I>> toShardIndexMap(Set<M> models){
        return models
                .stream()
                .collect(Collectors.groupingBy(
                        CacheModel::getShardId,
                        Collectors.mapping(
                                CacheModel::getModelIdentifier,
                                Collectors.toSet()
                        )
                ));
    }

    public static ShardNodeModel mapToShardNodeModel(ShardNode node){
        return ShardNodeModel.builder()
                .nodeId(node.getNodeId())
                .shardId(node.getNodeShardMap().getShardId())
                .shardName(node.getNodeShardMap().getShardName())
                .databaseName(node.getNodeShardMap().getDatabaseName())
                .hostName(node.getHostName())
                .port(node.getPort())
                .region(node.getRegion())
                .domain(node.getNodeShardMap().getDomain())
                .role(node.getNodeRole())
                .status(node.getNodeStatus())
                .connectionSecret(node.getConnectionSecret())
                .nodeVersion(node.getVersion())
                .shardVersion(node.getNodeShardMap().getVersion())
                .build();
    }

    public static ShardRouteModel mapToRouteModel(ShardNodeModel nodeModel){
        return ShardRouteModel.builder()
                .shardId(nodeModel.getShardId())
                .shardName(nodeModel.getShardName())
                .databaseName(nodeModel.getDatabaseName())
                .hostName(nodeModel.getHostName())
                .port(nodeModel.getPort())
                .shardVersion(nodeModel.getShardVersion())
                .nodeVersion(nodeModel.getNodeVersion())
                .build();
    }

    public static ShardRouteModel mapToRouteModel(ShardNode node){
        return ShardRouteModel.builder()
                .shardId(node.getNodeShardMap().getShardId())
                .shardName(node.getNodeShardMap().getShardName())
                .databaseName(node.getNodeShardMap().getDatabaseName())
                .hostName(node.getHostName())
                .port(node.getPort())
                .shardVersion(node.getNodeShardMap().getVersion())
                .nodeVersion(node.getVersion())
                .build();
    }
}
