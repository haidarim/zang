package io.github.haidarim.shard.exception;

public class ShardValidationException extends ValidationException {
    public ShardValidationException(String message) {
        super(message);
    }

    public ShardValidationException(String message, String objectId) {
        super(message, objectId);
    }
}
