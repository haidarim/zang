package io.github.haidarim.shard.base.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardTopologyVersionRepository extends JpaRepository<@NonNull ShardTopologyVersion, @NonNull Integer> {
}
