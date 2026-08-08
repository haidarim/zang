package io.github.haidarim.shard.impl.runtime.grpc;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.runtime.service.ShardLockService;
import io.github.haidarim.shard.api.runtime.service.ShardResolver;
import io.github.haidarim.shard.grpc.ResolveShardRequest;
import io.github.haidarim.shard.grpc.ResolveShardResponse;
import io.github.haidarim.shard.grpc.ShardRuntimeServiceGrpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;


@GrpcService
@RequiredArgsConstructor
public class ShardRuntimeGrpcService extends ShardRuntimeServiceGrpc.ShardRuntimeServiceImplBase{

    private final ShardResolver shardResolver;
    private final ShardLockService shardLockService;

    @Override
    public void resolveShard(ResolveShardRequest request, StreamObserver<ResolveShardResponse> responseObserver){
        ShardDomain domain = ShardDomain.valueOf(request.getDomain().name());
        UUID entityId = UUID.fromString(request.getId());

        ShardRouteModel route = shardResolver.resolve(domain, entityId);

        ResolveShardResponse response = ResolveShardResponse.newBuilder()
                .setShardId(route.shardId())
                .setDatabase(route.databaseName())
                .node
    }
}
