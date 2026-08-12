package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.api.event.VirtualShardCacheEvent;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.VIRTUAL_SHARDS;

@Service
@RequiredArgsConstructor
public class VirtualShardServiceImpl implements VirtualShardService {

    private final VirtualShardMapRepository virtualShardMapRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void initializeMappingForShard(ShardMap shard) {
        Set<VirtualShardModel> newCacheModels  = new HashSet<>();
        for (int virtualShardId = 0; virtualShardId < VIRTUAL_SHARDS; virtualShardId++){
            VirtualShardMapId id =
                    new VirtualShardMapId(
                            shard.getDomain(),
                            virtualShardId
                    );

            VirtualShardMap mapping = new VirtualShardMap(id, shard);
            virtualShardMapRepository.save(mapping);

            newCacheModels.add(
                    VirtualShardModel.builder()
                    .virtualShardId(virtualShardId)
                    .domain(shard.getDomain().name())
                    .shardId(shard.getShardId())
                    .build()
            );
        }

        eventPublisher.publishEvent(
                new VirtualShardCacheEvent(
                        newCacheModels,
                        CacheProperty.CacheEventType.REPAIR
                )
        );
    }
}
