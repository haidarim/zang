package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.api.runtime.service.ShardNodeCacheManager;
import io.github.haidarim.shard.api.runtime.service.ShardRouteCacheManager;
import io.github.haidarim.shard.api.runtime.service.VirtualShardCacheManager;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.repository.*;
import io.github.haidarim.shard.cache.RedisCachePublisher;
import io.github.haidarim.shard.exception.ShardNotFoundException;
import io.github.haidarim.shard.exception.ShardValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShardServiceImpl implements ShardService {

    private final ShardMapRepository shardMapRepository;
    private final ShardNodeRepository nodeRepository;
    private final ShardLockRepository lockRepository;
    private final ShardMigrationRepository migrationRepository;
    private final VirtualShardMapRepository virtualShardRepository;

    private final VirtualShardService virtualShardService;

    private final VirtualShardCacheManager virtualShardCacheManager;
    private final ShardRouteCacheManager routeCacheManager;
    private final ShardNodeCacheManager nodeCacheManager;
    private final RedisCachePublisher cachePublisher;


    @Override
    public List<ShardMap> getAllShards() {
        return shardMapRepository.findAll();
    }

    @Override
    public ShardMap getShard(String shardName) {
        return shardMapRepository.findByShardName(shardName).orElseThrow(() -> new ShardNotFoundException(shardName));
    }

    @Override
    public List<ShardMap> getShardsForDatabase(String databaseName, ShardDomain domain) {
        return shardMapRepository.findShardsForDatabase(databaseName, domain);
    }

    @Override
    @Transactional
    public ShardMap createShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status) {
        if (shardMapRepository.existsByShardName(shardName)){
            throw new ShardValidationException("Shard already exists, shardName", shardName);
        }
        ShardMap shard = new ShardMap(shardName, databaseName, domain, status);
        shardMapRepository.save(shard);

        virtualShardService.initializeMappingForShard(shard);

        return shard;
    }

    @Override
    @Transactional
    public ShardMap updateShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status, Long expectedVersion) {
        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new ShardNotFoundException(shardName));

        validateShardVersion(shard.getVersion(), expectedVersion);
        shard.setDatabaseName(databaseName);
        shard.setDomain(domain);
        shard.setStatus(status);

        return shard;
    }

    @Override
    @Transactional
    public Integer deleteShard(String shardName) {
        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new ShardNotFoundException(shardName));
        validateForShardDeletion(shard.getShardId());
        shardMapRepository.delete(shard);
        return shard.getShardId();
    }

    private void validateShardVersion(Long actualVersion, Long expectedVersion){
        if (!actualVersion.equals(expectedVersion)){
            throw new ShardValidationException("Shard version is not equal to expected version", expectedVersion.toString());
        }
    }

    private void validateForShardDeletion(Integer shardId){
        // nodes
        if(nodeRepository.existsByNodeShardMap_ShardId(shardId)){
            throw new ShardValidationException("Shard cannot be deleted, node exists for shard", shardId.toString());
        }
        // locks
        if (lockRepository.existsByShard_ShardId(shardId)){
            throw new ShardValidationException("Shard cannot be deleted, lock exists for shard", shardId.toString());
        }
        // migrations
        if(migrationRepository.existsActiveMigration(shardId)){
            throw new ShardValidationException("Shard cannot be deleted, active migration exists for shard", shardId.toString());
        }
        // virtual mapping
        if (virtualShardRepository.existsByPhysicalShardMap_ShardId(shardId)){
            throw new ShardValidationException("Shard cannot be deleted, virtual shard exists for shard", shardId.toString());
        }
    }
}
