package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.generated.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ShardGrpcApiService, handles requests for shard operations as part of control plane
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ShardGrpcApiService extends ShardMapApiServiceGrpc.ShardMapApiServiceImplBase {

    private final ShardService shardService;

    @Override
    public void getAllShards(GetAllShardsRequest request, StreamObserver<GetAllShardsResponse> responseStreamObserver){
        try {
            List<ShardMap> shards =  shardService.getAllShards();
            GetAllShardsResponse response = GetAllShardsResponse.newBuilder()
                    .addAllShards(
                            shards.stream()
                            .map(this::toShardResponse)
                            .collect(Collectors.toList())
                    )
                    .build();

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process getAllShard request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    @Override
    public void getShard(GetShardRequest request, StreamObserver<GetShardResponse> responseStreamObserver){
        try{
            GetShardResponse response = toShardResponse(
                    shardService.getShard(request.getShardName())
            );

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process getShard request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    @Override
    public void getShardsForDatabase(GetShardsForDatabaseRequest request, StreamObserver<GetShardsForDatabaseResponse> responseStreamObserver){
        try{
            List<ShardMap> shards = shardService.getShardsForDatabase(request.getDatabaseName(), io.github.haidarim.shard.api.common.type.ShardDomain.valueOf(request.getDomain().name()));
            GetShardsForDatabaseResponse response = GetShardsForDatabaseResponse.newBuilder()
                    .addAllShards(
                            shards.stream().map(
                                    this::toShardResponse
                            ).collect(Collectors.toList())
                    )
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process getShardsForDatabase request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    @Override
    public void createShard(CreateShardRequest request, StreamObserver<CreateShardResponse> responseStreamObserver){
        try {
            ShardMap shard = shardService.createShard(request.getShardName(), request.getDatabaseName(), io.github.haidarim.shard.api.common.type.ShardDomain.valueOf(request.getDomain().name()), io.github.haidarim.shard.api.common.type.ShardStatus.valueOf(request.getStatus().name()));
            CreateShardResponse response = CreateShardResponse.newBuilder()
                    .setShardId(shard.getShardId())
                    .setShardName(shard.getShardName())
                    .build();

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process createShard request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    @Override
    public void updateShard(UpdateShardRequest request, StreamObserver<UpdateShardResponse> responseStreamObserver){
        try {
            ShardMap shard = shardService
                    .updateShard(request.getShardName(), request.getDatabaseName(), io.github.haidarim.shard.api.common.type.ShardStatus.valueOf(request.getStatus().name()), request.getExpectedVersion());
            UpdateShardResponse response = UpdateShardResponse.newBuilder()
                    .setShardId(shard.getShardId())
                    .setShardName(shard.getShardName())
                    .setVersion(shard.getVersion())
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process updateShard request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    @Override
    public void deleteShard(DeleteShardRequest request, StreamObserver<DeleteShardResponse> responseStreamObserver){
        try{
            DeleteShardResponse response = DeleteShardResponse.newBuilder()
                    .setShardId(
                            shardService.deleteShard(request.getShardName())
                    )
                    .build();

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        }catch (Exception e){
            log.error("Failed to process deleteShard request: {}, error: {}", request, e.getMessage());
            responseStreamObserver.onError(toShardGrpcException(e));
        }
    }

    private GetShardResponse toShardResponse(ShardMap shard){
        return GetShardResponse.newBuilder()
                .setShardId(shard.getShardId())
                .setShardName(shard.getShardName())
                .setDatabaseName(shard.getDatabaseName())
                .setDomain(ShardDomain.valueOf(shard.getDomain().name()))
                .setStatus(ShardStatus.valueOf(shard.getStatus().name()))
                .setVersion(shard.getVersion())
                .build();
    }

    private io.grpc.StatusRuntimeException toShardGrpcException(Exception e){
        return Status.INTERNAL
                .withDescription(
                        e.getMessage() != null ? e.getMessage()
                                : "Internal Server Error"
                )
                .withCause(e)
                .asRuntimeException();
    }
}
