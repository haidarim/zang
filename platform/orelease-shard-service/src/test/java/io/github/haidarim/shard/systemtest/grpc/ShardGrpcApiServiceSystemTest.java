package io.github.haidarim.shard.systemtest.grpc;


import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.exception.ShardNotFoundException;
import io.github.haidarim.shard.generated.grpc.*;
import io.github.haidarim.shard.integrationtest.common.AbstractShardTest;
import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.VIRTUAL_SHARD_SIZE;
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

    @Autowired
    VirtualShardService virtualShardService;

    @BeforeEach
    public void before(){
        shardMapRepository.deleteAll();
        virtualShardRepository.deleteAll();

        stub = ShardMapApiServiceGrpc.newBlockingStub(channel);

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
        assertEquals(getShardNames().size(), responses.size());

        List<VirtualShardMap> virtualShards =  virtualShardRepository.findAll();
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShards.size());
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
        assertNotNull(response2);

         Map<String, GetShardResponse> responses =  stub.getShardsForDatabase(
                 GetShardsForDatabaseRequest.newBuilder()
                        .setDatabaseName(TEST_DATABASE_NAME_B)
                        .setDomain(ShardDomain.CHAT)
                        .build()
         ).getShardsList().stream().collect(Collectors.toMap(GetShardResponse::getShardName, r->r));

         assertEquals(2, responses.size());
         assertTrue(responses.containsKey(TEST_SHARD_NAME_F));
         assertTrue(responses.containsKey(TEST_SHARD_NAME_G));
         responses.forEach((s, r) -> assertEquals(TEST_DATABASE_NAME_B, r.getDatabaseName()));
    }

    @Test
    public void createShard(){
        CreateShardResponse response = stub.createShard(
                CreateShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_F)
                        .setDatabaseName(TEST_DATABASE_NAME_B)
                        .setDomain(ShardDomain.CHAT)
                        .setStatus(ShardStatus.ACTIVE)
                        .build()
        );
        assertNotNull(response);
        ShardMap shard = shardMapRepository.findByShardName(TEST_SHARD_NAME_F).orElseThrow(() -> new AssertionError("No shard found with this shardName"));
        assertEquals(TEST_SHARD_NAME_F, shard.getShardName());
        assertEquals(TEST_DATABASE_NAME_B, shard.getDatabaseName());
        assertEquals(io.github.haidarim.shard.api.common.type.ShardDomain.CHAT, shard.getDomain());
        assertEquals(io.github.haidarim.shard.api.common.type.ShardStatus.ACTIVE, shard.getStatus());

        assertNotEquals(0, virtualShardRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardRepository.findAllById_DomainOrderById_VirtualShardId(shard.getDomain()).size());
    }

    @Test
    public void updateShard(){
        UpdateShardResponse response = stub.updateShard(
                UpdateShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_B)
                        .setDatabaseName(TEST_DATABASE_NAME_A)
                        .setStatus(ShardStatus.INACTIVE)
                        .setExpectedVersion(0L)
                        .build()
        );

        ShardMap shard = shardMapRepository.findByShardName(TEST_SHARD_NAME_B).orElseThrow(() -> new AssertionError("No shard found with this shardName"));
        assertNotNull(shard);

        assertEquals(response.getShardId(), shard.getShardId());
        assertEquals(TEST_SHARD_NAME_B, response.getShardName());
        assertEquals(TEST_SHARD_NAME_B, shard.getShardName());
        assertEquals(1L, shard.getVersion());
        assertEquals(1L, response.getVersion());

        assertEquals(0, virtualShardRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardRepository.findAllById_Domain(shard.getDomain()).size());
    }

    @Test
    public void deleteShard(){
        assertThrows(
                StatusRuntimeException.class,
                () -> stub.deleteShard(
                        DeleteShardRequest.newBuilder()
                                .setShardName(TEST_SHARD_NAME_C)
                                .build()
                )
        );
        ShardMap shard = shardMapRepository.findByShardName(TEST_SHARD_NAME_C).orElseThrow(() -> new ShardNotFoundException("No shard found for given shard name"));

        virtualShardService.rebalanceBeforeShardDeletion(shard);

        DeleteShardResponse response = stub.deleteShard(
                DeleteShardRequest.newBuilder()
                        .setShardName(TEST_SHARD_NAME_C)
                        .build()
        );
        assertTrue(shardMapRepository.findById(shard.getShardId()).isEmpty());
        assertTrue(shardMapRepository.findByShardName(TEST_SHARD_NAME_C).isEmpty());
        assertEquals(0, virtualShardRepository.findAllActiveVirtualIdsByShardId(response.getShardId()).size());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardRepository.findAllById_Domain(io.github.haidarim.shard.api.common.type.ShardDomain.CHAT).size());

        assertThrows(
                StatusRuntimeException.class,
                () -> stub.getShard(
                        GetShardRequest.newBuilder()
                                .setShardName(TEST_SHARD_NAME_C)
                                .build()
                )
        );
    }

    private @NotNull List<String> getShardNames() {
        return List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B, TEST_SHARD_NAME_C,
                TEST_SHARD_NAME_D, TEST_SHARD_NAME_E);
    }
}
