package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.api.common.model.ShardRouteModel;
import io.github.haidarim.shard.api.control.service.ShardNodeService;
import io.github.haidarim.shard.api.runtime.service.ShardResolver;
import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ShardControlGrpcService
        extends ShardControlServiceGrpc.ShardControlServiceImplBase{

    private final ShardNodeService nodeService;

    @Override
    public void addNode(AddNodeRequest request, StreamObserver<AddNodeResponse> responseObserver){
        try {


            responseObserver.onNext(); // stream data
            responseObserver.onCompleted(); // after complete close the stream (connection/thread)

        } catch (IllegalArgumentException e) {
            // Handle malformed UUID strings explicitly (HTTP 400 equivalent)
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid UUID format: " + request.getId())
                    .withCause(e)
                    .asRuntimeException());

        } catch (Exception e) {
            // Catch all other unexpected failures (HTTP 500 equivalent)
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to resolve shard route due to internal error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
