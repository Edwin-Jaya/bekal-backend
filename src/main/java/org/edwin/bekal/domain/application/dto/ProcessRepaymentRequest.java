package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProcessRepaymentRequest {

    @NotNull(message = "Customer ID wajib diisi")
    private UUID customerId;

    @NotNull(message = "Loan Application ID wajib diisi")
    private UUID loanApplicationId;

    @NotNull(message = "Jumlah pelunasan wajib diisi")
    private BigDecimal amountPaid;
}
