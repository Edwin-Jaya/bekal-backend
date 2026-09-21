package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.application.dto.CreateLoanApplicationRequest;
import org.edwin.bekal.domain.application.dto.LoanApplicationResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PlafondRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.enums.CreditTier;
import org.edwin.bekal.enums.LoanStatus;
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
import org.springframework.data.domain.Sort;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PlafondRepository plafondRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService;

    // ==================================================================== //
    //  Pagination / Query Method Test Cases                                //
    // ==================================================================== //

    @Nested
    @DisplayName("Read-Only Pagination Queries")
    class ReadOnlyQueries {

        @Test
        @DisplayName("getLoanApplicationByCustomer - Success returning mapped CacheablePage")
        void getLoanApplicationByCustomer_success() {
            UUID customerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            LoanApplication mockLoan = createMockLoanApplication();
            Page<LoanApplication> page = new PageImpl<>(List.of(mockLoan), pageable, 1);

            given(loanApplicationRepository.findByCustomer_Id(customerId, pageable)).willReturn(page);

            // Menggunakan CacheablePage sesuai implementasi service terbaru
            CacheablePage<LoanApplicationResponse> result = loanApplicationService.getLoanApplicationByCustomer(customerId, 0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(mockLoan.getId());
            verify(loanApplicationRepository).findByCustomer_Id(customerId, pageable);
        }

        @Test
        @DisplayName("getLoanApplication - Success returning mapped page")
        void getLoanApplication_success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            LoanApplication mockLoan = createMockLoanApplication();
            Page<LoanApplication> page = new PageImpl<>(List.of(mockLoan), pageable, 1);

            given(loanApplicationRepository.findAll(pageable)).willReturn(page);

            Page<LoanApplicationResponse> result = loanApplicationService.getLoanApplication(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(loanApplicationRepository).findAll(pageable);
        }

        @Test
        @DisplayName("getPendingLoanApplication - Success returning mapped page")
        void getPendingLoanApplication_success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            LoanApplication mockLoan = createMockLoanApplication();
            Page<LoanApplication> page = new PageImpl<>(List.of(mockLoan), pageable, 1);

            given(loanApplicationRepository.findPendingMarketingReviews(pageable)).willReturn(page);

            Page<LoanApplicationResponse> result = loanApplicationService.getPendingLoanApplication(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(loanApplicationRepository).findPendingMarketingReviews(pageable);
        }

        @Test
        @DisplayName("getPendingLoanApplicationApproval - Success returning mapped page")
        void getPendingLoanApplicationApproval_success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            LoanApplication mockLoan = createMockLoanApplication();
            Page<LoanApplication> page = new PageImpl<>(List.of(mockLoan), pageable, 1);

            given(loanApplicationRepository.findPendingBranchMarketingApproval(pageable)).willReturn(page);

            Page<LoanApplicationResponse> result = loanApplicationService.getPendingLoanApplicationApproval(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(loanApplicationRepository).findPendingBranchMarketingApproval(pageable);
        }

        @Test
        @DisplayName("getPendingLoanApplicationDisbursement - Success returning mapped page")
        void getPendingLoanApplicationDisbursement_success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            LoanApplication mockLoan = createMockLoanApplication();
            Page<LoanApplication> page = new PageImpl<>(List.of(mockLoan), pageable, 1);

            given(loanApplicationRepository.findPendingBackOfficeDisbursement(pageable)).willReturn(page);

            Page<LoanApplicationResponse> result = loanApplicationService.getPendingLoanApplicationDisbursement(0, 10);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(loanApplicationRepository).findPendingBackOfficeDisbursement(pageable);
        }
    }

    // ==================================================================== //
    //  createLoanApplication Validation Failure Test Cases                 //
    // ==================================================================== //

    @Nested
    @DisplayName("createLoanApplication - Validation Failures")
    class CreateLoanApplicationValidations {

        @Test
        @DisplayName("Throws exception when Customer is not found")
        void createLoanApplication_customerNotFound_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer tidak ditemukan");

            verifyNoInteractions(plafondRepository, branchRepository, loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when Plafond is not found")
        void createLoanApplication_plafondNotFound_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            Customer mockCustomer = new Customer();

            given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.of(mockCustomer));
            given(plafondRepository.findById(request.getPlafond().getId())).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Plafond produk tidak ditemukan");

            verifyNoInteractions(branchRepository, loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when Branch is not found")
        void createLoanApplication_branchNotFound_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            Customer mockCustomer = new Customer();
            Plafond mockPlafond = new Plafond();

            given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.of(mockCustomer));
            given(plafondRepository.findById(request.getPlafond().getId())).willReturn(Optional.of(mockPlafond));
            given(branchRepository.findById(request.getBranch().getId())).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Branch tidak ditemukan");

            verifyNoInteractions(loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when Tenor is not in ALLOWED_TENORS whitelist")
        void createLoanApplication_invalidTenor_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(10); // Not in {6, 8, 12, 16, 20, 24}

            setupValidEntitiesForRequest(request, CreditTier.TIER_1, new BigDecimal("100000000"), new BigDecimal("0"), 24);

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Tenor tidak valid. Pilihan tenor yang tersedia: 6, 8, 12, 16, 20, 24 bulan");

            verifyNoInteractions(loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when requested amount exceeds Customer Credit Tier max cap")
        void createLoanApplication_exceedsTierCap_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(12);
            request.setAmountRequested(CreditTier.TIER_1.getMaxCap().add(new BigDecimal("1000")));

            setupValidEntitiesForRequest(request, CreditTier.TIER_1, new BigDecimal("1000000000"), new BigDecimal("0"), 24);

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Jumlah pengajuan melebihi batas maksimum Tier TIER_1");

            verifyNoInteractions(loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when requested amount exceeds remaining Plafond available amount")
        void createLoanApplication_exceedsAvailablePlafond_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(12);
            request.setAmountRequested(new BigDecimal("5000000")); // Tier 1 max cap is larger than 5M

            // Plafond total 10M, used 6M -> available 4M (less than requested 5M)
            setupValidEntitiesForRequest(request, CreditTier.TIER_1, new BigDecimal("10000000"), new BigDecimal("6000000"), 24);

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Jumlah pengajuan melebihi sisa plafond produk");

            verifyNoInteractions(loanApplicationRepository);
        }

        @Test
        @DisplayName("Throws exception when requested tenor exceeds Plafond max tenor")
        void createLoanApplication_exceedsMaxPlafondTenor_throwsException() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(24);
            request.setAmountRequested(new BigDecimal("2000000"));

            // Plafond max tenor is only 12 months
            setupValidEntitiesForRequest(request, CreditTier.TIER_1, new BigDecimal("100000000"), new BigDecimal("0"), 12);

            assertThatThrownBy(() -> loanApplicationService.createLoanApplication(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Tenor melebihi batas maksimum produk plafond (12 bulan)");

            verifyNoInteractions(loanApplicationRepository);
        }
    }

    // ==================================================================== //
    //  createLoanApplication Success & Interest Calculation Test Cases    //
    // ==================================================================== //

    @Nested
    @DisplayName("createLoanApplication - Success Scenarios")
    class CreateLoanApplicationSuccess {

        @Test
        @DisplayName("Success creating application with customer having explicit CreditTier and correct interest calculations")
        void createLoanApplication_success_withExplicitCreditTier() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(12);
            request.setAmountRequested(new BigDecimal("10000000")); // 10 Million
            request.setPurpose("Modal Usaha");

            Customer customer = new Customer();
            customer.setId(request.getCustomer().getId());
            customer.setCreditTier(CreditTier.TIER_2);

            Plafond plafond = new Plafond();
            plafond.setId(request.getPlafond().getId());
            plafond.setPlafondAmount(new BigDecimal("500000000"));
            plafond.setUsedAmount(new BigDecimal("100000000"));
            plafond.setMaxTenorMonths(24);
            plafond.setInterestRate(new BigDecimal("12.00")); // 12% per annum

            Branch branch = new Branch();
            branch.setId(request.getBranch().getId());

            given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.of(customer));
            given(plafondRepository.findById(request.getPlafond().getId())).willReturn(Optional.of(plafond));
            given(branchRepository.findById(request.getBranch().getId())).willReturn(Optional.of(branch));

            given(loanApplicationRepository.save(any(LoanApplication.class))).willAnswer(invocation -> {
                LoanApplication app = invocation.getArgument(0);
                app.setId(UUID.randomUUID());
                return app;
            });

            LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);

            assertThat(response).isNotNull();
            assertThat(response.getAmountRequested()).isEqualTo(new BigDecimal("10000000"));
            assertThat(response.getTenorMonths()).isEqualTo(12);
            assertThat(response.getPurpose()).isEqualTo("Modal Usaha");
            assertThat(response.getInterestRate()).isEqualTo(new BigDecimal("12.00"));
            assertThat(response.getStatus()).isEqualTo(LoanStatus.IN_REVIEW.getValue());
            assertThat(response.getApplicationNumber()).startsWith("APP-");

            // Calculation Verification:
            // totalInterest = 10,000,000 * 12% * 12 / 12 = 1,200,000
            // totalRepayment = 10,000,000 + 1,200,000 = 11,200,000.00
            // monthlyInstallment = 11,200,000 / 12 = 933,333.33
            assertThat(response.getTotalRepayment()).isEqualTo(new BigDecimal("11200000.00"));
            assertThat(response.getMonthlyInstallment()).isEqualTo(new BigDecimal("933333.33"));

            verify(loanApplicationRepository).save(argThat(app ->
                    app.getApplicationNumber().startsWith("APP-") &&
                            app.getStatus().equals(LoanStatus.IN_REVIEW.getValue()) &&
                            app.getSubmittedAt() != null
            ));
        }

        @Test
        @DisplayName("Success creating application when customer CreditTier is null (defaults to TIER_1)")
        void createLoanApplication_success_withNullCreditTierDefaultsToTier1() {
            CreateLoanApplicationRequest request = createBaseRequest();
            request.setTenorMonths(6);
            request.setAmountRequested(new BigDecimal("2000000"));

            Customer customer = new Customer();
            customer.setId(request.getCustomer().getId());
            customer.setCreditTier(null); // Null tier will default to TIER_1

            Plafond plafond = new Plafond();
            plafond.setId(request.getPlafond().getId());
            plafond.setPlafondAmount(new BigDecimal("100000000"));
            plafond.setUsedAmount(BigDecimal.ZERO);
            plafond.setMaxTenorMonths(12);
            plafond.setInterestRate(new BigDecimal("10.00"));

            Branch branch = new Branch();
            branch.setId(request.getBranch().getId());

            given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.of(customer));
            given(plafondRepository.findById(request.getPlafond().getId())).willReturn(Optional.of(plafond));
            given(branchRepository.findById(request.getBranch().getId())).willReturn(Optional.of(branch));

            given(loanApplicationRepository.save(any(LoanApplication.class))).willAnswer(invocation -> invocation.getArgument(0));

            LoanApplicationResponse response = loanApplicationService.createLoanApplication(request);

            assertThat(response).isNotNull();
            assertThat(response.getTenorMonths()).isEqualTo(6);
            verify(loanApplicationRepository).save(any(LoanApplication.class));
        }
    }

    // ==================================================================== //
    //  Helper Methods                                                      //
    // ==================================================================== //

    private CreateLoanApplicationRequest createBaseRequest() {
        UUID customerId = UUID.randomUUID();
        UUID plafondId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Customer customerRef = new Customer();
        customerRef.setId(customerId);

        Plafond plafondRef = new Plafond();
        plafondRef.setId(plafondId);

        Branch branchRef = new Branch();
        branchRef.setId(branchId);

        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest();
        request.setCustomer(customerRef);
        request.setPlafond(plafondRef);
        request.setBranch(branchRef);
        request.setTenorMonths(6);
        request.setAmountRequested(new BigDecimal("1000000"));
        request.setPurpose("Biaya Pendidikan");

        return request;
    }

    private void setupValidEntitiesForRequest(CreateLoanApplicationRequest request,
                                              CreditTier tier,
                                              BigDecimal plafondAmount,
                                              BigDecimal usedAmount,
                                              int maxTenorMonths) {
        Customer customer = new Customer();
        customer.setId(request.getCustomer().getId());
        customer.setCreditTier(tier);

        Plafond plafond = new Plafond();
        plafond.setId(request.getPlafond().getId());
        plafond.setPlafondAmount(plafondAmount);
        plafond.setUsedAmount(usedAmount);
        plafond.setMaxTenorMonths(maxTenorMonths);
        plafond.setInterestRate(new BigDecimal("10.00"));

        Branch branch = new Branch();
        branch.setId(request.getBranch().getId());

        given(customerRepository.findById(request.getCustomer().getId())).willReturn(Optional.of(customer));
        given(plafondRepository.findById(request.getPlafond().getId())).willReturn(Optional.of(plafond));
        given(branchRepository.findById(request.getBranch().getId())).willReturn(Optional.of(branch));
    }

    private LoanApplication createMockLoanApplication() {
        LoanApplication loan = new LoanApplication();
        loan.setId(UUID.randomUUID());
        loan.setApplicationNumber("APP-12345678");
        loan.setAmountRequested(new BigDecimal("5000000"));
        loan.setTenorMonths(12);
        loan.setStatus(LoanStatus.IN_REVIEW.getValue());
        loan.setSubmittedAt(Instant.now());
        return loan;
    }
}