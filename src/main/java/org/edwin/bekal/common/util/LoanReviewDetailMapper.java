package org.edwin.bekal.common.util;

import org.edwin.bekal.domain.application.dto.LoanApplicationResponse;
import org.edwin.bekal.domain.application.dto.LoanApprovalResponse;
import org.edwin.bekal.domain.application.dto.LoanDisbursementResponse;
import org.edwin.bekal.domain.application.dto.LoanReviewResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanDisbursement;
import org.edwin.bekal.domain.application.entity.LoanReview;
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.springframework.stereotype.Component;

@Component
public class LoanReviewDetailMapper {

    public CustomerResponse toCustomerResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .customerFullName(c.getCustomerFullName())
                .customerNik(c.getCustomerNik())
                .customerPhoneNumber(c.getCustomerPhoneNumber())
                .customerEmail(c.getCustomerEmail())
                .customerAddress(c.getCustomerAddress())
                .build();
    }

    public EmploymentResponse toEmploymentResponse(Employment e) {
        if (e == null) return null;
        return EmploymentResponse.builder()
                .customerEmploymentType(e.getCustomerEmploymentType())
                .customerCompanyName(e.getCustomerCompanyName())
                .customerJobTitle(e.getCustomerJobTitle())
                .customerDeclaredIncome(e.getCustomerDeclaredIncome())
                .customerVerifiedIncome(e.getCustomerVerifiedIncome())
                .build();
    }

    public DocumentResponse toDocumentResponse(Document d) {
        return DocumentResponse.builder()
                .id(d.getId())
                .documentType(d.getDocumentType())
                .fileUrl(d.getFileUrl())
                .status(d.getStatus())
                .isLatest(d.getIsLatest())
                .build();
    }

    public LoanApplicationResponse toLoanApplicationResponse(LoanApplication la) {
        return LoanApplicationResponse.builder()
                .id(la.getId())
                .applicationNumber(la.getApplicationNumber())
                .status(la.getStatus())
                .branch(la.getBranch())
                .amountRequested(la.getAmountRequested())
                .tenorMonths(la.getTenorMonths())
                .interestRate(la.getInterestRate())
                .monthlyInstallment(la.getMonthlyInstallment())
                .purpose(la.getPurpose())
                .submittedAt(la.getSubmittedAt())
                .build();
    }

    public LoanReviewResponse toLoanReviewResponse(LoanReview r) {
        if (r == null) return null;
        return LoanReviewResponse.builder()
                .id(r.getId())
                .loanApplicationId(r.getLoanApplicationId())
                .result(r.getResult() != null ? r.getResult().name() : null)
                .notes(r.getNotes())
                .reviewedBy(r.getReviewedBy())
                .reviewedAt(r.getReviewedAt())
                .build();
    }

    public LoanApprovalResponse toLoanApprovalResponse(LoanApproval r) {
        if (r == null) return null;
        return LoanApprovalResponse.builder()
                .id(r.getId())
                .loanApplicationId(r.getLoanApplicationId())
                .result(r.getResult() != null ? r.getResult().name() : null)
                .notes(r.getNotes())
                .approvedBy(r.getApprovedBy())
                .approvedAt(r.getApprovedAt())
                .build();
    }

    public LoanDisbursementResponse toLoanDisbursementResponse(LoanDisbursement r) {
        if (r == null) return null;
        return LoanDisbursementResponse.builder()
                .id(r.getId())
                .loanApplicationId(r.getLoanApplicationId())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .customerBankId(r.getCustomerBankId())
//                .(r.getBankCustomerId())
                .disbursedBy(r.getDisbursedBy())
                .disbursedAt(r.getDisbursedAt())
                .referenceNumber(r.getReferenceNumber())
                .build();
    }
}
