package io.github.haidarim.shard.systemtest.grpc;

import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.generated.grpc.*;
import io.github.haidarim.shard.integrationtest.common.AbstractShardTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.common.constants.TestConstants.*;
import static io.github.haidarim.shard.common.constants.TestConstants.TEST_SHARD_NAME_D;
import static io.github.haidarim.shard.common.constants.TestConstants.TEST_SHARD_NAME_E;
import static org.junit.jupiter.api.Assertions.*;

public class ShardGrpcApiServiceSystemTest extends AbstractShardTest {

    private ShardMapApiServiceGrpc.ShardMapApiServiceBlockingStub stub;

    @Autowired
    private ShardMapRepository shardMapRepository;

    @Autowired
    private VirtualShardMapRepository virtualShardRepository;

    @BeforeEach
    public void before(){
        shardMapRepository.deleteAll();
        virtualShardRepository.deleteAll();

        stub = ShardMapApiServiceGrpc.newBlockingStub(getChannel());

        Map<Integer, String> responses = getShardNames().stream()
                .map(shardName ->
                        CreateShardRequest.newBuilder()
                                .setShardName(shardName)
                                .setDatabaseName(TEST_DATABASE_NAME_A)
                                .setDomain(ShardDomain.CHAT)
                                .setStatus(ShardStatus.ACTIVE)
                                .build()
                )
                .map(request -> stub.createShard(request))
                .collect(Collectors.toMap(
                        CreateShardResponse::getShardId,
                        CreateShardResponse::getShardName
                ));
        assertEquals(5, responses.size());
    }

    @Test
    public void getAllShards(){
         Map<Integer, GetShardResponse> shardResponses = stub.getAllShards(GetAllShardsRequest.newBuilder().build())
                 .getShardsList()
                 .stream()
                 .collect(Collectors.toMap(
                    GetShardResponse::getShardId,
                    response -> response
                 ));

        List<ShardMap> shards = shardMapRepository.findAll();
        assertEquals(5, shards.size());

        assertEquals(shards.size(), shardResponses.size());
        shards.forEach(shard -> {
            GetShardResponse response = shardResponses.get(shard.getShardId());
            assertTrue(shardResponses.containsKey(shard.getShardId()));
            assertEquals(shard.getShardName(), response.getShardName());
            assertEquals(TEST_DATABASE_NAME_A, response.getDatabaseName());
            assertEquals(ShardDomain.CHAT, response.getDomain());
            assertEquals(ShardStatus.ACTIVE, response.getStatus());
        });
    }

    @Test
    public void getShard(){
        GetShardResponse response = stub.getShard(
                GetShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_D)
                        .build()
        );
        assertNotNull(response);

        ShardMap shard = shardMapRepository.findByShardName(TEST_SHARD_NAME_D).orElseThrow(() -> new AssertionError("No shard found with this shardName"));
        assertEquals(shard.getShardId(), response.getShardId());
        assertEquals(shard.getShardName(), response.getShardName());
        assertEquals(shard.getDatabaseName(), response.getDatabaseName());
        assertEquals(shard.getDomain().name(), response.getDomain().name());
        assertEquals(shard.getStatus().name(), response.getStatus().name());
    }

    @Test
    public void getShardsForDatabase(){
        CreateShardResponse response1 = stub.createShard(
                CreateShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_F)
                        .setDatabaseName(TEST_DATABASE_NAME_B)
                        .setDomain(ShardDomain.CHAT)
                        .setStatus(ShardStatus.ACTIVE)
                        .build()
        );
        assertNotNull(response1);
        CreateShardResponse response2 = stub.createShard(
                CreateShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_G)
                        .setDatabaseName(TEST_DATABASE_NAME_B)
                        .setDomain(ShardDomain.CHAT)
                        .setStatus(ShardStatus.ACTIVE)
                        .build()
        );
        assertNotNull(response1);

         Map<String, GetShardResponse> responses =  stub.getShardsForDatabase(
                 GetShardsForDatabaseRequest.newBuilder()
                        .setDatabaseName(TEST_DATABASE_NAME_B)
                        .setDomain(ShardDomain.CHAT)
                        .build()
         ).getShardsList().stream().collect(Collectors.toMap(r -> r.getShardName(), r->r));

         assertEquals(2, responses.size());
         assertTrue(responses.containsKey(TEST_SHARD_NAME_F));
         assertTrue(responses.containsKey(TEST_SHARD_NAME_G));
         responses.forEach((s, r) -> {
             assertEquals(TEST_DATABASE_NAME_B, responses.get(TEST_SHARD_NAME_F).getDatabaseName());
         });
    }

    @Test
    public void createShard(){

    }

    @Test
    public void updateShard(){

    }

    @Test
    public void deleteShard(){

    }

    private @NotNull List<String> getShardNames() {
        return List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B, TEST_SHARD_NAME_C,
                TEST_SHARD_NAME_D, TEST_SHARD_NAME_E);
    }
}
