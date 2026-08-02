package io.github.haidarim.shard.impl.repository;


import io.github.haidarim.shard.impl.entity.VirtualShardMap;
import io.github.haidarim.shard.impl.entity.VirtualShardMapId;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualShardMapRepository extends JpaRepository<@NonNull VirtualShardMap, @NonNull VirtualShardMapId> {
}
