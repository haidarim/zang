package io.github.haidarim.shard.base.repository;

import io.github.haidarim.shard.base.entity.ShardLock;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardLockRepository extends JpaRepository<@NonNull ShardLock, @NonNull Integer> {
    boolean existsByShard_ShardId(Integer shardId);
}
