package org.edwin.bekal.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private UUID paymentId;
    private UUID loanId;
    private String applicationNumber;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime paymentDate;
    private String status;
}
