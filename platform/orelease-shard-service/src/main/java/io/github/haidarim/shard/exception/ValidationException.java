package io.github.haidarim.shard.exception;

public class ValidationException extends RuntimeException{

    public ValidationException(String message, String objectId){
        super(message + ": "+ objectId);
    }

    public ValidationException(String message){
        super(message);
    }
}
