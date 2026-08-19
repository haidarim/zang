package io.github.haidarim.shard.integrationtest.service;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import io.github.haidarim.shard.api.control.service.ShardService;
import io.github.haidarim.shard.api.control.service.VirtualShardService;
import io.github.haidarim.shard.base.entity.ShardMap;
import io.github.haidarim.shard.base.entity.VirtualShardMap;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.integrationtest.common.AbstractShardTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.github.haidarim.shard.api.common.constants.ShardConstants.VIRTUAL_SHARD_SIZE;
import static io.github.haidarim.shard.common.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * VirtualShardIntegrationTest
 */
public class VirtualShardIntegrationTest extends AbstractShardTest {

    @Autowired
    ShardService shardService;

    @Autowired
    VirtualShardService virtualShardService;

    @Autowired
    VirtualShardMapRepository virtualShardMapRepository;

    @Autowired
    ShardMapRepository shardMapRepository;

    @Override
    public void preTest(){
        List<String> shardNames = getShardNames();
        shardNames.forEach(shardName -> {
            ShardMap shard = shardService.createShard(
                    shardName,
                    TEST_DATABASE_NAME_A,
                    ShardDomain.CHAT,
                    ShardStatus.ACTIVE
            );
            assertNotNull(shard);
        });

        int totalVirtualSize = 0;
        for (String shardName : shardNames) {
            ShardMap shard = shardService.getShard(shardName);
            totalVirtualSize += virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size();
            assertNotEquals(0, totalVirtualSize);
        }

        assertEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(ShardDomain.CHAT).size());
    }

    private @NotNull List<String> getShardNames() {
        return List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B, TEST_SHARD_NAME_C,
                        TEST_SHARD_NAME_D, TEST_SHARD_NAME_E);
    }

    @Test
    public void virtualShardsInitializationTest(){
        ShardMap shard = shardService.createShard(TEST_SHARD_NAME_F, TEST_DATABASE_NAME_A, ShardDomain.CLIENT, ShardStatus.ACTIVE);

        assertNotNull(shard);
        List<VirtualShardMap> virtualShardMaps =  virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard.getShardId());

        assertFalse(virtualShardMaps.isEmpty());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMaps.size());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_Domain(ShardDomain.CLIENT).size());
    }

    @Test
    public void virtualShardsRebalancingTest(){
        ShardMap shard1 = shardService.createShard(TEST_SHARD_NAME_F, TEST_DATABASE_NAME_A, ShardDomain.CLIENT, ShardStatus.ACTIVE);
        assertNotNull(shard1);

        List<VirtualShardMap> virtualShardMaps =  virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard1.getShardId());
        assertFalse(virtualShardMaps.isEmpty());
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMaps.size());

        ShardMap shard2 = shardService.createShard(TEST_SHARD_NAME_G, TEST_DATABASE_NAME_A, ShardDomain.CLIENT, ShardStatus.ACTIVE);
        assertNotNull(shard2);

        int totalVirtualSize = virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard1.getShardId()).size();
        assertNotEquals(0, totalVirtualSize);
        assertNotEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        totalVirtualSize += virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard2.getShardId()).size();

        assertEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_Domain(ShardDomain.CLIENT).size());
    }


    @Test
    public void virtualShardRebalancingWhenDeletingShardTest(){
        ShardMap shardToDelete = shardService.getShard(TEST_SHARD_NAME_C);
        virtualShardService.rebalanceBeforeShardDeletion(shardToDelete);
        Integer shardId = shardService.deleteShard(TEST_SHARD_NAME_C);
        assertNotNull(shardId);
        assertEquals(shardToDelete.getShardId(), shardId);
        assertTrue(shardMapRepository.findByShardName(TEST_SHARD_NAME_C).isEmpty());

        List<VirtualShardMap> virtualShardMaps =  virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shardId);
        assertTrue(virtualShardMaps.isEmpty());

        List<String> shardNames =  List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B,
                TEST_SHARD_NAME_D, TEST_SHARD_NAME_E);

        int totalVirtualSize = 0;
        for (String shardName : shardNames) {
            ShardMap shard = shardService.getShard(shardName);
            totalVirtualSize += virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size();
            assertNotEquals(0, totalVirtualSize);
        }

        assertEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(ShardDomain.CHAT).size());


        // Deleting last shard for domain
        deleteShards(shardNames);
        assertEquals(0, virtualShardMapRepository.findAllById_Domain(ShardDomain.CHAT).size());
    }

    @Test
    public void virtualShardRebalancingWhenUpdatingShardTest(){
        ShardMap shardToUpdate = shardService.updateShard(TEST_SHARD_NAME_C, TEST_DATABASE_NAME_A, ShardStatus.INACTIVE, 0L);
        assertEquals(ShardStatus.INACTIVE, shardToUpdate.getStatus());

        List<VirtualShardMap> virtualShardMaps =  virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shardToUpdate.getShardId());
        assertTrue(virtualShardMaps.isEmpty());

        List<String> shardNames =  List.of(TEST_SHARD_NAME_A, TEST_SHARD_NAME_B,
                TEST_SHARD_NAME_D, TEST_SHARD_NAME_E);

        int totalVirtualSize = 0;
        for (String shardName : shardNames) {
            ShardMap shard = shardService.getShard(shardName);
            totalVirtualSize += virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size();
            assertNotEquals(0, totalVirtualSize);
        }

        assertEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(ShardDomain.CHAT).size());

        shardToUpdate = shardService.updateShard(TEST_SHARD_NAME_C, TEST_DATABASE_NAME_A, ShardStatus.ACTIVE, 1L);
        assertEquals(ShardStatus.ACTIVE, shardToUpdate.getStatus());
        virtualShardMaps =  virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shardToUpdate.getShardId());
        assertFalse(virtualShardMaps.isEmpty());

        totalVirtualSize = 0;
        for (String shardName : shardNames) {
            ShardMap shard = shardService.getShard(shardName);
            totalVirtualSize += virtualShardMapRepository.findAllActiveVirtualIdsByShardId(shard.getShardId()).size();
            assertNotEquals(0, totalVirtualSize);
        }
        totalVirtualSize += virtualShardMaps.size();
        assertEquals(VIRTUAL_SHARD_SIZE, totalVirtualSize);
        assertEquals(VIRTUAL_SHARD_SIZE, virtualShardMapRepository.findAllById_DomainOrderById_VirtualShardId(ShardDomain.CHAT).size());

        getShardNames().forEach(shardName -> {
            Long shardVersion = TEST_SHARD_NAME_C.equals(shardName) ? 2L : 0L;
            shardService.updateShard(shardName, TEST_DATABASE_NAME_A, ShardStatus.INACTIVE, shardVersion);
        });

        assertEquals(0, virtualShardMapRepository.findAllById_Domain(ShardDomain.CHAT).size());
    }

    private void deleteShards(List<String> shardNames){
        for (String shardName : shardNames){
            ShardMap shard = shardService.getShard(shardName);
            virtualShardService.rebalanceBeforeShardDeletion(shard);
            shardService.deleteShard(shardName);
        }
    }
}
