package io.github.haidarim.shard.api.control.service;


import io.github.haidarim.shard.api.control.command.NodeCommand;
import io.github.haidarim.shard.api.control.command.RemoveNodeCommand;

public interface ShardNodeService {

    void addNode(NodeCommand command);

    void updateNode(NodeCommand command);

    void removeNode(RemoveNodeCommand command);
}
