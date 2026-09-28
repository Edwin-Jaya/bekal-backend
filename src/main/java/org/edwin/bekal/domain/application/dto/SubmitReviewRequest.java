package org.edwin.bekal.domain.application.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message = "Loan Application ID is required")
    private UUID loanApplicationId;

    private UUID internalUserId;
    
    private BigDecimal verifiedIncome;

    @NotNull(message = "Review result is required")
    private String result;

    private String notes;
}
