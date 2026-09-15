package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.CreateLoanApplicationRequest;
import org.edwin.bekal.domain.application.dto.LoanApplicationResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanApplicationService {
    Page<LoanApplicationResponse> getLoanApplication(int page, int size);
    Page<LoanApplicationResponse> getPendingLoanApplication(int page, int size);
    Page<LoanApplicationResponse> getPendingLoanApplicationApproval(int page, int size);
    Page<LoanApplicationResponse> getPendingLoanApplicationDisbursement(int page, int size);
    LoanApplicationResponse createLoanApplication(CreateLoanApplicationRequest request);
    Page<LoanApplicationResponse> getLoanApplicationByCustomer(UUID customerId, int page, int size);
}
