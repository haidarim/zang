package io.github.haidarim.shard.impl.control.service;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShardServiceImpl implements ShardService {

    private final ShardMapRepository shardMapRepository;

    @Override
    public List<ShardMap> getAllShards() {
        return shardMapRepository.findAll();
    }

    @Override
    public ShardMap getShard(String shardName) {
        return shardMapRepository.findByShardName(shardName).orElseThrow(() -> new NotFoundException("Shard Not Found for shardName", shardName));
    }

    @Override
    public List<ShardMap> getShardsForDatabase(String databaseName, ShardDomain domain) {
        return shardMapRepository.findShardsForDatabase(databaseName, domain).orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public ShardMap createShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status) {
        ShardMap shard = new ShardMap(shardName, databaseName, domain, status);
        shardMapRepository.save(shard);
        return shard;
    }

    @Override
    @Transactional
    public ShardMap updateShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status, Long expectedVersion) {
        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new NotFoundException("Shard Not Found for shardName", shardName));

        shard.setDatabaseName(databaseName);
        shard.setDomain(domain);
        shard.setStatus(status);
        shard.setVersion(expectedVersion); // need validation?

        return shard;
    }

    @Override
    @Transactional
    public Integer deleteShard(String shardName) {
        ShardMap shard = shardMapRepository.findByShardName(shardName).orElseThrow(() -> new NotFoundException("Shard Not Found for shardName", shardName));
        shardMapRepository.delete(shard); // check for FKs
        return shard.getShardId();
    }
}
