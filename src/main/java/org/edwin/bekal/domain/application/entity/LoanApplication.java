package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "TRX_LOAN_APPLICATIONS", schema = "dbo")
public class LoanApplication extends BaseAuditEntity {
    @Size(max = 30)
    @NotNull
    @Column(name = "application_number", nullable = false, length = 30)
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plafond_id", nullable = false)
    private Plafond plafond;

    @NotNull
    @Column(name = "amount_requested", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountRequested;

    @NotNull
    @Column(name = "tenor_months", nullable = false)
    private Integer tenorMonths;

    @Size(max = 150)
    @Column(name = "purpose", length = 150)
    private String purpose;

    @NotNull
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotNull
    @Column(name = "monthly_installment", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @NotNull
    @Column(name = "total_repayment", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepayment;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'submitted'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_marketing_id")
    private InternalUser assignedMarketing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_branch_manager_id")
    private InternalUser assignedBranchManager;

    @NotNull
    @ColumnDefault("sysutcdatetime()")
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;


}