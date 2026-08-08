package io.github.haidarim.shard.api.runtime.service;

import java.util.UUID;

public interface VirtualSharCalculator {

    int calculate(UUID entityId);
}
