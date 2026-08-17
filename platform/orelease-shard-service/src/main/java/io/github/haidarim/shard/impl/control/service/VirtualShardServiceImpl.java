package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.api.event.VirtualShardCacheEvent;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.LARGE_PRIME_NUMBER;
import static io.github.haidarim.shard.api.common.constants.ShardConstants.VIRTUAL_SHARDS;

@Service
@RequiredArgsConstructor
public class VirtualShardServiceImpl implements VirtualShardService {

    private final VirtualShardMapRepository virtualShardMapRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ShardMapRepository shardMapRepository;

    @Override
    @Transactional
    public void initializeOrRebalanceVirtualShards(ShardMap newShard) {
        ShardDomain domain = newShard.getDomain();
        List<VirtualShardMap> mappings = virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(domain);

        if(mappings.isEmpty()){
            initializeVirtualShards(newShard);
            return;
        }

        rebalanceVirtualShards(domain, mappings);
    }

    @Transactional
    @Override
    public void rebalanceBeforeShardDeletion(ShardMap shard){
        ShardDomain domain = shard.getDomain();

        List<ShardMap> shards = shardMapRepository.findAllByDomainAndStatusOrderByShardId(domain, ShardStatus.ACTIVE);

        if(shards.isEmpty()){
            throw  new IllegalStateException("No Active shards available for domain: " + domain);
        }

        List<ShardMap> remainingShards = shards.stream()
                .filter(s -> !shard.getShardId().equals(s.getShardId()))
                .toList();

        if(remainingShards.isEmpty()){
            deleteVirtualShardsForDomain(domain);
            return;
        }

        List<VirtualShardMap> virtualShards = virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(domain);
        virtualShards = virtualShards.stream()
                .filter(vs -> shard.getShardId().equals(vs.getPhysicalShardMap().getShardId()))
                .toList();

        List<VirtualShardMap> changed = new ArrayList<>();

        for (VirtualShardMap virtualShard : virtualShards){
            int virtualShardId = virtualShard.getId().getVirtualShardId();
            ShardMap target = selectShard(domain, virtualShardId, remainingShards);
            virtualShard.setPhysicalShardMap(target);
            changed.add(virtualShard);
        }

        if(!changed.isEmpty()){
            virtualShardMapRepository.saveAllAndFlush(changed);
            publishVirtualShardCacheEvent(
                    changed, CacheProperty.CacheEventType.REPAIR
            );
        }
    }

    private void initializeVirtualShards(ShardMap newShard){
        List<VirtualShardMap> newVirtualShards  = new ArrayList<>();
        for (int virtualShardId = 0; virtualShardId < VIRTUAL_SHARDS; virtualShardId++){
            VirtualShardMapId id =
                    new VirtualShardMapId(
                            newShard.getDomain(),
                            virtualShardId
                    );

            newVirtualShards.add(new VirtualShardMap(id, newShard));
        }

        virtualShardMapRepository.saveAllAndFlush(newVirtualShards);

        publishVirtualShardCacheEvent(newVirtualShards, CacheProperty.CacheEventType.CREATED);
    }


    private void rebalanceVirtualShards(ShardDomain domain, List<VirtualShardMap> mappings){
        List<ShardMap> shards = shardMapRepository.findAllByDomainAndStatusOrderByShardId(domain, ShardStatus.ACTIVE);

        if(shards.isEmpty()){
            throw  new IllegalStateException("No Active shards available for domain: " + domain);
        }

        List<VirtualShardMap> changedVirtualShards = new ArrayList<>();

        mappings.forEach(virtualShard -> {
            int virtualShardId = virtualShard.getId().getVirtualShardId();

            ShardMap targetShard = selectShard(domain, virtualShardId, shards);

            if(!targetShard.getShardId().equals(virtualShard.getPhysicalShardMap().getShardId())){
                virtualShard.setPhysicalShardMap(targetShard);
                changedVirtualShards.add(virtualShard);
            }
        });

        if (changedVirtualShards.isEmpty()){
            return;
        }

        virtualShardMapRepository.saveAllAndFlush(changedVirtualShards);

        publishVirtualShardCacheEvent(changedVirtualShards, CacheProperty.CacheEventType.REPAIR);
    }

    private ShardMap selectShard(ShardDomain domain, int virtualShardId, List<ShardMap> shards){
        ShardMap selectedShard = null;
        long bestScore = Long.MIN_VALUE;

        for (ShardMap shard : shards){
            long score = score(domain, virtualShardId, shard.getShardId());
            if(selectedShard == null || Long.compareUnsigned(score, bestScore) > 0){
                selectedShard = shard;
                bestScore = score;
            }
        }

        return  selectedShard;
    }

    private long score(ShardDomain domain, int virtualShardId, int shardId){
        long hash = LARGE_PRIME_NUMBER;

        hash = 31 * hash + domain.ordinal();
        hash = 31 * hash + virtualShardId;
        hash = 31 * hash + shardId;

        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);

        return hash;
    }

    private void deleteVirtualShardsForDomain(ShardDomain domain){
        List<VirtualShardMap> virtualShards = virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(domain);

        if(virtualShards.isEmpty()){
            throw new IllegalStateException("No virtual shards found with domain: " + domain + " for deletion");
        }

        virtualShardMapRepository.deleteAllInBatch(virtualShards);
        publishVirtualShardCacheEvent(virtualShards, CacheProperty.CacheEventType.DELETED);
    }

    private void publishVirtualShardCacheEvent(List<VirtualShardMap> virtualShards, CacheProperty.CacheEventType eventType){
        Set<VirtualShardModel> models = virtualShards.stream()
                .map(virtualShard -> VirtualShardModel.builder()
                        .virtualShardId(virtualShard.getId().getVirtualShardId())
                        .domain(virtualShard.getPhysicalShardMap().getDomain().name())
                        .shardId(virtualShard.getPhysicalShardMap().getShardId())
                        .shardVersion(virtualShard.getPhysicalShardMap().getVersion())
                        .virtualVersion(virtualShard.getVersion())
                        .build()
                ).collect(Collectors.toSet());

        eventPublisher.publishEvent(
                new VirtualShardCacheEvent(
                        models,
                        eventType
                )
        );
    }
}
