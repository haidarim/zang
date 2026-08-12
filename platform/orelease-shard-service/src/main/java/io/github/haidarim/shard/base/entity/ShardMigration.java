package io.github.haidarim.shard.base.entity;

import io.github.haidarim.shard.api.common.type.MigrationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.github.haidarim.shard.api.common.type.MigrationStatus.COMPLETED;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "SHARD_MIGRATION"
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class ShardMigration extends BaseEntity {

    @Id
    @Column(name = "MIGRATION_ID")
    @GeneratedValue
    @EqualsAndHashCode.Include
    @ToString.Include
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
            name = "STATUS",
            nullable = false
    )
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private MigrationStatus status = MigrationStatus.STARTED;

    @Column(
            name = "PROGRESS_PERCENT",
            nullable = false
    )
    @ToString.Include
    private Integer processPercent = 0;

    @Column(
            name = "ERROR_MESSAGE"
    )
    @ToString.Include
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

    @Version
    @Column(name = "VERSION", nullable = false)
    @ToString.Include
    private Long version;

    @OneToMany(
            mappedBy = "migration",
            orphanRemoval = true
    )
    private List<ShardLock> locks = new ArrayList<>();

    public void validate(){
        if (fromShardMap.getShardId().equals(toShardMap.getShardId())) {
            throw new IllegalArgumentException("FROM_SHARD and TO_SHARD must be different");
        }
    }

    public boolean isCompleted(){
        return COMPLETED.equals(status);
    }
}
