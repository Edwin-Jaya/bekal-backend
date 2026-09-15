package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edwin.bekal.common.util.LoanReviewDetailMapper;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanDisbursement;
import org.edwin.bekal.domain.application.entity.LoanReview;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.LoanApprovalRepository;
import org.edwin.bekal.domain.application.repository.LoanDisbursementRepository;
import org.edwin.bekal.domain.application.repository.LoanReviewRepository;
import org.edwin.bekal.domain.application.service.LoanDisbursementService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.master.entity.BankAccount;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.BankAccountRepository;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.edwin.bekal.enums.DisburseResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanDisbursementServiceImpl implements LoanDisbursementService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final InternalUserRepository internalUserRepository;
    private final CustomerRepository customerRepository;
    private final EmploymentRepository employmentRepository;
    private final DocumentRepository documentRepository;
    private final LoanReviewRepository loanReviewRepository;
    private final LoanReviewDetailMapper mapper;
    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final BankAccountRepository bankAccountRepository;

    @Override
    @Transactional
    public LoanReviewDetail getDetailDisbursement(UUID loanApplicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new EntityNotFoundException("Loan application not found: " + loanApplicationId));

        UUID customerId = loanApplication.getCustomer().getId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

        Employment employment = employmentRepository
                .findByCustomer_IdAndCustomerIsCurrentTrue(customerId)
                .orElse(null);

        List<Document> documents = documentRepository
                .findByCustomerIdAndIsLatestTrue(customerId);

        LoanReview latestReview = loanReviewRepository
                .findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId)
                .orElse(null);

        LoanApproval latestApproval = loanApprovalRepository
                .findTopByLoanApplicationId_IdOrderByApprovedAtDesc(loanApplicationId)
                .orElse(null);

        return LoanReviewDetail.builder()
                .loanApplicationResponse(mapper.toLoanApplicationResponse(loanApplication))
                .customerResponse(mapper.toCustomerResponse(customer))
                .employmentResponse(mapper.toEmploymentResponse(employment))
                .documentResponse(documents.stream().map(mapper::toDocumentResponse).toList())
                .loanReviewResponse(mapper.toLoanReviewResponse(latestReview))
                .loanApprovalResponse(mapper.toLoanApprovalResponse(latestApproval))
                .build();
    }

    @Override
    @Transactional
    public LoanDisbursementResponse submitDisbursement(SubmitDisbursementRequest request, UUID reviewerUserId) {
        // 1. Cari Loan Application
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Loan application not found with ID: " + request.getLoanApplicationId()));

        // 2. Cari Reviewer (InternalUser)
        InternalUser reviewer = internalUserRepository.findById(reviewerUserId)
                .orElseThrow(() -> new EntityNotFoundException("Internal user not found with ID: " + reviewerUserId));

        // 3. Ambil Bank Account milik Customer
        UUID customerId = loanApplication.getCustomer().getId();

// 2. Cari Bank Account milik customer dari DB
        BankAccount bankAccount = bankAccountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Bank account not found for customer: " + customerId));

        // 4. Safe parse Enum Status
        DisburseResult disburseStatus;
        try {
            disburseStatus = DisburseResult.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid disburse status: " + request.getStatus());
        }

        // 5. Build Entity (Sesuai variabel nama di class LoanDisbursement)
        LoanDisbursement loanDisbursement = LoanDisbursement.builder()
                .loanApplicationId(loanApplication)                          // Field: loanApplicationId
                .disbursedBy(reviewer)                                        // Field: disbursedBy
                .disbursementAmount(loanApplication.getAmountRequested())     // Field: disbursementAmount
                .customerBankId(bankAccount)                                  // Field: bankCustomerId (Object BankAccount)
                .referenceNumber(request.getReferenceNumber())                // Field: referenceNumber
                .status(disburseStatus)                                       // Field: status
                .disbursedAt(Instant.now())                                   // Field: disbursedAt
                .build();

        // 6. Save ke Database
        LoanDisbursement savedDisbursement = loanDisbursementRepository.save(loanDisbursement);

        // 7. Update status pada LoanApplication (Opsional)
        if (disburseStatus == DisburseResult.SUCCESS) {
            loanApplication.setStatus("DISBURSED");
            loanApplicationRepository.save(loanApplication);
        }
        if (disburseStatus == DisburseResult.FAILED) {
            loanApplication.setStatus("CANCELLED");
            loanApplicationRepository.save(loanApplication);
        }

        return mapper.toLoanDisbursementResponse(savedDisbursement);
    }

    @Override
    @Transactional
    public Page<LoanDisbursementHistoryResponse> getApplicationDisbursementHistory(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<LoanDisbursement> dibursementPage = loanDisbursementRepository.findDisbursementHistory(pageable);

        return dibursementPage.map(review -> {
            var app = review.getLoanApplicationId();

            return new LoanDisbursementHistoryResponse(
                    app.getId(),
                    app.getApplicationNumber(),
                    app.getCustomer().getCustomerFullName(),
                    app.getStatus(),
                    review.getDisbursedAt()
            );
        });
    }

}
