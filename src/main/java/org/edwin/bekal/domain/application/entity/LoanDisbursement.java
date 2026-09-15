package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.BankAccount;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.enums.DisburseResult;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TRX_LOAN_DISBURSEMENTS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class LoanDisbursement extends BaseAuditEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplicationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disbursed_by", nullable = false)
    private InternalUser disbursedBy;

    @NotNull
    @Column(name = "disbursement_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal disbursementAmount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_bank_account_id", nullable = false)
    private BankAccount customerBankId;

    @Size(max = 50)
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Size(max = 20)
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DisburseResult status;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;


}