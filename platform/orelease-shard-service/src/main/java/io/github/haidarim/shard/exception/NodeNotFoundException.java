package io.github.haidarim.shard.exception;

public class NodeNotFoundException extends NotFoundException{

    public NodeNotFoundException(String objectId){
        super("Shard Node not found for nodeId:", objectId);
    }


}
