package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.util.LoanReviewDetailMapper;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanReview;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.LoanApprovalRepository;
import org.edwin.bekal.domain.application.repository.LoanReviewRepository;
import org.edwin.bekal.domain.application.service.LoanApprovalService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.edwin.bekal.enums.ApprovalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanApprovalServiceImpl implements LoanApprovalService{

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final EmploymentRepository employmentRepository;
    private final DocumentRepository documentRepository;
    private final LoanReviewRepository loanReviewRepository;
    private final InternalUserRepository internalUserRepository;
    private final LoanReviewDetailMapper mapper;
    private final LoanApprovalRepository loanApprovalRepository;

    @Override
    @Transactional
    public LoanReviewDetail getDetailApproval(UUID loanApplicationId) {
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

        return LoanReviewDetail.builder()
                .loanApplicationResponse(mapper.toLoanApplicationResponse(loanApplication))
                .customerResponse(mapper.toCustomerResponse(customer))
                .employmentResponse(mapper.toEmploymentResponse(employment))
                .documentResponse(documents.stream().map(mapper::toDocumentResponse).toList())
                .loanReviewResponse(mapper.toLoanReviewResponse(latestReview))
                .build();
    }

    @Override
    @Transactional
    public LoanApprovalResponse submitApproval(SubmitReviewRequest request, UUID reviewerUserId) {
        // 1. Cari Loan Application
        LoanApplication loanApplication = loanApplicationRepository.findById(request.getLoanApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Loan application not found: " + request.getLoanApplicationId()));

        // 2. Cari Internal User (Reviewer)
        InternalUser reviewer = internalUserRepository.findById(reviewerUserId)
                .orElseThrow(() -> new EntityNotFoundException("Internal user not found: " + reviewerUserId));

        // 3. Update Verified Income pada Employment (Jika di-input oleh reviewer)
        if (request.getVerifiedIncome() != null) {
            UUID customerId = loanApplication.getCustomer().getId();
            employmentRepository.findByCustomer_IdAndCustomerIsCurrentTrue(customerId)
                    .ifPresent(employment -> {
                        employment.setCustomerVerifiedIncome(request.getVerifiedIncome());
                        employment.setInternalUser(reviewer);
                        employmentRepository.save(employment);
                    });
        }

        // 4. Buat Entity LoanReview Baru
        LoanApproval loanApproval = LoanApproval.builder()
                .loanApplicationId(loanApplication)
                .approvedBy(reviewer)
                .result(ApprovalResult.valueOf(request.getResult()))
                .notes(request.getNotes())
                .approvedAt(Instant.now())
                .build();

        LoanApproval savedReview = loanApprovalRepository.save(loanApproval);

        String result = request.getResult();

        if ("approved".equalsIgnoreCase(result)) {
            loanApplication.setStatus("in_disbursement");
        } else if ("rejected".equalsIgnoreCase(result)) {
            loanApplication.setStatus("approval_rejected");
        } else {
            throw new IllegalArgumentException("Invalid review result: " + result);
        }
        loanApplicationRepository.save(loanApplication);

        // 6. Return Response via Mapper
        return mapper.toLoanApprovalResponse(savedReview);
    }

    @Override
    @Transactional
    public Page<LoanHistoryResponse> getApplicationApprovalHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<LoanApproval> approvalPage = loanApprovalRepository.findApprovalHistory(pageable);

        return approvalPage.map(review -> {
            var app = review.getLoanApplicationId(); // atau review.getLoanApplication()

            return new LoanHistoryResponse(
                    app.getId(),                             // 1. UUID (Tambahkan .getId() di sini)
                    app.getApplicationNumber(),              // 2. String
                    app.getCustomer().getCustomerFullName(), // 3. String
                    app.getAmountRequested(),                // 4. BigDecimal
                    app.getStatus(),                         // 5. String
                    review.getResult().name(),                      // 6. String
                    review.getNotes(),                       // 7. String
                    review.getApprovedAt()                   // 8. LocalDateTime
            );
        });
    }
}
