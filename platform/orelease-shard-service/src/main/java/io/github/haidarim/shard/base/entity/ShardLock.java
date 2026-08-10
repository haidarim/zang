package io.github.haidarim.shard.base.entity;


import io.github.haidarim.shard.api.common.type.LockReason;
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
@EqualsAndHashCode(callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class ShardLock extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "SHARD_ID")
    @ToString.Include
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
            name = "LOCKED_AT",
            nullable = false,
            updatable = false
    )
    @ToString.Include
    private Instant lockedAt;

    @Column(
            name = "OWNER_KEY", nullable = false, unique = true
    )
    private String ownerKey;

    @Column(
            name = "CREATED_BY", nullable = false
    )
    @ToString.Include
    private String createdBy;

    @Column(
            name = "LOCK_REASON", nullable = false
    )
    @ToString.Include
    private LockReason lockReason;

    @Column(name = "EXPIRES_AT", nullable = false)
    @ToString.Include
    private Instant expiresAt;
}
