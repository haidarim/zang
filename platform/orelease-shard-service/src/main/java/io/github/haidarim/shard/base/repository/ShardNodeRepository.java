package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.base.entity.ShardNode;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShardNodeRepository extends JpaRepository<@NonNull ShardNode, @NonNull Long> {

    List<ShardNode> fetchNodesByStatus(String nodeStatus);

    @Query("""
           SELECT node FROM ShardNode node
           WHERE node.nodeShardMap.shardId = :shardId
           AND node.nodeStatus = :status
           AND node.nodeRole = :role
           """)
    Optional<ShardNode> fetchByShardIdAndStatusAndRole(
            @Param("shardId") Integer shardId,
            @Param("status") NodeStatus status,
            @Param("role") NodeRole role
    );

    boolean existsByNodeShardMap_ShardId(Integer shardId);

    @Query("SELECT node FROM ShardNode node WHERE node.nodeStatus = 'ONLINE' AND node.nodeRole = 'PRIMARY'")
    List<ShardNode> findAllOnlineAndPrimaryNodes();

    List<ShardNode> findByNodeShardMap_ShardId(Integer shardId);
}
