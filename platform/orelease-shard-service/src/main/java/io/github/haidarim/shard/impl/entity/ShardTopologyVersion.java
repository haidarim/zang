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
        name = "SHARD_TOPOLOGY_VERSION"
)
@EqualsAndHashCode
public class ShardTopologyVersion {

    @Id
    @GeneratedValue
    @Column(name = "TOPOLOGY_ID")
    private Integer topologyId;

    @Column(name = "VERSION", nullable = false)
    private Double version;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt = Instant.now();
}
