package io.github.haidarim.shard.base.entity;


import io.github.haidarim.shard.api.common.type.LockReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Columns;

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

    @Column(
            name = "OWNER_KEY", nullable = false, unique = true
    )
    private String ownerKey;

    @Column(
            name = "CREATED_BY", nullable = false
    )
    private String createdBy;

    @Column(
            name = "LOCK_REASON", nullable = false
    )
    private LockReason lockReason;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;
}
