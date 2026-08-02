package io.github.haidarim.shard.impl.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "SHARD_LOCK"
)
@EqualsAndHashCode
public class ShardLock {

    @Id
    @GeneratedValue
    @Column(name = "SHARD_ID")
    private Integer shardId;

    // shard map 1 <--------------> 0, 1
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "SHARD_ID")
    private ShardMap shard;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MIGRATION_ID")
    private ShardMigration migration;


    @Column(
            name = "LOCKED_AT"
    )
    private Instant lockedAt = Instant.now();
}
