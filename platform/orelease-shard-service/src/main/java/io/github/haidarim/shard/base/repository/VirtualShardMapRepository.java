package io.github.haidarim.shard.base.repository;


import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualShardMapRepository extends JpaRepository<@NonNull VirtualShardMap, @NonNull VirtualShardMapId> {

    boolean existsByPhysicalShardMap_ShardId(Integer shardId);

    @Query("SELECT vm FROM VirtualShardMap vm WHERE vm.physicalShardMap.status = 'ACTIVE'")
    List<VirtualShardMap> findAllActiveMappings();

    @Query("SELECT vm FROM VirtualShardMap vm WHERE vm.physicalShardMap.status = 'ACTIVE' AND vm.id = :id")
    Optional<VirtualShardMap> findActiveVirtualShardMapById(@Param("id") VirtualShardMapId id);

    @Query("SELECT vm FROM VirtualShardMap vm WHERE vm.physicalShardMap.status = 'ACTIVE' AND vm.physicalShardMap.shardId = :shardId")
    List<VirtualShardMap> findAllActiveVirtualIdsByShardId(@Param("shardId") Integer shardId);

    boolean existsById_Domain(ShardDomain domain);

    List<VirtualShardMap> findAllById_Domain(ShardDomain domain);

    List<VirtualShardMap> findAllById_DomainOrderById_VirtualShardId(
            ShardDomain domain
    );
}
