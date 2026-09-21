package org.edwin.bekal.domain.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class LoanBalanceResponse {
    private UUID loanId;
    private String applicationNumber;
    private BigDecimal totalRepayment;
    private BigDecimal totalPaidSoFar;
    private BigDecimal remainingBalance;
    private BigDecimal monthlyInstallment;
    private Integer tenorMonths;
    private String status;
    @JsonProperty("isFullyPaid")
    private boolean isFullyPaid;
}
