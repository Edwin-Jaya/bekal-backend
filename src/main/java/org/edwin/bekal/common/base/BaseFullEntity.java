package org.edwin.bekal.common.base;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public abstract class BaseFullEntity extends BaseAuditEntity {
    @Column(name="deleted_at")
    private Instant deletedAt;
}
