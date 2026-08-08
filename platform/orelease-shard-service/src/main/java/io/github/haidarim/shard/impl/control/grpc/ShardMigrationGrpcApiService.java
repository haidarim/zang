package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.grpc.*;
import io.grpc.stub.StreamObserver;

/**
 * ShardMigrationGrpcApiService
 */
public class ShardMigrationGrpcApiService extends ShardMigrationApiServiceGrpc.ShardMigrationApiServiceImplBase {

    @Override
    public void getMigration(GetMigrationRequest request, StreamObserver<GetMigrationResponse> responseStreamObserver){}

    @Override
    public void getAllMigrations(GetAllMigrationsRequest request, StreamObserver<GetAllMigrationsResponse> responseStreamObserver){}

    @Override
    public void createMigration(CreateMigrationRequest request, StreamObserver<CreateMigrationResponse>responseStreamObserver){}

    @Override
    public void updateMigration(UpdateMigrationRequest request, StreamObserver<UpdateMigrationResponse> responseStreamObserver){}

    @Override
    public void cancelMigration(CancelMigrationRequest request, StreamObserver<CancelMigrationResponse> responseStreamObserver){}
}
