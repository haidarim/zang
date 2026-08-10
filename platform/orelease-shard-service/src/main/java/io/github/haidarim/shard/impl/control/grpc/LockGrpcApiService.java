package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.generated.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * ShardLockGrpcApiService
 */
@GrpcService
@RequiredArgsConstructor
public class LockGrpcApiService extends ShardLockApiServiceGrpc.ShardLockApiServiceImplBase {

    @Override
    public void getShardLocks(GetShardLocksRequest request, StreamObserver<GetShardLocksResponse> responseStreamObserver){

    }

    @Override
    public void getAllLocksForShard(GetAllLocksForShardRequest request, StreamObserver<GetShardLocksResponse> responseStreamObserver){}

    @Override
    public void acquireLock(AcquireLockRequest request, StreamObserver<AcquireLockResponse> responseStreamObserver){}

    @Override
    public void releaseLock(ReleaseLockRequest request, StreamObserver<ReleaseLockResponse> responseStreamObserver){}
}
