package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.base.entity.ShardMigration;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardMigrationRepository extends JpaRepository<@NonNull ShardMigration, @NonNull Long> {
}
