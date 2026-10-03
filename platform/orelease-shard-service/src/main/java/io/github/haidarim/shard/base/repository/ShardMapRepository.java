package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.projection.ShardMapProjection;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShardMapRepository extends JpaRepository<@NonNull ShardMap, @NonNull Integer> {

    Optional<ShardMap> findShardMapByShardName(String shardName);

    @Query("""
           SELECT
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.databaseName AS databaseName,
                shard.domain AS domain,
                shard.status AS status,
                shard.version AS version
           FROM ShardMap shard
           WHERE shard.shardName = :shardName
           """)
    Optional<ShardMapProjection> findShardProjectionByShardName(String shardName);

    @Query("""
           SELECT
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.databaseName AS databaseName,
                shard.domain AS domain,
                shard.status AS status,
                shard.version AS version
           FROM ShardMap shard
           WHERE shard.databaseName = :databaseName AND shard.domain = :domain
           """)
    List<ShardMapProjection> findShardsForDatabase(@Param("databaseName") String databaseName, @Param("domain") ShardDomain domain);

    boolean existsByShardName(String shardName);

    List<ShardMap> findAllByDomainAndStatusOrderByShardId(ShardDomain domain, ShardStatus status);

    @Query("""
           SELECT
                shard.shardId AS shardId,
                shard.shardName AS shardName,
                shard.databaseName AS databaseName,
                shard.domain AS domain,
                shard.status AS status,
                shard.version AS version
           FROM ShardMap shard
           """)
    List<ShardMapProjection> findAllProjections();
}
