package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDisbursementRequest {
    private UUID loanApplicationId;
    private UUID customerBankId;
    private UUID disbursedBy;
    private BigDecimal disbursementAmount;
    private String status;
    private String referenceNumber;
}
