package io.github.haidarim.shard.impl.entity;

import io.github.haidarim.shard.api.type.ShardDomain;
import io.github.haidarim.shard.api.type.ShardStatus;
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
        name = "SHARD_MAP",
        uniqueConstraints = {
                @UniqueConstraint(name = "shard_name_uk", columnNames = {"SHARD_NAME"})
        }
)
@ToString(exclude = "connectionSecret")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShardMap {

    @Id
    @GeneratedValue
    @Column(name = "SHARD_ID")
    @EqualsAndHashCode.Include
    private Integer shardId;

    @Column(name = "SHARD_NAME", nullable = false)
    @EqualsAndHashCode.Include
    private String shardName;

    @Column(name = "DATABASE_NAME", nullable = false)
    @EqualsAndHashCode.Include
    private String databaseName;

    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    @Column(name = "DOMAIN", nullable = false)
    private ShardDomain domain;

    @Column(name = "CONNECTION_SECRET")
    private String connectionSecret;

    @Column(name = "MAX_CONNECTIONS")
    private Integer maxConnections = 100;

    @Column(name = "WEIGHT")
    private Integer weight = 100;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShardStatus status = ShardStatus.ACTIVE;

    @Column(name = "VERSION", nullable = false)
    private Double version = 1.0;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "UPDATED_AT", nullable = false)
    private Instant updatedAt = Instant.now();

    // ShardMap referenced by ShardNode
    @OneToMany(
            mappedBy = "shardMapId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShardNode> nodes = new ArrayList<>();

    @OneToMany(
            mappedBy = "fromShardMap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShardMigration> fromShardMigrations = new ArrayList<>();

    @OneToMany(
            mappedBy = "toShardMap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ShardMigration> toShardMigrations = new ArrayList<>();

    @OneToOne(
            mappedBy = "shard"
    )
    private ShardLock lock;

    @OneToMany(
            mappedBy = "virtualShardMap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<VirtualShardMap> virtualShardMaps = new ArrayList<>();
}
