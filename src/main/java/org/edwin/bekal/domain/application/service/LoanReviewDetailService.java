package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.LoanHistoryResponse;
import org.edwin.bekal.domain.application.dto.LoanReviewDetail;
import org.edwin.bekal.domain.application.dto.LoanReviewResponse;
import org.edwin.bekal.domain.application.dto.SubmitReviewRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanReviewDetailService {
    LoanReviewDetail getDetail(UUID loanApplicationId);
    LoanReviewResponse submitReview(SubmitReviewRequest request, UUID reviewerUserId);
    Page<LoanHistoryResponse> getApplicationReviewHistory(int page, int size);
}
