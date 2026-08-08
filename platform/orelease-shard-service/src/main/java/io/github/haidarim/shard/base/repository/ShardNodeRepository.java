package io.github.haidarim.shard.base.repository;

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

    @Query("SELECT node FROM ShardNode node WHERE node.nodeStatus = :nodeStatus")
    Optional<List<ShardNode>> fetchNodesByStatus(@Param("nodeStatus") String nodeStatus);

    @Query("""
           SELECT node FROM ShardNode node
           WHERE node.nodeShardMap.shardId = :shardId
           AND node.nodeStatus = 'ONLINE'
           AND node.nodeRole = 'PRIMARY'
           """)
    Optional<ShardNode> fetchByPrimaryNode(@Param("shardId") Integer shardId);

}
