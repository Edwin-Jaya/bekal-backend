package org.edwin.bekal.domain.customer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mst_customer_documents", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class Document extends BaseFullEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Size(max = 30)
    @NotNull
    @Nationalized
    @Column(name = "document_type", nullable = false, length = 30)
    private String documentType;

    @Size(max = 500)
    @NotNull
    @Nationalized
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Size(max = 64)
    @Nationalized
    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @ColumnDefault("'pending'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private InternalUser verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Size(max = 255)
    @Nationalized
    @Column(name = "rejection_reason")
    private String rejectionReason;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}