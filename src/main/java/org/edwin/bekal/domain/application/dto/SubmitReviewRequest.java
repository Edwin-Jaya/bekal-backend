package org.edwin.bekal.domain.application.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Internal User ID is required")
    private UUID internalUserId;
    
    private BigDecimal verifiedIncome;

    @NotNull(message = "Review result is required")
    @Size(max = 20, message = "Result max length is 20 characters")
    private String result;

    private String notes;
}
