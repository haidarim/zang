package io.github.haidarim.shard.base.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "VIRTUAL_SHARD_MAP"
)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class VirtualShardMap extends BaseEntity{
    // Combo primary key
    @EmbeddedId
    @EqualsAndHashCode.Include
    @ToString.Include
    private VirtualShardMapId id;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "SHARD_ID", nullable = false)
    private ShardMap physicalShardMap;

    @Version
    @Column(name = "VERSION", nullable = false)
    @ToString.Include
    private Long version;

    public VirtualShardMap (VirtualShardMapId id, ShardMap physicalShardMap){
        this.id = id;
        this.physicalShardMap = physicalShardMap;
    }
}
