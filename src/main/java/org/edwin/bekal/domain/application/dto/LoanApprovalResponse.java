package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LoanApprovalResponse implements Serializable {
    UUID id;
    @NotNull
    LoanApplication loanApplicationId;
    @NotNull
    InternalUser approvedBy;
    @NotNull
    @Size(max = 20)
    String result;
    BigDecimal approvedAmount;
    String notes;
    @NotNull
    Instant approvedAt;
}