package io.github.haidarim.shard.api.control.service;

import io.github.haidarim.shard.api.common.model.ShardMapModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.base.entity.ShardMap;

import java.util.List;

/***
 * ShardService, interface for {@link ShardMap} operation
 */
public interface ShardService {

    /**
     * Returns All ShardMap
     * @return shardMapModels List
     */
    List<ShardMapModel> getAllShards();

    /**
     * Returns ShardMap
     * @param shardName String
     * @return shard ShardMapModel
     */
    ShardMapModel getShard(String shardName);

    /**
     * Returns all shard maps for given database and domain
     * @param databaseName String
     * @param domain {@link io.github.haidarim.shard.api.common.type.ShardDomain}
     * @return shardMapModels List
     */
    List<ShardMapModel> getShardsForDatabase(String databaseName, ShardDomain domain);

    /**
     * Creates new shard
     * @param shardName String
     * @param databaseName String
     * @param domain {@link ShardDomain}
     * @param status {@link io.github.haidarim.shard.api.common.type.ShardStatus}
     * @return shard {@link ShardMapModel}
     */
    ShardMapModel createShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status);

    /**
     * Update shard
     * @param shardName String
     * @param databaseName String
     * @param status ShardStatus
     * @param expectedVersion Long
     * @return shard {@link ShardMapModel}
     */
    ShardMapModel updateShard(String shardName, String databaseName, ShardStatus status, Long expectedVersion);

    /**
     * Delete shard
     * @param shardName String
     * @return shardId Integer
     */
    Integer deleteShard(String shardName);
}
