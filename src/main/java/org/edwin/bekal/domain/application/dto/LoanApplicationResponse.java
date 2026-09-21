package org.edwin.bekal.domain.application.dto;



import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;
import org.edwin.bekal.domain.application.entity.Plafond;

import org.edwin.bekal.domain.customer.entity.Customer;

import org.edwin.bekal.domain.master.entity.Branch;

import org.edwin.bekal.domain.master.entity.InternalUser;



import java.io.Serializable;

import java.math.BigDecimal;

import java.time.Instant;

import java.util.UUID;





@Data
@NoArgsConstructor          // ✅ required for Redis deserialization
@AllArgsConstructor
@Builder
public class LoanApplicationResponse implements Serializable {

    UUID id;

    @NotNull

    @Size(max = 30)

    String applicationNumber;

    Branch branch;

    @NotNull

    Customer customer;

    @NotNull

    Plafond plafond;

    @NotNull

    BigDecimal amountRequested;

    @NotNull

    Integer tenorMonths;

    @Size(max = 150)

    String purpose;

    @NotNull

    BigDecimal interestRate;

    @NotNull

    BigDecimal monthlyInstallment;

    @NotNull

    BigDecimal totalRepayment;

    @NotNull

    @Size(max = 20)

    String status;

    InternalUser assignedMarketing;

    InternalUser assignedBranchManager;

    @NotNull

    Instant submittedAt;

}