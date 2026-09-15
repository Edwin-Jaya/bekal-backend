package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.edwin.bekal.common.base.BaseEntity;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.enums.ReviewResult;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRX_LOAN_REVIEWS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class LoanReview extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplicationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_by", nullable = false)
    private InternalUser reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private ReviewResult result;

    @Nationalized
    @Lob
    @Column(name = "notes")
    private String notes;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;


}