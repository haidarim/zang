package io.github.haidarim.shard.base.repository;


import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualShardMapRepository extends JpaRepository<@NonNull VirtualShardMap, @NonNull VirtualShardMapId> {

    boolean existsByPhysicalShardMap_ShardId(Integer shardId);

    @Query("SELECT vm FROM VirtualShardMap vm WHERE vm.physicalShardMap.status = 'ACTIVE'")
    List<VirtualShardMap> findAllActiveMappings();
}
