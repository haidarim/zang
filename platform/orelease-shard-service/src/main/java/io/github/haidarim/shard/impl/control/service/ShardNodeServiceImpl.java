package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardNodeService;
import io.github.haidarim.shard.api.event.NodeCacheEvent;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import io.github.haidarim.shard.exception.NodeNotFoundException;
import io.github.haidarim.shard.exception.NodeValidationException;
import io.github.haidarim.shard.exception.ShardNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.github.haidarim.shard.api.common.type.NodeStatus.ONLINE;
import static io.github.haidarim.shard.api.common.type.ShardStatus.ACTIVE;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.CREATED;
import static io.github.haidarim.shard.impl.control.cache.CacheProperty.CacheEventType.UPDATED;


@Service
@Transactional
@RequiredArgsConstructor
public class ShardNodeServiceImpl implements ShardNodeService {

    private final ShardNodeRepository nodeRepository;
    private final ShardMapRepository shardMapRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ShardNode getNodeById(Long nodeId) {
        if (nodeId == null){
            throw new NodeNotFoundException("nodeId cannot be null");
        }

        return nodeRepository.findById(nodeId).orElseThrow(() -> new NodeNotFoundException(nodeId.toString()));
    }

    @Override
    public ShardNode getNodeByDetails(String shardName, String hostName, Integer port) {
        validateNodeDetailsParameters(shardName, hostName, port);

        return nodeRepository.findByNodeShardMap_ShardNameAndHostNameAndPort(shardName.trim(), hostName, port)
                .orElseThrow(() -> new NodeNotFoundException(shardName, hostName, port));
    }

    @Override
    public List<ShardNode> getAllNodesForShard(String shardName) {
        if (shardName == null || shardName.isBlank()){
            throw new NodeValidationException("Shard name cannot be null or empty: " + shardName);
        }

        return nodeRepository.findAllByNodeShardMap_ShardName(shardName.trim());
    }

    @Override
    public List<ShardNode> getAllNodes() {
        return nodeRepository.findAll();
    }

    @Override
    public ShardNode createNode(String shardName, String hostName, Integer port, String region, NodeRole role, String username, String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status) {
        validateNodeDetailsParameters(shardName, hostName, port);
        validateNodeMandatoryFields(region, role, username, connectionSecret, maxConnections, weight, status);
        String trimmedShardName = shardName.trim();
        if(!shardMapRepository.existsByShardName(trimmedShardName)){
            throw new NodeValidationException("Shard does not exists");
        }

        if (nodeRepository.existsByNodeShardMap_ShardNameAndHostNameAndPort(trimmedShardName, hostName, port)){
            throw new NodeValidationException("Node Already exists");
        }
        ShardMap shard = shardMapRepository.findByShardName(trimmedShardName).orElseThrow(() -> new ShardNotFoundException(shardName));
        ShardNode node = new ShardNode(shard, hostName, port, region, role, username, connectionSecret, maxConnections, weight, status);

        nodeRepository.saveAndFlush(node);
        return node;
    }

    @Override
    public ShardNode updateNode(Long nodeId, String shardName, String hostName, Integer port, String region, NodeRole role, String username, String connectionSecret, Integer maxConnections, Integer weight, NodeStatus status) {
        if (nodeId == null){
            throw new NodeValidationException("nodeId cannot be null!");
        }

        ShardNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new NodeValidationException("Node not exists"));
        String currentShardName = node.getNodeShardMap().getShardName();

        ShardNodeModel.ShardNodeModelBuilder nodeCacheModelBuilder =  null;
        if (shardNameShouldBeUpdated(shardName, currentShardName)){
            ShardMap newShard = shardMapRepository.findByShardName(shardName.trim()).orElseThrow(() -> new ShardNotFoundException(shardName));
            node.setNodeShardMap(newShard);

            nodeCacheModelBuilder = ShardNodeModel.builder();
        }

        if (hostName != null && !hostName.isBlank() && !hostName.trim().equals(node.getHostName())){
            node.setHostName(hostName.trim());
            nodeCacheModelBuilder = (nodeCacheModelBuilder == null) ? ShardNodeModel.builder().hostName(hostName.trim()) : nodeCacheModelBuilder.hostName(hostName.trim());
        }

        if (port != null){
            validatePortRange(port);
            node.setPort(port);
            nodeCacheModelBuilder = (nodeCacheModelBuilder == null) ? ShardNodeModel.builder().port(port) : nodeCacheModelBuilder.port(port);
        }

        if (region != null && !region.isBlank() && !region.trim().equals(node.getRegion())){
            node.setRegion(region.trim());
            nodeCacheModelBuilder = (nodeCacheModelBuilder == null) ? ShardNodeModel.builder().region(region.trim()) : nodeCacheModelBuilder.region(region.trim());
        }

        if(role != null && !role.equals(node.getNodeRole())){
            node.setNodeRole(role);
            nodeCacheModelBuilder = (nodeCacheModelBuilder == null) ? ShardNodeModel.builder().role(role) : nodeCacheModelBuilder.role(role);
        }

        if(username != null && !username.isBlank() && !username.trim().equals(node.getUsername())){
            node.setUsername(username.trim());
        }

        if(connectionSecret != null && !connectionSecret.isBlank() && !connectionSecret.trim().equals(node.getConnectionSecret())){
            node.setConnectionSecret(connectionSecret.trim());
        }

        if(maxConnections != null && maxConnections > 0 && !maxConnections.equals(node.getMaxConnections())){
            node.setMaxConnections(maxConnections);
        }

        if(weight != null && weight > 0 && !weight.equals(node.getWeight())){
            node.setWeight(weight);
        }

        if (status != null && !status.equals(node.getNodeStatus())){
            node.setNodeStatus(status);
        }

        nodeRepository.saveAndFlush(node);
        checkForStatusUpdates(node.getNodeShardMap().getStatus(), node.getNodeStatus());
        if(nodeCacheModelBuilder != null && ONLINE.equals(node.getNodeStatus()) && ACTIVE.equals(node.getNodeShardMap().getStatus())){
            eventPublisher.publishEvent(
                new NodeCacheEvent(
                        nodeCacheModelBuilder
                                .nodeId(node.getNodeId())
                                .nodeVersion(node.getVersion())
                                .shardId(node.getNodeShardMap().getShardId())
                                .shardName(node.getNodeShardMap().getShardName())
                                .shardStatus(node.getNodeShardMap().getStatus())
                                .domain(node.getNodeShardMap().getDomain())
                                .shardVersion(node.getNodeShardMap().getVersion())
                                .build(),
                        UPDATED
                )
            );
        }

        return node;
    }

    @Override
    public Long deleteNode(String shardName, String hostName, Integer port) {
        return 0L;
    }

    private void validateNodeDetailsParameters(String shardName, String hostName, Integer port){
        if (shardName == null || shardName.isBlank()){
            throw new NodeValidationException("Shard name cannot be null or empty: " + shardName);
        }

        if (hostName == null || hostName.isBlank()) {
            throw new NodeValidationException("Host name cannot be null or empty: " + hostName);
        }

        if (port == null) {
            throw new NodeValidationException("Port cannot be null");
        }

        validatePortRange(port);
    }

    private void validatePortRange(Integer port){

        if (port < 1 || port > 65535){
            throw new NodeValidationException("Port must be between 1 and 65535: " + port);
        }
    }

    private void validateNodeMandatoryFields(String region, NodeRole role, String username, String connectionSecret, Integer maxConnection, Integer weight, NodeStatus status){
        if (region == null || region.isBlank()) {
            throw new NodeValidationException("Region is required");
        }

        if (role == null) {
            throw new NodeValidationException("Node role is required");
        }

        if (username == null || username.isBlank()) {
            throw new NodeValidationException("Username is required");
        }

        if (connectionSecret == null || connectionSecret.isBlank()) {
            throw new NodeValidationException("Connection secret is required");
        }

        if (maxConnection != null && maxConnection <= 0) {
            throw new NodeValidationException("Max connections must be greater than 0");
        }

        if (weight != null && weight <= 0) {
            throw new NodeValidationException("Weight must be greater than 0");
        }

        if (status == null) {
            throw new NodeValidationException("Node status is required");
        }
    }

    private boolean shardNameShouldBeUpdated(String newShardName, String currentShardName){
        return newShardName != null &&
                !newShardName.isBlank()
                && !currentShardName.equals(newShardName);
    }

    private void checkForStatusUpdates(ShardStatus shardStatus, NodeStatus nodeStatus){
        // TODO
    }
}
