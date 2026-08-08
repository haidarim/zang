package io.github.haidarim.shard.api.control.command;

public record RemoveNodeCommand (
        Integer shardId,
        String hostName,
        Integer port
) {
}
