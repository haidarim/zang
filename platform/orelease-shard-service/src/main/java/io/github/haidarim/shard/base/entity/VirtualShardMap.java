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
@EqualsAndHashCode
public class VirtualShardMap {
    // Combo primary key
    @EmbeddedId
    private VirtualShardMapId id;

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @Column(name = "SHARD_ID", nullable = false)
    private ShardMap virtualShardMap;

    @Column(name = "VERSION", nullable = false)
    private Long version;
}
