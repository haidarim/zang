package io.github.haidarim.shard.impl.runtime.grpc;

import io.github.haidarim.shard.generated.grpc.ShardRouteApiServiceGrpc;
import io.github.haidarim.shard.generated.grpc.ShardRouteRequest;
import io.github.haidarim.shard.generated.grpc.ShardRouteResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * ShardRouteGrpcApiService, handles runtime requests
 */
@GrpcService
@RequiredArgsConstructor
public class RouteGrpcApiService extends ShardRouteApiServiceGrpc.ShardRouteApiServiceImplBase {

    @Override
    public void resolveRoute(ShardRouteRequest request, StreamObserver<ShardRouteResponse> responseStreamObserver){

    }
}
