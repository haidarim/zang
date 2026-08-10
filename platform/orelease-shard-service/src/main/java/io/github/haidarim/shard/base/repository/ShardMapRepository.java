package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.ShardMap;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShardMapRepository extends JpaRepository<@NonNull ShardMap, @NonNull Integer> {

    @Query("SELECT shard FROM ShardMap shard WHERE shard.shardName = :shardName")
    Optional<ShardMap> findByShardName(@Param("shardName") String shardName);

    @Query("SELECT shard FROM ShardMap shard WHERE shard.databaseName = :databaseName AND shard.domain = :domain")
    Optional<List<ShardMap>> findShardsForDatabase(@Param("databaseName") String databaseName, @Param("domain") ShardDomain domain);
}
