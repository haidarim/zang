package io.github.haidarim.shard.api.runtime.service;

import io.github.haidarim.shard.api.common.model.ShardNodeModel;

import java.util.Map;
import java.util.Set;

public interface ShardNodeCacheManager {

    ShardNodeModel getNode(Long nodeId);

    void put(Long nodeId, ShardNodeModel model);

    void putAll(Set<ShardNodeModel> nodeModelMap);

    void remove(Long nodeId);

    void clear();

    Map<Long, ShardNodeModel> getAll();

    void refresh();

    void refreshLocal(Long nodeId);

    void removeLocal(Long nodeId);
}
