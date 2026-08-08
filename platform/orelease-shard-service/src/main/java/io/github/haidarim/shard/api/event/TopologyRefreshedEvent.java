package io.github.haidarim.shard.api.event;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Getter
@ToString
public class TopologyRefreshedEvent {

    private final Map<Integer, ShardRouteModel> routes;
    private final Instant timestamp;

    public TopologyRefreshedEvent(Map<Integer, ShardRouteModel> routes){
        this.routes = Collections.unmodifiableMap(routes);
        this.timestamp = Instant.now();
    }
}
