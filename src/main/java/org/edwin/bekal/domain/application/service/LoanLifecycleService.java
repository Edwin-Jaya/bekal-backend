package org.edwin.bekal.domain.application.service;

import java.util.UUID;

public interface LoanLifecycleService {
    void processFinalRepayment(UUID customerId, UUID loanApplicationId);
}
