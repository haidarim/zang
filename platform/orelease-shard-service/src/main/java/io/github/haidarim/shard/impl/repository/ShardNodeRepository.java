package io.github.haidarim.shard.impl.repository;

import io.github.haidarim.shard.impl.entity.ShardNode;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShardNodeRepository extends JpaRepository<@NonNull ShardNode, @NonNull Long> {
}
