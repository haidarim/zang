package io.github.haidarim.shard.base.entity;

import io.github.haidarim.shard.api.common.type.NodeRole;
import io.github.haidarim.shard.api.common.type.NodeStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
@Table(
        name = "SHARD_NODE",
        uniqueConstraints = {
                @UniqueConstraint(name = "shard_map_id_host_port_uk", columnNames = {"SHARD_MAP_ID", "HOST_NAME", "PORT"})
        }
)
public class ShardNode extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "NODE_ID")
    private Long nodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "SHARD_MAP_ID",
            nullable = false
    )
    private ShardMap nodeShardMap;

    @Column(name = "HOST_NAME", nullable = false)
    private String hostName;

    @Column(name = "PORT", nullable = false)
    private Integer port = 5432;

    @Column(name = "REGION")
    private String region;

    @Column(name = "NODE_ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private NodeRole nodeRole = NodeRole.PRIMARY;

    @Column(name = "CONNECTION_SECRET")
    private String connectionSecret;

    @Column(name = "MAX_CONNECTIONS")
    private Integer maxConnections = 100;

    @Column(name = "WEIGHT")
    private Integer weight = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "NODE_STATUS", nullable = false)
    private NodeStatus nodeStatus = NodeStatus.ONLINE;
}
