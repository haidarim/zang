package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.api.common.type.MigrationStatus;
import io.github.haidarim.shard.base.entity.ShardMigration;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardMigrationRepository extends JpaRepository<@NonNull ShardMigration, @NonNull Long> {
    @Query("""
        SELECT COUNT(m) > 0 FROM ShardMigration m
        WHERE (m.fromShardMap.shardId = :shardId OR m.toShardMap.shardId = :shardId)
        AND m.status IN ('STARTED', 'COPYING', 'VALIDATING')
    """)
    boolean existsActiveMigration(
            @Param("shardId") Integer shardId
    );
}
