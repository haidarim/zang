package io.github.haidarim.shard.impl.repository;

import io.github.haidarim.shard.impl.entity.ShardMap;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardMapRepository extends JpaRepository<@NonNull ShardMap, @NonNull Integer> {
}
