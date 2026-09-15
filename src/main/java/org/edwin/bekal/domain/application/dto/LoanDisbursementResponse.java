package org.edwin.bekal.domain.application.dto;

import lombok.Builder;
import lombok.Data;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.BankAccount;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LoanDisbursementResponse {
    private UUID id;
    private LoanApplication loanApplicationId;
    private InternalUser disbursedBy;
    private BigDecimal disbursementAmount;
    private String referenceNumber;
    private BankAccount customerBankId;
    private String status;
    private Instant disbursedAt;

}
