package io.github.haidarim.shard.api.control.service;

import jakarta.transaction.Transactional;

public interface MigrationService {

    @Transactional
    void migrate(Integer fromShardId, Integer toShardId);
}
