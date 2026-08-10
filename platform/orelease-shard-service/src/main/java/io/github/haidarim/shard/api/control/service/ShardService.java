package io.github.haidarim.shard.api.control.service;

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
     * @return shardMaps List
     */
    List<ShardMap> getAllShards();

    /**
     * Returns ShardMap
     * @param shardName String
     * @return shard ShardMap
     */
    ShardMap getShard(String shardName);

    /**
     * Returns all shard maps for giben database and domain
     * @param databaseName String
     * @param domain {@link io.github.haidarim.shard.api.common.type.ShardDomain}
     * @return shardMaps List
     */
    List<ShardMap> getShardsForDatabase(String databaseName, ShardDomain domain);

    /**
     * Creates new shard
     * @param shardName String
     * @param databaseName String
     * @param domain {@link ShardDomain}
     * @param status {@link io.github.haidarim.shard.api.common.type.ShardStatus}
     * @return
     */
    ShardMap createShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status);

    ShardMap updateShard(String shardName, String databaseName, ShardDomain domain, ShardStatus status, Long expectedVersion);

    Integer deleteShard(String shardName);
}
