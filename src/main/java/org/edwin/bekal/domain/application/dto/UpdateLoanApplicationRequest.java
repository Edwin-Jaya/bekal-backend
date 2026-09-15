package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link org.edwin.bekal.domain.application.entity.LoanApplication}
 */
@Data
public class UpdateLoanApplicationRequest implements Serializable {
    @NotNull
    BigDecimal amountRequested;
    @NotNull
    Integer tenorMonths;
    @Size(max = 150)
    String purpose;
}