package io.github.haidarim.shard.impl.entity;

import io.github.haidarim.shard.api.type.NodeRole;
import io.github.haidarim.shard.api.type.NodeStatus;
import io.github.haidarim.shard.api.type.ShardDomain;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Table(
        name = "SHARD_NODE",
        uniqueConstraints = {
                @UniqueConstraint(name = "shard_map_id_host_port_uk", columnNames = {"SHARD_MAP_ID", "HOST_NAME", "PORT"})
        }
)
public class ShardNode {
    @Id
    @GeneratedValue
    @Column(name = "NODE_ID")
    private Long nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SHARD_MAP_ID",
            nullable = false
    )
    private ShardMap shardMapId;

    @Column(name = "HOST_NAME", nullable = false)
    private String hostName;

    @Column(name = "PORT", nullable = false)
    private Integer port = 5432;

    @Column(name = "REGION")
    private String region;

    @Column(name = "DOMAIN", nullable = false)
    @Enumerated(EnumType.STRING)
    private ShardDomain domain;

    @Column(name = "NODE_ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private NodeRole nodeRole = NodeRole.PRIMARY;


    @Enumerated(EnumType.STRING)
    @Column(name = "NODE_STATUS", nullable = false)
    private NodeStatus nodeStatus = NodeStatus.ONLINE;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt = Instant.now();
}
