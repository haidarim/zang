package io.github.haidarim.shard.exception;

public class NotFoundException extends RuntimeException{


    public NotFoundException(String message, String objectId){
        super(message + ": "+ objectId);
    }
}
