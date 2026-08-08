package io.github.haidarim.shard.cache;

public interface CacheConstants {

    int BATCH_SIZE = 1000;

    String REDIS_KEY_ROUTE_PREFIX = "shard:route:%d";
    String REDIS_KEY_VIRTUAL_SHARD_PREFIX = "shard:virtual:%s:%d";
    String REDIS_KEY_SHARD_NODE_PREFIX = "shard:node:%d";

    String ALL_ROUTE_KEYS = "shard:route:*";
    String ALL_VIRTUAL_SHARD_KEYS = "shard:virtual:*";
    String ALL_SHARD_NODE_KEYS = "shard:node:*";

    String SHARD_NODE_CHANNEL = "cache:shard-node";

    static String shardRoute(Integer shardId){
        return String.format(REDIS_KEY_ROUTE_PREFIX, shardId);
    }

    static String virtualShard(String domain, Integer virtualShardId){
            return String.format(REDIS_KEY_VIRTUAL_SHARD_PREFIX, domain, virtualShardId);
    }

    static String shardNode(Long nodeId){
        return String.format(REDIS_KEY_SHARD_NODE_PREFIX, nodeId);
    }
}
