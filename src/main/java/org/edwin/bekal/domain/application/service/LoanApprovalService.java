package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanApprovalService {
//    Page<LoanApprovalResponse> getPendingLoanApplicationApproval(int page, int size);
    LoanReviewDetail getDetailApproval(UUID loanApplicationId);
    LoanApprovalResponse submitApproval(SubmitReviewRequest request, UUID reviewerUserId);
    Page<LoanHistoryResponse> getApplicationApprovalHistory(int page, int size);
}
