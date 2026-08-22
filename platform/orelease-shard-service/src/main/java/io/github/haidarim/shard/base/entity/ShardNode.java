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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "SHARD_NODE",
        uniqueConstraints = {
                @UniqueConstraint(name = "shard_map_id_host_port_uk", columnNames = {"SHARD_MAP_ID", "HOST_NAME", "PORT"})
        }
)
@ToString(onlyExplicitlyIncluded = true)
public class ShardNode extends BaseEntity{
    @Id
    @GeneratedValue
    @Column(name = "NODE_ID")
    @EqualsAndHashCode.Include
    @ToString.Include
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

    @Column(name = "REGION", nullable = false)
    private String region;

    @Column(name = "NODE_ROLE", nullable = false)
    @Enumerated(EnumType.STRING)
    private NodeRole nodeRole = NodeRole.PRIMARY;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "CONNECTION_SECRET", nullable = false)
    private String connectionSecret;

    @Column(name = "MAX_CONNECTIONS")
    private Integer maxConnections = 100;

    @Column(name = "WEIGHT")
    private Integer weight = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "NODE_STATUS", nullable = false)
    private NodeStatus nodeStatus = NodeStatus.ONLINE;

    @Version
    @Column(name = "VERSION", nullable = false)
    @ToString.Include
    private Long version;

    public ShardNode(ShardMap shard, String hostName,
                     Integer port, String region, NodeRole role,
                     String username, String connectionSecret,
                     Integer maxConnection, Integer weight, NodeStatus status){
        this.nodeShardMap = shard;
        this.hostName = hostName;
        this.port = port;
        this.region = region;
        this.nodeRole = role;
        this.username = username;
        this.connectionSecret = connectionSecret;
        this.maxConnections = maxConnection;
        this.weight = weight;
        this.nodeStatus = status;
    }
}
