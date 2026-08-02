package io.github.haidarim.shard.impl.repository;

import io.github.haidarim.shard.impl.entity.ShardTopologyVersion;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardTopologyVersionRepository extends JpaRepository<@NonNull ShardTopologyVersion, @NonNull Integer> {
}
