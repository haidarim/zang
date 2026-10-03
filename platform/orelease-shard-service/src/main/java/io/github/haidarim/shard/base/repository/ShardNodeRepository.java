package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.base.entity.ShardNode;
import io.github.haidarim.shard.base.projection.ShardNodeProjection;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShardNodeRepository extends JpaRepository<@NonNull ShardNode, @NonNull Long> {


    @Query("""
           SELECT
                 node.nodeId AS nodeId,
                 shard.shardId AS shardId,
                 shard.shardName AS shardName,
                 shard.status AS shardStatus,
                 node.hostName AS hostName,
                 node.port AS port,
                 node.region AS region,
                 node.nodeRole AS role,
                 node.nodeStatus AS nodeStatus,
                 node.version AS nodeVersion,
                 shard.domain AS domain,
                 shard.databaseName AS databaseName,
                 shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE node.nodeShardMap.shardId = :shardId
           AND node.nodeStatus = :status
           AND node.nodeRole = :role
           """)
    List<ShardNodeProjection> fetchByShardIdAndStatusAndRole(
            @Param("shardId") Integer shardId,
            @Param("status") NodeStatus status,
            @Param("role") NodeRole role
    );

    boolean existsByNodeShardMap_ShardId(Integer shardId);
    boolean existsByNodeShardMap_ShardNameAndHostNameAndPort(String shardName, String hostName, Integer port);


    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE
                node.nodeStatus = :nodeStatus
                AND
                node.nodeShardMap.status = :shardStatus
                AND
                node.nodeShardMap.shardId = :shardId
           """)
    List<ShardNodeProjection> findByShardIdAndNodeStatusAndShardStatus(
            @Param("shardId") Integer shardId,
            @Param("nodeStatus") NodeStatus nodeStatus,
            @Param("shardStatus")ShardStatus shardStatus
    );

    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE
                node.nodeStatus = :nodeStatus
                AND
                node.nodeShardMap.status = :shardStatus
           """)
    List<ShardNodeProjection> findByNodeStatusAndShardStatus(
            @Param("nodeStatus") NodeStatus nodeStatus,
            @Param("shardStatus")ShardStatus shardStatus
    );

    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE
                shard.shardName = :shardName
                AND
                node.hostName = :hostName
                AND
                node.port = :port
           """)
    Optional<ShardNodeProjection> findByNodeShardMap_ShardNameAndHostNameAndPort(String shardName, String hostName, Integer port);

    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE
                shard.shardName = :shardName
           """)
    List<ShardNodeProjection> findAllByNodeShardMap_ShardName(String shardName);

    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           WHERE
                node.nodeId = :nodeId
           """)
    Optional<ShardNodeProjection> findProjectionByNodeId(Long nodeId);

    @Query("""
           SELECT
                node.nodeId AS nodeId,
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.status AS shardStatus,
                node.hostName AS hostName,
                node.port AS port,
                node.region AS region,
                node.nodeRole AS role,
                node.nodeStatus AS nodeStatus,
                node.version AS nodeVersion,
                shard.domain AS domain,
                shard.databaseName AS databaseName,
                shard.version AS shardVersion
           FROM ShardNode node
           JOIN node.nodeShardMap shard
           """)
    List<ShardNodeProjection> findAllProjections();
}
