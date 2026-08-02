package io.github.haidarim.shard.impl.repository;

import io.github.haidarim.shard.impl.entity.ShardMigration;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardMigrationRepository extends JpaRepository<@NonNull ShardMigration, @NonNull Long> {
}
