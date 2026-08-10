package io.github.haidarim.shard.exception;

public class ShardNotFoundException extends NotFoundException {
    public ShardNotFoundException(String objectId) {
        super("Shard Not Found for shardName", objectId);
    }
}
