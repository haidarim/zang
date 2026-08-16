package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.control.service.ShardNodeService;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import io.github.haidarim.shard.exception.NodeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class ShardNodeServiceImpl implements ShardNodeService {

    private final ShardNodeRepository nodeRepository;

    @Override
    public ShardNode getNodeById(Long nodeId) {
        if (nodeId == null){
            throw new NodeNotFoundException("nodeId cannot be null");
        }

        return nodeRepository.findById(nodeId).orElseThrow(() -> new NodeNotFoundException(nodeId.toString()));
    }

    @Override
    public ShardNode getNodeByDetails(String shardName, String hostName, Integer port) {
        return null;
    }

    @Override
    public List<ShardNode> getAllNodesForShard(String shardName) {
        return List.of();
    }

    @Override
    public List<ShardNode> getAllNodes() {
        return List.of();
    }

    @Override
    public ShardNode createNode(String shardName, String hostName, Integer port, String region, NodeRole role, String connectionSecret, Integer maxConnection, Integer weight, NodeStatus status) {
        return null;
    }

    @Override
    public ShardNode updateNode(Long nodeId, String hostName, Integer port, String region, NodeRole role, String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status) {
        return null;
    }

    @Override
    public Long deleteNode(String shardName, String hostName, Integer port) {
        return 0L;
    }
}
