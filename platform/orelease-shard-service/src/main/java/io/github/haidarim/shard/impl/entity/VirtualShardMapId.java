package io.github.haidarim.shard.impl.entity;

import io.github.haidarim.shard.api.type.ShardDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VirtualShardMapId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "DOMAIN", nullable = false)
    private ShardDomain domain;

    @Column(name = "VIRTUAL_SHARD_ID", nullable = false)
    private Integer virtualShardId;
}
