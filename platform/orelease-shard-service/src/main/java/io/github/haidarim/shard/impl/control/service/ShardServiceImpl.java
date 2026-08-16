package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.api.event.ShardMapCacheEvent;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.repository.*;
import io.github.haidarim.shard.impl.control.cache.CacheProperty;
import io.github.haidarim.shard.exception.ShardNotFoundException;
import io.github.haidarim.shard.exception.ShardValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<ShardMap> getAllShards() {
        return shardMapRepository.findAll();
    }

    @Override
    public ShardMap getShard(String shardName) {
        if (shardName == null || shardName.isBlank()){
            throw new ShardValidationException("Shard name cannot be null or blank");
        }
        return shardMapRepository.findByShardName(shardName.trim()).orElseThrow(() -> new ShardNotFoundException(shardName));
    }

    @Override
    public List<ShardMap> getShardsForDatabase(String databaseName, ShardDomain domain) {
        if(databaseName == null || databaseName.isBlank() || domain == null){
            throw new ShardValidationException("Invalid database name or domain");
        }

        return shardMapRepository.findShardsForDatabase(databaseName, domain);
    }

    @Override
    @Transactional
    public ShardMap createShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status) {
        if (shardMapRepository.existsByShardName(shardName)){
            throw new ShardValidationException("Shard already exists, shardName", shardName);
        }
        validateFieldsAreNotNull(shardName, databaseName, domain, status);

        ShardMap shard = new ShardMap(shardName.trim(), databaseName.trim(), domain, status);
        shardMapRepository.save(shard);

        virtualShardService.initializeMappingForShard(shard);

        return shard;
    }

    @Override
    @Transactional
    public ShardMap updateShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status, Long expectedVersion) {
        if (shardName == null || shardName.isBlank()){
            throw new ShardValidationException("Shard name cannot be null or blank");
        }

        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new ShardNotFoundException(shardName));
        validateShardVersion(shard.getVersion(), expectedVersion);

        ShardMapModel.ShardMapModelBuilder modelBuilder = null;
        if(databaseName != null && !databaseName.isBlank() && !databaseName.equals(shard.getDatabaseName())){
            shard.setDatabaseName(databaseName);
            modelBuilder = ShardMapModel.builder()
                    .databaseName(databaseName);
        }

        if(domain != null && !domain.equals(shard.getDomain())){
            shard.setDomain(domain);
            modelBuilder = (modelBuilder != null) ? modelBuilder.domain(domain) : ShardMapModel.builder().domain(domain);
        }

        if(status != null && !status.equals(shard.getStatus())){
            shard.setStatus(status);
            modelBuilder = (modelBuilder != null) ? modelBuilder.status(status) : ShardMapModel.builder().status(status);
        }

        if (modelBuilder != null){
            shard = shardMapRepository.saveAndFlush(shard);
            eventPublisher.publishEvent(
                    new ShardMapCacheEvent(
                            modelBuilder
                                    .shardId(shard.getShardId())
                                    .shardName(shard.getShardName())
                                    .version(shard.getVersion())
                                    .domain(shard.getDomain())
                                    .databaseName(shard.getDatabaseName())
                                    .status(shard.getStatus())
                                    .build(),
                            CacheProperty.CacheEventType.UPDATED
                    )
            );
        }

        return shard;
    }

    @Override
    @Transactional
    public Integer deleteShard(String shardName) {
        if (shardName == null || shardName.isBlank()){
            throw new ShardValidationException("Shard name cannot be null or blank");
        }

        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new ShardNotFoundException(shardName));
        validateForShardDeletion(shard.getShardId());
        shardMapRepository.delete(shard);
        eventPublisher.publishEvent(
                new ShardMapCacheEvent(
                        ShardMapModel.builder()
                                .shardId(shard.getShardId())
                                .build(),
                        CacheProperty.CacheEventType.DELETED
                )
        );
        return shard.getShardId();
    }

    private void validateFieldsAreNotNull(String shardName, String databaseName, ShardDomain domain, ShardStatus status){
        if(shardName == null || shardName.isBlank()){
            throw new ShardValidationException("Shard name cannot be null or empty");
        }
        if(databaseName == null || databaseName.isBlank()){
            throw new ShardValidationException("Database name cannot be null or empty");
        }
        if(domain == null){
            throw new ShardValidationException("Domain cannot be null or empty");
        }
        if (status == null){
            throw new ShardValidationException("Status cannot be null or empty");
        }
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
