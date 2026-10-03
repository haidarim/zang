package io.github.haidarim.shard.base.repository;


import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;
import io.github.haidarim.shard.base.projection.VirtualShardProjection;
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

    @Query("""
           SELECT
                vm.id.virtualShardId AS virtualShardId,
                vm.physicalShardMap.domain AS domain,
                vm.physicalShardMap.shardId AS shardId,
                vm.version AS virtualVersion,
                vm.physicalShardMap.version AS shardVersion
           FROM VirtualShardMap vm
           WHERE vm.physicalShardMap.status = 'ACTIVE'
           """)
    List<VirtualShardProjection> findAllActiveMappings();

    @Query("""
           SELECT
                vm.id.virtualShardId AS virtualShardId,
                vm.physicalShardMap.domain AS domain,
                vm.physicalShardMap.shardId AS shardId,
                vm.version AS virtualVersion,
                vm.physicalShardMap.version AS shardVersion
           FROM VirtualShardMap vm
           WHERE vm.physicalShardMap.status = 'ACTIVE' AND vm.id = :id
           """)
    Optional<VirtualShardProjection> findActiveVirtualShardMapById(@Param("id") VirtualShardMapId id);

    @Query("""
           SELECT
                vm.id.virtualShardId AS virtualShardId,
                vm.physicalShardMap.domain AS domain,
                vm.physicalShardMap.shardId AS shardId,
                vm.version AS virtualVersion,
                vm.physicalShardMap.version AS shardVersion
           FROM VirtualShardMap vm
           WHERE vm.physicalShardMap.status = 'ACTIVE' AND vm.physicalShardMap.shardId = :shardId
           """)
    List<VirtualShardProjection> findAllActiveVirtualIdsByShardId(@Param("shardId") Integer shardId);

    boolean existsById_Domain(ShardDomain domain);

    @Query("""
           SELECT
                vm.id.virtualShardId AS virtualShardId,
                vm.physicalShardMap.domain AS domain,
                vm.physicalShardMap.shardId AS shardId,
                vm.version AS virtualVersion,
                vm.physicalShardMap.version AS shardVersion
           FROM VirtualShardMap vm
           WHERE vm.id.domain = :domain
           """)
    List<VirtualShardProjection> findAllById_Domain(ShardDomain domain);

    List<VirtualShardMap> findAllById_DomainOrderById_VirtualShardId(
            ShardDomain domain
    );
}
