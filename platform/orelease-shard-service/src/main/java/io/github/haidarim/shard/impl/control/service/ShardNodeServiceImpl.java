package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.control.command.NodeCommand;
import io.github.haidarim.shard.api.control.command.RemoveNodeCommand;
import io.github.haidarim.shard.api.control.service.ShardNodeService;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import io.github.haidarim.shard.cache.Cache;
import io.github.haidarim.shard.cache.message.CacheMessage;
import io.github.haidarim.shard.cache.RedisCachePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.haidarim.shard.api.common.type.NodeRole.PRIMARY;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShardNodeServiceImpl implements ShardNodeService {

    private final ShardMapRepository shardMapRepository;
    private final ShardNodeRepository shardNodeRepository;
    private final ShardNodeCacheManager nodeCacheManager;
    private final ShardRouteCacheManager routeCacheManager;
    private final RedisCachePublisher cachePublisher;

    @Override
    public void addNode(NodeCommand command) {
        validateCommandForCreation(command);
        ShardNode node = createNode(command);

        try {
            shardNodeRepository.save(node);
        }
        catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Node Already Exists Exception");
        }
        ShardNodeModel nodeModel = mapToShardNodeModel(node);
        nodeCacheManager.put(nodeModel.nodeId(), nodeModel);
        cachePublisher.publish(
                new CacheMessage(
                        Cache.CacheEntity.SHARD_NODE,
                        Cache.CacheEventType.CREATED,
                        nodeModel.nodeId()
                )
        );

        // if primary node
        if (PRIMARY.equals(command.nodeRole())){
            // update routeCache and send event to other instance update localCaches
            ShardRouteModel routeModel = mapToRouteModel(node);
            routeCacheManager.put(routeModel.shardId(), routeModel);
            cachePublisher.publish(
                    new CacheMessage(
                            Cache.CacheEntity.SHARD_ROUTE,
                            Cache.CacheEventType.UPDATED,
                            routeModel.shardId()
                    )
            );
        }
    }

    @Override
    public void updateNode(NodeCommand command) {
        validateCommandForCreation(command);
    }

    @Override
    public void removeNode(RemoveNodeCommand command) {

    }

    private void validateCommandForCreation(NodeCommand command){
        validateShardExists(command.shardId());
        validateMandatoryFields(command.shardId(), command.hostName(), command.port(), command.nodeRole());
    }

    private void validateShardExists(Integer shardId){
        if(shardId != null && shardMapRepository.existsById(shardId)){
            return;
        }
        throw new IllegalArgumentException("Shard Id does not exits, shardId: " + shardId);
    }

    private void validateMandatoryFields(Integer shardId, String hostName, Integer port, NodeRole role){
        if(hostName == null || hostName.isBlank()){
            throw new IllegalArgumentException("Host name cannot be empty or null: " + hostName);
        }

        if(port == null){
            throw new IllegalArgumentException("Port cannot be null");
        }

        ShardNode primaryNode = shardNodeRepository.fetchByShardIdAndStatusAndRole(shardId, NodeStatus.ONLINE, PRIMARY)
                .orElse(null);
        if((role==null || PRIMARY.equals(role)) && primaryNode != null){
            throw new IllegalArgumentException("Primary node already exists");
        }
    }

    private ShardNode createNode(NodeCommand command){
        ShardMap shard = shardMapRepository.findById(command.shardId()).orElseThrow(() -> new IllegalArgumentException("Shard Id does not exits"));
        return ShardNode.builder()
                .nodeShardMap(shard)
                .hostName(command.hostName())
                .port(command.port())
                .region(command.region())
                .nodeRole(command.nodeRole())
                .connectionSecret(command.connectionSecret())
                .maxConnections(command.maxConnections())
                .weight(command.weight())
                .nodeStatus(command.status())
                .build();
    }

    private ShardNodeModel mapToShardNodeModel(ShardNode node){
        return ShardNodeModel.builder()
                .nodeId(node.getNodeId())
                .shardId(node.getNodeShardMap().getShardId())
                .hostName(node.getHostName())
                .port(node.getPort())
                .region(node.getRegion())
                .domain(node.getNodeShardMap().getDomain())
                .role(node.getNodeRole())
                .status(node.getNodeStatus())
                .connectionSecret(node.getConnectionSecret())
                .build();
    }

    private ShardRouteModel mapToRouteModel(ShardNode node){
        return ShardRouteModel.builder()
                .shardId(node.getNodeShardMap().getShardId())
                .shardName(node.getNodeShardMap().getShardName())
                .databaseName(node.getNodeShardMap().getDatabaseName())
                .hostName(node.getHostName())
                .port(node.getPort())
                .topologyVersion(node.getNodeShardMap().getVersion())
                .build();
    }
}
