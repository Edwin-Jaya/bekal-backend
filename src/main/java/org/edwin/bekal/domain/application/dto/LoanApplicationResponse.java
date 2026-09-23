package org.edwin.bekal.domain.application.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.InternalUserResponse;

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
    BranchResponse branch;

    @NotNull
    CustomerResponse customer;

    @NotNull
    PlafondResponse plafond;

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
    InternalUserResponse assignedMarketing;

    InternalUserResponse assignedBranchManager;

    @NotNull

    Instant submittedAt;

}