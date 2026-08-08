package io.github.haidarim.shard.impl.control.topology;

import io.github.haidarim.shard.api.common.constants.ShardConstants;
import io.github.haidarim.shard.api.event.TopologyRefreshedEvent;
import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.control.service.ShardTopologyService;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.ShardNodeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.NO_SHARD_FOUND;
import static io.github.haidarim.shard.api.common.constants.ShardConstants.NO_SHARD_NODE_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShardTopologyServiceImpl implements ShardTopologyService {


    private final ApplicationEventPublisher eventPublisher;
    private final ShardRouteCacheManager routeCacheManager;
    private final ShardNodeCacheManager nodeCacheManager;
    private final VirtualShardCacheManager virtualShardCacheManager;

    @PostConstruct
    public void init(){
        refresh();
    }

    @Override
    public ShardRouteModel getRoute(Integer shardId) {
        return routeCacheManager.getRoute(shardId);
    }

    @Override
    public void refresh() {
        log.info("Starting topology cache refresh...");

        routeCacheManager.refresh();
        nodeCacheManager.refresh();
        virtualShardCacheManager.refresh();

        log.info("Updated route cache with {} routes", routeCacheManager.getAll().size());
        log.info("Updated node cache with {} nodes", nodeCacheManager.getAll().size());
        log.info("Updated virtual shards cache with {} virtualShards", virtualShardCacheManager.getAll().size());

        eventPublisher.publishEvent(new TopologyRefreshedEvent(Map.copyOf(routeCacheManager.getAll())));
    }
}
