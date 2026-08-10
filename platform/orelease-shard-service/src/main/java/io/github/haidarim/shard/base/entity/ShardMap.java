package io.github.haidarim.shard.base.entity;

import io.github.haidarim.shard.api.common.type.ShardDomain;
import io.github.haidarim.shard.api.common.type.ShardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
        uniqueConstraints ={
                @UniqueConstraint(
                        name = "shard_name_uk", columnNames = {"SHARD_NAME"}
                )
        }
)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShardMap {

    @Id
    @GeneratedValue
    @Column(name = "SHARD_ID")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer shardId;

    @Column(name = "SHARD_NAME", nullable = false)
    @ToString.Include
    private String shardName;

    @Column(name = "DATABASE_NAME", nullable = false)
    @ToString.Include
    private String databaseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "DOMAIN", nullable = false)
    @ToString.Include
    private ShardDomain domain;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private ShardStatus status = ShardStatus.ACTIVE;

    @Version
    @Column(name = "VERSION", nullable = false)
    @ToString.Include
    private Long version;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @ToString.Include
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT", nullable = false)
    @ToString.Include
    private Instant updatedAt;

    // ShardMap referenced by ShardNode
    @OneToMany(
            mappedBy = "nodeShardMap",
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


    public ShardMap(String shardName, String databaseName, ShardDomain domain, ShardStatus status){
        this.setShardName(shardName);
        this.setDatabaseName(databaseName);
        this.setDomain(domain);
        this.setStatus(status);
    }
}
