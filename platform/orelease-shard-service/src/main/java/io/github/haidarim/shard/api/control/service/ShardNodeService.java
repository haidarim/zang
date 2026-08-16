package io.github.haidarim.shard.api.control.service;


import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.base.entity.ShardNode;

import java.util.List;

public interface ShardNodeService {

    ShardNode getNodeById(Long nodeId);

    ShardNode getNodeByDetails(String shardName, String hostName, Integer port);

    List<ShardNode> getAllNodesForShard(String shardName);

    List<ShardNode> getAllNodes();

    ShardNode createNode(String shardName, String hostName, Integer port, String region, NodeRole role,
                    String connectionSecret, Integer maxConnection, Integer weight, NodeStatus status);

    ShardNode updateNode(Long nodeId, String hostName, Integer port, String region, NodeRole role,
            String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status);

    Long deleteNode(String shardName, String hostName, Integer port);
}
