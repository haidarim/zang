package io.github.haidarim.shard.api.common.type;

public enum LockReason {
    MIGRATION,
    NODE_ADDITION,
    NODE_REMOVAL,
    REBALANCE,
    MAINTENANCE,
    MANUAL
}
