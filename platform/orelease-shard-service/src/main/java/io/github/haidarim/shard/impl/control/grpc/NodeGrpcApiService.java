package io.github.haidarim.shard.impl.control.grpc;

import io.github.haidarim.shard.generated.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * NodeGrpcApiService
 */
@GrpcService
@RequiredArgsConstructor
public class NodeGrpcApiService extends ShardNodeApiServiceGrpc.ShardNodeApiServiceImplBase {

    @Override
    public void getNodeById(GetNodeByIdRequest request, StreamObserver<GetNodeResponse> responseStreamObserver){}

    @Override
    public void getNodeByDetails(GetNodeByDetailsRequest request, StreamObserver<GetNodeResponse> responseStreamObserver){}

    @Override
    public void getAllNodesForShard(GetAllNodesForShardRequest request, StreamObserver<GetAllNodesResponse> responseStreamObserver){}

    @Override
    public void getAllNodes(GetAllNodesRequest request, StreamObserver<GetAllNodesResponse> responseStreamObserver){}

    @Override
    public void createNode(CreateNodeRequest request, StreamObserver<CreateNodeResponse> responseStreamObserver){}

    @Override
    public void updateNode(UpdateNodeRequest request, StreamObserver<UpdateNodeResponse> responseStreamObserver){}

    @Override
    public void deleteNode(DeleteNodeRequest request, StreamObserver<DeleteNodeResponse> responseStreamObserver){}
}
