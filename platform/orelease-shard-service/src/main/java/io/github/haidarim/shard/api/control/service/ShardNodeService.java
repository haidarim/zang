package io.github.haidarim.shard.api.control.service;


import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;

import java.util.List;

public interface ShardNodeService {

    /**
     * Get Node by nodeId
     * @param nodeId Long
     * @return node ShardNodeModel
     */
    ShardNodeModel getNodeById(Long nodeId);

    /**
     * Get Node by details
     * @param shardName String
     * @param hostName String
     * @param port Integer
     * @return node ShardNodeModel
     */
    ShardNodeModel getNodeByDetails(String shardName, String hostName, Integer port);

    /**
     * Get all nodes for shard by shardName
     * @param shardName String
     * @return nodes List
     */
    List<ShardNodeModel> getAllNodesForShard(String shardName);

    /**
     * Get all existing nodes
     * @return nodes List
     */
    List<ShardNodeModel> getAllNodes();

    /**
     * Create new node
     * @param shardName String
     * @param hostName String
     * @param port Integer
     * @param region String
     * @param role NodeRole
     * @param username String
     * @param connectionSecret Stirng
     * @param maxConnections Integer
     * @param weight Integer
     * @param status NodeStatus
     * @return node ShardNodeModel
     */
    ShardNodeModel createNode(String shardName, String hostName, Integer port, String region, NodeRole role,
                         String username, String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status);

    /**
     * Update node
     * @param nodeId Long
     * @param shardName String
     * @param hostName String
     * @param port Integer
     * @param region String
     * @param role NodeRole
     * @param username String
     * @param connectionSecret String
     * @param maxConnections Integer
     * @param weight Integer
     * @param status NodeStatus
     * @return node ShardNodeModel
     */
    ShardNodeModel updateNode(Long nodeId, String shardName, String hostName, Integer port, String region, NodeRole role,
                         String username, String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status);

    /**
     * Delete node
     * @param shardName String
     * @param hostName String
     * @param port Integer
     * @return nodeId Long
     */
    Long deleteNode(String shardName, String hostName, Integer port);
}
