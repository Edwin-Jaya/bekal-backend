package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.edwin.bekal.common.base.BaseEntity;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.enums.ApprovalResult;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRX_LOAN_APPROVALS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class LoanApproval extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplicationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approved_by", nullable = false)
    private InternalUser approvedBy;

    @Size(max = 20)
    @NotNull
    @Column(name = "result", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalResult result;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Nationalized
    @Lob
    @Column(name = "notes")
    private String notes;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "approved_at", nullable = false)
    private Instant approvedAt;


}