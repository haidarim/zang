package io.github.haidarim.shard.integrationtest.shard;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.integrationtest.common.AbstractIntegrationTest;
import org.aspectj.lang.annotation.After;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.github.haidarim.shard.common.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO happy path
//  edge cases,
//  validations inputs,
//  outputs,
//  impact on caches,

public class ShardIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ShardService shardService;

    @Autowired
    private ShardMapRepository repository;

    @Autowired
    private VirtualShardMapRepository virtualShardMapRepository;

    @BeforeEach
    public void before(){
        List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B, TEST_SHARD_NAME_C,
                        TEST_SHARD_NAME_D, TEST_SHARD_NAME_E)
                .forEach(shardName -> {
                    ShardMap shard = shardService.createShard(
                            shardName,
                            TEST_DATABASE_NAME_A,
                            ShardDomain.CHAT,
                            ShardStatus.ACTIVE
                    );
                    assertNotNull(shard);
                });

        ShardMap shard = shardService.createShard(
                TEST_SHARD_NAME_F,
                TEST_DATABASE_NAME_B,
                ShardDomain.CHAT,
                ShardStatus.ACTIVE
        );

        assertNotNull(shard);
    }

    @Test
    public void getAllShardsTest(){
        List<ShardMap> shards = shardService.getAllShards();
        assertFalse(shards.isEmpty());
        assertEquals(6, shards.size());
    }


    @Test
    public void getShardTest(){
        ShardMap shard = shardService.getShard(TEST_SHARD_NAME_D);
        assertNotNull(shard);
        assertEquals(TEST_DATABASE_NAME_A, shard.getDatabaseName());
        assertEquals(ShardDomain.CHAT, shard.getDomain());
        assertEquals(ShardStatus.ACTIVE, shard.getStatus());
    }

    @Test
    public void getShardsForDatabaseTest(){
        List<ShardMap> shards = shardService.getShardsForDatabase(TEST_DATABASE_NAME_A, ShardDomain.CHAT);

        assertFalse(shards.isEmpty());
        assertEquals(5, shards.size());

        shards = shardService.getShardsForDatabase(TEST_DATABASE_NAME_B, ShardDomain.CHAT);
        assertFalse(shards.isEmpty());
        assertEquals(1, shards.size());
        assertEquals(TEST_SHARD_NAME_F, shards.get(0).getShardName());
    }

    @Test
    public void createShardTest(){
        ShardMap shardToStore = shardService.createShard(
                TEST_SHARD_NAME_G,
                TEST_DATABASE_NAME_A,
                ShardDomain.CHAT,
                ShardStatus.ACTIVE
        );

        assertNotNull(shardToStore);

        ShardMap storedShard = repository.findByShardName(TEST_SHARD_NAME_G).orElseThrow(
                () -> new AssertionFailure("No shard found with this shard name")
        );
        assertNotNull(storedShard);

        assertEquals(shardToStore, storedShard);
    }

    @Test
    public void updateSharTest(){
        shardService.updateShard(TEST_SHARD_NAME_A, TEST_DATABASE_NAME_B, ShardStatus.INACTIVE, 0L);
        ShardMap shard = repository.findByShardName(TEST_SHARD_NAME_A).orElseThrow(()-> new AssertionFailure("Failed to find shard with this shardName"));

        assertEquals(1L, shard.getVersion());
        assertEquals(TEST_DATABASE_NAME_B, shard.getDatabaseName());
    }

    @Test
    public void deleteShardTest(){
        shardService.deleteShard(TEST_SHARD_NAME_B);

    }

    @AfterEach
    public void after(){
        virtualShardMapRepository.deleteAll();
        repository.deleteAll();
    }
}
