package io.github.haidarim.shard.impl.repository;

import io.github.haidarim.shard.impl.entity.ShardLock;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardLockRepository extends JpaRepository<@NonNull ShardLock, @NonNull Long> {

}
