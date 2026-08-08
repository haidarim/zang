package io.github.haidarim.shard.impl.control.topology;

import io.github.haidarim.shard.base.repository.ShardTopologyVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * Version management of Shards to give ability to microservices that have the correct shards
 */
@Service
@RequiredArgsConstructor
public class TopologyVersionManager {

    private final ShardTopologyVersionRepository repository;

//    private final

//    @Transactional
//    public Long updateTopology(ShardMap shardMap, Runnable changeLogic){
//        ShardMap map =
//        ShardTopologyVersion version = repository
//                .findById(1)
//                .orElseThrow();
//
//        version.setVersion(version.getVersion() +1);
//        return version.getVersion();
//    }
//
//    public Long current(){
//        return repository
//                .findById(1)
//                .orElseThrow()
//                .getVersion();
//    }
}
