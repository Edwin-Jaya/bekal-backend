package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
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
import org.edwin.bekal.domain.customer.dto.CustomerResponse;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanDisbursementServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private InternalUserRepository internalUserRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private LoanReviewRepository loanReviewRepository;

    @Mock
    private LoanReviewDetailMapper mapper;

    @Mock
    private LoanApprovalRepository loanApprovalRepository;

    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private PushNotificationServiceImpl pushNotificationService;

    @InjectMocks
    private LoanDisbursementServiceImpl loanDisbursementService;

    // ==================================================================== //
    //  1. getDetailDisbursement Test Cases                                 //
    // ==================================================================== //

    @Nested
    @DisplayName("getDetailDisbursement Tests")
    class GetDetailDisbursementTests {

        @Test
        @DisplayName("getDetailDisbursement - Success when all entities and optional fields are present")
        void getDetailDisbursement_success_allEntitiesPresent() {
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
            LoanApproval loanApproval = new LoanApproval();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(employmentRepository.findByCustomer_IdAndCustomerIsCurrentTrue(customerId)).willReturn(Optional.of(employment));
            given(documentRepository.findByCustomerIdAndIsLatestTrue(customerId)).willReturn(List.of(document));
            given(loanReviewRepository.findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId)).willReturn(Optional.of(loanReview));
            given(loanApprovalRepository.findTopByLoanApplicationId_IdOrderByApprovedAtDesc(loanApplicationId)).willReturn(Optional.of(loanApproval));

            LoanApplicationResponse appResponse = LoanApplicationResponse.builder().id(loanApplicationId).build();
            CustomerResponse custResponse = CustomerResponse.builder().id(customerId).build();
            EmploymentResponse empResponse = EmploymentResponse.builder().build();
            DocumentResponse docResponse = DocumentResponse.builder().build();
            LoanReviewResponse reviewResponse = LoanReviewResponse.builder().build();
            LoanApprovalResponse approvalResponse = LoanApprovalResponse.builder().build();

            given(mapper.toLoanApplicationResponse(loanApplication)).willReturn(appResponse);
            given(mapper.toCustomerResponse(customer)).willReturn(custResponse);
            given(mapper.toEmploymentResponse(employment)).willReturn(empResponse);
            given(mapper.toDocumentResponse(document)).willReturn(docResponse);
            given(mapper.toLoanReviewResponse(loanReview)).willReturn(reviewResponse);
            given(mapper.toLoanApprovalResponse(loanApproval)).willReturn(approvalResponse);

            LoanReviewDetail result = loanDisbursementService.getDetailDisbursement(loanApplicationId);

            assertThat(result).isNotNull();
            assertThat(result.getLoanApplicationResponse()).isEqualTo(appResponse);
            assertThat(result.getCustomerResponse()).isEqualTo(custResponse);
            assertThat(result.getEmploymentResponse()).isEqualTo(empResponse);
            assertThat(result.getDocumentResponse()).hasSize(1).contains(docResponse);
            assertThat(result.getLoanReviewResponse()).isEqualTo(reviewResponse);
            assertThat(result.getLoanApprovalResponse()).isEqualTo(approvalResponse);

            verify(loanApplicationRepository).findById(loanApplicationId);
            verify(customerRepository).findById(customerId);
            verify(employmentRepository).findByCustomer_IdAndCustomerIsCurrentTrue(customerId);
            verify(documentRepository).findByCustomerIdAndIsLatestTrue(customerId);
            verify(loanReviewRepository).findTopByLoanApplicationId_IdOrderByReviewedAtDesc(loanApplicationId);
            verify(loanApprovalRepository).findTopByLoanApplicationId_IdOrderByApprovedAtDesc(loanApplicationId);
        }

        @Test
        @DisplayName("getDetailDisbursement - Success when optional employment, review, and approval are null")
        void getDetailDisbursement_success_optionalEntitiesEmpty() {
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
            given(loanApprovalRepository.findTopByLoanApplicationId_IdOrderByApprovedAtDesc(loanApplicationId)).willReturn(Optional.empty());

            given(mapper.toLoanApplicationResponse(loanApplication)).willReturn(LoanApplicationResponse.builder().build());
            given(mapper.toCustomerResponse(customer)).willReturn(CustomerResponse.builder().build());
            given(mapper.toEmploymentResponse(null)).willReturn(null);
            given(mapper.toLoanReviewResponse(null)).willReturn(null);
            given(mapper.toLoanApprovalResponse(null)).willReturn(null);

            LoanReviewDetail result = loanDisbursementService.getDetailDisbursement(loanApplicationId);

            assertThat(result).isNotNull();
            assertThat(result.getEmploymentResponse()).isNull();
            assertThat(result.getDocumentResponse()).isEmpty();
            assertThat(result.getLoanReviewResponse()).isNull();
            assertThat(result.getLoanApprovalResponse()).isNull();
        }

        @Test
        @DisplayName("getDetailDisbursement - Throws EntityNotFoundException when LoanApplication not found")
        void getDetailDisbursement_loanApplicationNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanDisbursementService.getDetailDisbursement(loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Loan application not found: " + loanApplicationId);

            verifyNoInteractions(customerRepository, employmentRepository, documentRepository, loanReviewRepository, loanApprovalRepository);
        }

        @Test
        @DisplayName("getDetailDisbursement - Throws EntityNotFoundException when Customer not found")
        void getDetailDisbursement_customerNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanDisbursementService.getDetailDisbursement(loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verifyNoInteractions(employmentRepository, documentRepository, loanReviewRepository, loanApprovalRepository);
        }
    }

    // ==================================================================== //
    //  2. submitDisbursement Test Cases                                    //
    // ==================================================================== //

    @Nested
    @DisplayName("submitDisbursement Tests")
    class SubmitDisbursementTests {

        @Test
        @DisplayName("submitDisbursement - Success with SUCCESS status and status updated to DISBURSED")
        void submitDisbursement_successStatus_disbursed() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            BigDecimal amountRequested = new BigDecimal("25000000");

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setStatus("success");
            request.setReferenceNumber("REF-123456");

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);
            loanApplication.setAmountRequested(amountRequested);

            InternalUser reviewer = new InternalUser();
            reviewer.setId(reviewerUserId);

            BankAccount bankAccount = new BankAccount();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));
            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.of(bankAccount));

            LoanDisbursement savedDisbursement = LoanDisbursement.builder()
                    .loanApplicationId(loanApplication)
                    .disbursedBy(reviewer)
                    .disbursementAmount(amountRequested)
                    .customerBankId(bankAccount)
                    .referenceNumber("REF-123456")
                    .status(DisburseResult.SUCCESS)
                    .disbursedAt(Instant.now())
                    .build();

            given(loanDisbursementRepository.save(any(LoanDisbursement.class))).willReturn(savedDisbursement);

            LoanDisbursementResponse mockResponse = LoanDisbursementResponse.builder().build();
            given(mapper.toLoanDisbursementResponse(savedDisbursement)).willReturn(mockResponse);

            LoanDisbursementResponse result = loanDisbursementService.submitDisbursement(request, reviewerUserId);

            assertThat(result).isNotNull();
            assertThat(loanApplication.getStatus()).isEqualTo("DISBURSED");

            verify(loanDisbursementRepository).save(any(LoanDisbursement.class));
            verify(loanApplicationRepository).save(loanApplication);
            verify(pushNotificationService).sendToCustomer(
                    eq(customerId),
                    eq("Dana Kamu Sudah Cair! 🎉"),
                    contains("25.000.000")
            );
        }

        @Test
        @DisplayName("submitDisbursement - Success with FAILED status and status updated to CANCELLED")
        void submitDisbursement_failedStatus_cancelled() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setStatus("FAILED");
            request.setReferenceNumber("REF-999999");

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanApplicationId);
            loanApplication.setCustomer(customer);
            loanApplication.setAmountRequested(new BigDecimal("10000000"));

            InternalUser reviewer = new InternalUser();
            reviewer.setId(reviewerUserId);

            BankAccount bankAccount = new BankAccount();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));
            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.of(bankAccount));

            LoanDisbursement savedDisbursement = LoanDisbursement.builder()
                    .status(DisburseResult.FAILED)
                    .disbursementAmount(new BigDecimal("10000000"))
                    .build();

            given(loanDisbursementRepository.save(any(LoanDisbursement.class))).willReturn(savedDisbursement);
            given(mapper.toLoanDisbursementResponse(savedDisbursement)).willReturn(LoanDisbursementResponse.builder().build());

            LoanDisbursementResponse result = loanDisbursementService.submitDisbursement(request, reviewerUserId);

            assertThat(result).isNotNull();
            assertThat(loanApplication.getStatus()).isEqualTo("CANCELLED");

            verify(loanDisbursementRepository).save(any(LoanDisbursement.class));
            verify(loanApplicationRepository).save(loanApplication);
            verify(pushNotificationService).sendToCustomer(eq(customerId), anyString(), anyString());
        }

        @Test
        @DisplayName("submitDisbursement - Throws EntityNotFoundException when LoanApplication not found")
        void submitDisbursement_loanApplicationNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanDisbursementService.submitDisbursement(request, reviewerUserId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Loan application not found with ID: " + loanApplicationId);

            verifyNoInteractions(internalUserRepository, bankAccountRepository, loanDisbursementRepository, pushNotificationService);
        }

        @Test
        @DisplayName("submitDisbursement - Throws EntityNotFoundException when InternalUser not found")
        void submitDisbursement_reviewerNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);

            LoanApplication loanApplication = new LoanApplication();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanDisbursementService.submitDisbursement(request, reviewerUserId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Internal user not found with ID: " + reviewerUserId);

            verifyNoInteractions(bankAccountRepository, loanDisbursementRepository, pushNotificationService);
        }

        @Test
        @DisplayName("submitDisbursement - Throws EntityNotFoundException when BankAccount not found")
        void submitDisbursement_bankAccountNotFound_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setCustomer(customer);

            InternalUser reviewer = new InternalUser();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));
            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanDisbursementService.submitDisbursement(request, reviewerUserId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Bank account not found for customer: " + customerId);

            verifyNoInteractions(loanDisbursementRepository, pushNotificationService);
        }

        @Test
        @DisplayName("submitDisbursement - Throws IllegalArgumentException for invalid disburse status")
        void submitDisbursement_invalidStatus_throwsException() {
            UUID loanApplicationId = UUID.randomUUID();
            UUID reviewerUserId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();

            SubmitDisbursementRequest request = new SubmitDisbursementRequest();
            request.setLoanApplicationId(loanApplicationId);
            request.setStatus("INVALID_STATUS");

            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setCustomer(customer);

            InternalUser reviewer = new InternalUser();
            BankAccount bankAccount = new BankAccount();

            given(loanApplicationRepository.findById(loanApplicationId)).willReturn(Optional.of(loanApplication));
            given(internalUserRepository.findById(reviewerUserId)).willReturn(Optional.of(reviewer));
            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.of(bankAccount));

            assertThatThrownBy(() -> loanDisbursementService.submitDisbursement(request, reviewerUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid disburse status: INVALID_STATUS");

            verifyNoInteractions(loanDisbursementRepository, pushNotificationService);
        }
    }

    // ==================================================================== //
    //  3. getApplicationDisbursementHistory Test Cases                    //
    // ==================================================================== //

    @Nested
    @DisplayName("getApplicationDisbursementHistory Tests")
    class GetApplicationDisbursementHistoryTests {

        @Test
        @DisplayName("getApplicationDisbursementHistory - Success returning mapped history page")
        void getApplicationDisbursementHistory_success() {
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);

            UUID loanId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setCustomerFullName("Jane Doe");

            LoanApplication loanApplication = new LoanApplication();
            loanApplication.setId(loanId);
            loanApplication.setApplicationNumber("APP-67890");
            loanApplication.setAmountRequested(new BigDecimal("50000000"));
            loanApplication.setCustomer(customer);
            loanApplication.setStatus("DISBURSED");

            Instant disbursedAt = Instant.now();
            LoanDisbursement disbursement = LoanDisbursement.builder()
                    .loanApplicationId(loanApplication)
                    .disbursedAt(disbursedAt)
                    .build();

            Page<LoanDisbursement> mockPage = new PageImpl<>(List.of(disbursement), pageable, 1);

            given(loanDisbursementRepository.findDisbursementHistory(pageable)).willReturn(mockPage);

            Page<LoanDisbursementHistoryResponse> result = loanDisbursementService.getApplicationDisbursementHistory(page, size);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);

            LoanDisbursementHistoryResponse historyResponse = result.getContent().get(0);
            assertThat(historyResponse).isNotNull();

            verify(loanDisbursementRepository).findDisbursementHistory(pageable);
        }
    }
}