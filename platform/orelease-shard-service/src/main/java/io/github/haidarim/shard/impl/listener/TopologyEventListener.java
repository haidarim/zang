package io.github.haidarim.shard.impl.listener;


import io.github.haidarim.shard.api.common.model.ShardNodeModel;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.base.entity.ShardNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopologyEventListener {

    private final ShardNodeCacheManager nodeCacheManager;


    @EventListener
    public void handleNodeCreation(NodeCreatedEvent event){
//        ShardNodeModel model = mapToNodeModel(event.getNode());
//        nodeCacheManager.put(model.nodeId(), model);
    }

    private ShardNodeModel mapToNodeModel(ShardNode node){
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
}
