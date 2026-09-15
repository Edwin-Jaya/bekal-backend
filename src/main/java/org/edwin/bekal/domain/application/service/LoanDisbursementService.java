package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanDisbursementService {
    LoanReviewDetail getDetailDisbursement(UUID loanApplicationId);
    LoanDisbursementResponse submitDisbursement(SubmitDisbursementRequest request, UUID reviewerUserId);
    Page<LoanDisbursementHistoryResponse> getApplicationDisbursementHistory(int page, int size);
}
