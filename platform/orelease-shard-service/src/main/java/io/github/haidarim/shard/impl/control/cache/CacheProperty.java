package io.github.haidarim.shard.impl.control.cache;

import io.github.haidarim.shard.api.common.type.RouteIntent;

public final class CacheProperty {

    public static final int BATCH_SIZE = 1000;

    public static final String REDIS_KEY_ROUTE_PREFIX = "shard:route:%d:%s";
    public static final String REDIS_KEY_VIRTUAL_SHARD_PREFIX = "shard:virtual:%s:%d";
    public static final String REDIS_KEY_SHARD_NODE_PREFIX = "shard:node:%d";
    public static final String REDIS_KEY_SHARD_NODE_BY_SHARD_ID = "shard:nodes:%d";

    public static final String ALL_ROUTE_KEYS = "shard:route:*";
    public static final String ALL_VIRTUAL_SHARD_KEYS = "shard:virtual:*";
    public static final String ALL_SHARD_NODE_KEYS = "shard:node:*";
    public static final String ALL_SHARD_NODE_INDEX_KEYS = "shard:nodes:*";

    public static final String SHARD_NODE_CHANNEL = "channel:shard-node";
    public static final String SHARD_MAP_CHANNEL = "channel:shard-map";
    public static final String VIRTUAL_SHARD_CHANNEL = "channel:virtual-shard";


    static String shardRoute(Integer shardId, RouteIntent routeIntent){
        return String.format(REDIS_KEY_ROUTE_PREFIX, shardId, routeIntent.name());
    }

    static String virtualShard(String domain, Integer virtualShardId){
        return String.format(REDIS_KEY_VIRTUAL_SHARD_PREFIX, domain, virtualShardId);
    }

    static String shardNode(Long nodeId){
        return String.format(REDIS_KEY_SHARD_NODE_PREFIX, nodeId);
    }
    static String shardNodeByShardId(Integer shardId){
        return String.format(REDIS_KEY_SHARD_NODE_BY_SHARD_ID, shardId);
    }

    public static enum CacheEntity {
        SHARD_MAP,
        SHARD_NODE,
        SHARD_ROUTE,
        VIRTUAL_SHARD
    }

    public static enum CacheEventType {
        CREATED,
        UPDATED,
        DELETED,
        REFRESH
    }
}
