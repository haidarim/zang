package io.github.haidarim.shard.cache;

public final class Cache {

    static int BATCH_SIZE = 1000;

    static String REDIS_KEY_ROUTE_PREFIX = "shard:route:%d";
    static String REDIS_KEY_VIRTUAL_SHARD_PREFIX = "shard:virtual:%s:%d";
    static String REDIS_KEY_SHARD_NODE_PREFIX = "shard:node:%d";

    static String ALL_ROUTE_KEYS = "shard:route:*";
    static String ALL_VIRTUAL_SHARD_KEYS = "shard:virtual:*";
    static String ALL_SHARD_NODE_KEYS = "shard:node:*";

    static String SHARD_NODE_CHANNEL = "cache:shard-node";

    static String shardRoute(Integer shardId){
        return String.format(REDIS_KEY_ROUTE_PREFIX, shardId);
    }

    static String virtualShard(String domain, Integer virtualShardId){
        return String.format(REDIS_KEY_VIRTUAL_SHARD_PREFIX, domain, virtualShardId);
    }

    static String shardNode(Long nodeId){
        return String.format(REDIS_KEY_SHARD_NODE_PREFIX, nodeId);
    }


    public static enum CacheEntity {
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
