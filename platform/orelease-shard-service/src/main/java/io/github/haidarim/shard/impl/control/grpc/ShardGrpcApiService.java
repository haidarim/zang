package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.generated.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * ShardGrpcApiService, handles requests for shard operations as part of control plane
 */
@GrpcService
@RequiredArgsConstructor
public class ShardGrpcApiService extends ShardMapApiServiceGrpc.ShardMapApiServiceImplBase {


    @Override
    public void getAllShards(GetAllShardsRequest request, StreamObserver<GetAllShardsResponse> responseStreamObserver){

    }

    @Override
    public void getShard(GetShardRequest request, StreamObserver<GetShardResponse> responseStreamObserver){

    }

    @Override
    public void getShardsForDatabase(GetShardsForDatabaseRequest request, StreamObserver<GetShardsForDatabaseResponse> responseStreamObserver){

    }

    @Override
    public void createShard(CreateShardRequest request, StreamObserver<CreateShardResponse> responseStreamObserver){

    }

    @Override
    public void updateShard(UpdateShardRequest request, StreamObserver<UpdateShardResponse> responseStreamObserver){}

    @Override
    public void deleteShard(DeleteShardRequest request, StreamObserver<DeleteShardResponse> responseStreamObserver){}
}
