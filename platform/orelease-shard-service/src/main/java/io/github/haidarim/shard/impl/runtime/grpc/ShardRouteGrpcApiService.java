package io.github.haidarim.shard.impl.runtime.grpc;

import io.github.haidarim.shard.grpc.ShardRouteApiServiceGrpc;
import io.github.haidarim.shard.grpc.ShardRouteRequest;
import io.github.haidarim.shard.grpc.ShardRouteResponse;
import io.grpc.stub.StreamObserver;

/**
 * ShardRouteGrpcApiService, handles runtime requests
 */
public class ShardRouteGrpcApiService extends ShardRouteApiServiceGrpc.ShardRouteApiServiceImplBase {

    @Override
    public void resolveRoute(ShardRouteRequest request, StreamObserver<ShardRouteResponse> responseStreamObserver){

    }
}
