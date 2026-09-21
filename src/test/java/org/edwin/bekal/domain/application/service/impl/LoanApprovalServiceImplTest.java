package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.edwin.bekal.common.util.LoanReviewDetailMapper;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanReview;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.LoanApprovalRepository;
import org.edwin.bekal.domain.application.repository.LoanReviewRepository;
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.edwin.bekal.enums.ApprovalResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApprovalServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LoanReviewRepository loanReviewRepository;

    @Mock
    private InternalUserRepository internalUserRepository;

    @Mock
    private LoanReviewDetailMapper mapper;

    @Mock
    private LoanApprovalRepository loanApprovalRepository;

    @InjectMocks
    private LoanApprovalServiceImpl loanApprovalService;

    // ==================================================================== //
    //  1. getDetailApproval Test Cases                                     //
    // ==================================================================== //

    @Nested
    @DisplayName("getDetailApproval Tests")
    class GetDetailApprovalTests {

        @Test
        @DisplayName("getDetailApproval - Success when all entities and optional fields are present")
        void getDetailApproval_success_allEntitiesPresent() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);

            Employment employment = new Employment();
            Document document = new Document();
            LoanReview loanReview = new LoanReview();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(employmentRepository.findByCustomer_IdAndCustomerIsCurrentTrue(customerId)).willReturn(Optional.of(employment));
            given(documentRepository.findByCustomerIdAndIsLatestTrue(customerId)).willReturn(List.of(document));
            given(loanReviewRepository.findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId)).willReturn(Optional.of(loanReview));

            LoanApplicationResponse appResponse = LoanApplicationResponse.builder().id(loanApplicationId).build();
            CustomerResponse custResponse = CustomerResponse.builder().id(customerId).build();
            EmploymentResponse empResponse = EmploymentResponse.builder().build();
            DocumentResponse docResponse = DocumentResponse.builder().build();
            LoanReviewResponse reviewResponse = LoanReviewResponse.builder().build();

            given(mapper.toLoanApplicationResponse(loanApplication)).willReturn(appResponse);
            given(mapper.toCustomerResponse(customer)).willReturn(custResponse);
            given(mapper.toEmploymentResponse(employment)).willReturn(empResponse);
            given(mapper.toDocumentResponse(document)).willReturn(docResponse);
            given(mapper.toLoanReviewResponse(loanReview)).willReturn(reviewResponse);

            LoanReviewDetail result = loanApprovalService.getDetailApproval(loanApplicationId);

            assertThat(result).isNotNull();
            assertThat(result.getLoanApplicationResponse()).isEqualTo(appResponse);
            assertThat(result.getCustomerResponse()).isEqualTo(custResponse);
            assertThat(result.getEmploymentResponse()).isEqualTo(empResponse);
            assertThat(result.getDocumentResponse()).hasSize(1).contains(docResponse);
            assertThat(result.getLoanReviewResponse()).isEqualTo(reviewResponse);

            verify(loanApplicationRepository).findById(loanApplicationId);
            verify(customerRepository).findById(customerId);
            verify(employmentRepository).findByCustomer_IdAndCustomerIsCurrentTrue(customerId);
            verify(documentRepository).findByCustomerIdAndIsLatestTrue(customerId);
            verify(loanReviewRepository).findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId);
        }

        @Test
        @DisplayName("getDetailApproval - Success when optional employment and review are null")
        void getDetailApproval_success_optionalEntitiesEmpty() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(employmentRepository.findByCustomer_IdAndCustomerIsCurrentTrue(customerId)).willReturn(Optional.empty());
            given(documentRepository.findByCustomerIdAndIsLatestTrue(customerId)).willReturn(List.of());
            given(loanReviewRepository.findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId)).willReturn(Optional.empty());

            given(mapper.toLoanApplicationResponse(loanApplication)).willReturn(LoanApplicationResponse.builder().build());
            given(mapper.toCustomerResponse(customer)).willReturn(CustomerResponse.builder().build());
            given(mapper.toEmploymentResponse(null)).willReturn(null);
            given(mapper.toLoanReviewResponse(null)).willReturn(null);

            LoanReviewDetail result = loanApprovalService.getDetailApproval(loanApplicationId);

            assertThat(result).isNotNull();
            assertThat(result.getEmploymentResponse()).isNull();
            assertThat(result.getDocumentResponse()).isEmpty();
            assertThat(result.getLoanReviewResponse()).isNull();
        }

        @Test
        @DisplayName("getDetailApproval - Throws EntityNotFoundException when LoanApplication not found")
        void getDetailApproval_loanApplicationNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApprovalService.getDetailApproval(loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Loan application not found: " + loanApplicationId);

            verifyNoInteractions(customerRepository, employmentRepository, documentRepository, loanReviewRepository);
        }

        @Test
        @DisplayName("getDetailApproval - Throws EntityNotFoundException when Customer not found")
        void getDetailApproval_customerNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApprovalService.getDetailApproval(loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verifyNoInteractions(employmentRepository, documentRepository, loanReviewRepository);
        }
    }

    // ==================================================================== //
    //  2. submitApproval Test Cases                                        //
    // ==================================================================== //

    @Nested
    @DisplayName("submitApproval Tests")
    class SubmitApprovalTests {

        @Test
        @DisplayName("submitApproval - Success APPROVED with verified income updated")
        void submitApproval_approved_withVerifiedIncome_success() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setResult("APPROVED");
            request.setNotes("Approved by BM");
            request.setVerifiedIncome(new BigDecimal("15000000"));

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);

            InternalUser reviewer = new InternalUser();
            reviewer.setId(reviewerUserId);

            Employment employment = new Employment();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));
            given(employmentRepository.findByCustomer_IdAndCustomerIsCurrentTrue(customerId)).willReturn(Optional.of(employment));

            LoanApproval savedApproval = LoanApproval.builder()
                    .loanApplicationId(loanApplication)
                    .approvedBy(reviewer)
                    .result(ApprovalResult.APPROVED)
                    .notes("Approved by BM")
                    .approvedAt(Instant.now())
                    .build();

            given(loanApprovalRepository.save(any(LoanApproval.class))).willReturn(savedApproval);

            LoanApprovalResponse mockResponse = LoanApprovalResponse.builder().build();
            given(mapper.toLoanApprovalResponse(savedApproval)).willReturn(mockResponse);

            LoanApprovalResponse result = loanApprovalService.submitApproval(request, reviewerUserId);

            assertThat(result).isNotNull();
            assertThat(loanApplication.getStatus()).isEqualTo("in_disbursement");

            verify(employmentRepository).save(argThat(emp ->
                    new BigDecimal("15000000").equals(emp.getCustomerVerifiedIncome()) &&
                            reviewer.equals(emp.getInternalUser())
            ));
            verify(loanApplicationRepository).save(loanApplication);
            verify(loanApprovalRepository).save(any(LoanApproval.class));
        }

        @Test
        @DisplayName("submitApproval - Success REJECTED without verified income")
        void submitApproval_rejected_withoutVerifiedIncome_success() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setResult("REJECTED");
            request.setNotes("High risk profile");
            request.setVerifiedIncome(null);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);

            InternalUser reviewer = new InternalUser();
            reviewer.setId(reviewerUserId);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));

            LoanApproval savedApproval = LoanApproval.builder()
                    .loanApplicationId(loanApplication)
                    .approvedBy(reviewer)
                    .result(ApprovalResult.REJECTED)
                    .notes("High risk profile")
                    .build();

            given(loanApprovalRepository.save(any(LoanApproval.class))).willReturn(savedApproval);
            given(mapper.toLoanApprovalResponse(savedApproval)).willReturn(LoanApprovalResponse.builder().build());

            LoanApprovalResponse result = loanApprovalService.submitApproval(request, reviewerUserId);

            assertThat(result).isNotNull();
            assertThat(loanApplication.getStatus()).isEqualTo("approval_rejected");

            verifyNoInteractions(employmentRepository);
            verify(loanApplicationRepository).save(loanApplication);
            verify(loanApprovalRepository).save(any(LoanApproval.class));
        }

        @Test
        @DisplayName("submitApproval - Throws IllegalArgumentException for invalid result string")
        void submitApproval_invalidResult_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setResult("INVALID_RESULT");
            request.setNotes("Invalid review result test");

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);

            InternalUser reviewer = new InternalUser();
            reviewer.setId(reviewerUserId);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));

            assertThatThrownBy(() -> loanApprovalService.submitApproval(request, reviewerUserId))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(loanApprovalRepository, never()).save(any());
        }

        @Test
        @DisplayName("submitApproval - Throws EntityNotFoundException when LoanApplication not found")
        void submitApproval_loanApplicationNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setLoanApplicationId(loanApplicationId);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApprovalService.submitApproval(request, reviewerUserId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Loan application not found: " + loanApplicationId);

            verifyNoInteractions(internalUserRepository, employmentRepository, loanApprovalRepository);
        }

        @Test
        @DisplayName("submitApproval - Throws EntityNotFoundException when InternalUser not found")
        void submitApproval_reviewerNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitReviewRequest request = new SubmitReviewRequest();
            request.setLoanApplicationId(loanApplicationId);

            LoanApplication loanApplication = new LoanApplication();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApprovalService.submitApproval(request, reviewerUserId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Internal user not found: " + reviewerUserId);

            verifyNoInteractions(employmentRepository, loanApprovalRepository);
        }
    }

    // ==================================================================== //
    //  3. getApplicationApprovalHistory Test Cases                        //
    // ==================================================================== //

    @Nested
    @DisplayName("getApplicationApprovalHistory Tests")
    class GetApplicationApprovalHistoryTests {

        @Test
        @DisplayName("getApplicationApprovalHistory - Success returning mapped history page using record accessors")
        void getApplicationApprovalHistory_success() {
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);

            UUID loanId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setCustomerFullName("John Doe");

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanId);
            loanApplication.setApplicationNumber("APP-12345");
            loanApplication.setCustomer(customer);
            loanApplication.setAmountRequested(new BigDecimal("10000000"));
            loanApplication.setStatus("in_disbursement");

            Instant now = Instant.now();
            LoanApproval approval = LoanApproval.builder()
                    .loanApplicationId(loanApplication)
                    .result(ApprovalResult.APPROVED)
                    .notes("Approved by manager")
                    .approvedAt(now)
                    .build();

            Page<LoanApproval> mockPage = new PageImpl<>(List.of(approval), pageable, 1);

            given(loanApprovalRepository.findApprovalHistory(pageable)).willReturn(mockPage);

            Page<LoanHistoryResponse> result = loanApprovalService.getApplicationApprovalHistory(page, size);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);

            LoanHistoryResponse historyResponse = result.getContent().get(0);
            assertThat(historyResponse.applicationId()).isEqualTo(loanId);
            assertThat(historyResponse.applicationNumber()).isEqualTo("APP-12345");
            assertThat(historyResponse.customerName()).isEqualTo("John Doe");
            assertThat(historyResponse.amountRequested()).isEqualTo(new BigDecimal("10000000"));
            assertThat(historyResponse.currentStatus()).isEqualTo("in_disbursement");
            assertThat(historyResponse.reviewResult()).isEqualTo("APPROVED");
            assertThat(historyResponse.reviewNotes()).isEqualTo("Approved by manager");
            assertThat(historyResponse.reviewedAt()).isEqualTo(now);

            verify(loanApprovalRepository).findApprovalHistory(pageable);
        }
    }
}