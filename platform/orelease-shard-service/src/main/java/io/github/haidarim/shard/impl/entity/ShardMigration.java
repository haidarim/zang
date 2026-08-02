package io.github.haidarim.shard.impl.entity;

import io.github.haidarim.shard.api.type.MigrationStatus;
import io.github.haidarim.shard.api.type.ShardDomain;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "SHARD_MIGRATION"
)
@EqualsAndHashCode
public class ShardMigration {

    @Id
    @Column(name = "MIGRATION_ID")
    @GeneratedValue
    private Long migrationId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "FROM_SHARD",
            nullable = false
    )
    private ShardMap fromShardMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TO_SHARD",
            nullable = false
    )
    private ShardMap toShardMap;

    @Column(
            name = "DOMAIN",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private ShardDomain domain;

    @Column(
            name = "STATUS",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    private MigrationStatus status = MigrationStatus.STARTED;

    @Column(
            name = "PROGRESS_PERCENT",
            nullable = false
    )
    private Integer processPercent = 0;

    @Column(
            name = "ERROR_MESSAGE",
            nullable = false
    )
    private String errorMessage;

    @Column(
            name = "STARTED_AT",
            nullable = false
    )
    private Instant startedAt = Instant.now();

    @Column(
            name = "COMPLETED_AT"
    )
    private Instant completedAt;

    @Column(
            name = "CREATED_AT",
            nullable = false
    )
    private Instant createdAt = Instant.now();

    @Column(
            name = "UPDATED_AT",
            nullable = false
    )
    private Instant updatedAt = Instant.now();

    @OneToMany(
            mappedBy = "migration",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShardLock> locks = new ArrayList<>();

    public void validate(){
        if (fromShardMap.getShardId().equals(toShardMap.getShardId())) {
            throw new IllegalArgumentException("FROM_SHARD and TO_SHARD must be different");
        }
    }
}
