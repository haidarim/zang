package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.VirtualShardModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMapId;


import java.util.Map;
import java.util.Set;

public interface VirtualShardCacheManager {

    VirtualShardModel getVirtualShard(Integer virtualId, ShardDomain domain);

    void put(Integer virtualId, ShardDomain domain, VirtualShardModel model);

    void putAll(Set<VirtualShardModel> virtualShardModelMap);

    void remove(VirtualShardMapId virtualId);

    void clear();

    Map<VirtualShardMapId, VirtualShardModel> getAll();

    void refresh();
}
