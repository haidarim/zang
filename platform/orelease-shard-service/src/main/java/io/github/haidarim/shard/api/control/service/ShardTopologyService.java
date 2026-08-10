package io.github.haidarim.shard.api.control.service;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import io.github.haidarim.shard.base.entity.ShardNode;

import java.util.List;

public interface ShardTopologyService {



    void refresh();
}
