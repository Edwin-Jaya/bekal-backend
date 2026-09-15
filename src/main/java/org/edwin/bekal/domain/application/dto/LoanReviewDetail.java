package org.edwin.bekal.domain.application.dto;

import lombok.Builder;

import lombok.Getter;
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;

import java.util.List;

@Getter
@Builder
public class LoanReviewDetail {
    LoanApplicationResponse loanApplicationResponse;
    CustomerResponse customerResponse;
    EmploymentResponse employmentResponse;
    List<DocumentResponse> documentResponse;
    LoanReviewResponse loanReviewResponse;
    LoanApprovalResponse loanApprovalResponse;

}
