package io.github.haidarim.shard.exception;

public class NodeNotFoundException extends NotFoundException{

    public NodeNotFoundException(String objectId){
        super("Shard Node not found for nodeId: ", objectId);
    }

    public NodeNotFoundException(String shardName, String hostName, Integer port){
        super("Shard Node not found for shardName: "+ shardName
                + ", hostName: " + hostName + ", port: " + port);
    }
}
