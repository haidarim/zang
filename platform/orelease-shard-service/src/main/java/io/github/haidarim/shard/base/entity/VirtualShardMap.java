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
@EqualsAndHashCode(callSuper = false)
public class VirtualShardMap extends BaseEntity{
    // Combo primary key
    @EmbeddedId
    private VirtualShardMapId id;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "SHARD_ID", nullable = false)
    private ShardMap physicalShardMap;
}
