package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PaymentTransactionRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.edwin.bekal.enums.LoanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private PaymentTransactionRepository trxPaymentRepository;

    @InjectMocks
    private HomeServiceImpl homeService;

    // ==================================================================== //
    //  Null Customer ID / Pre-Application Scenarios                        //
    // ==================================================================== //

    @Test
    @DisplayName("getDashboardData - Returns pre-application response when customerId is null")
    void getDashboardData_nullCustomerId_returnsPreApplicationResponse() {
        HomeDashboardResponse response = homeService.getDashboardData(null);

        assertThat(response).isNotNull();
        assertThat(response.getLoanStatus()).isEqualTo(LoanStatus.CLOSED.name());
        assertThat(response.getCreditTier()).isEqualTo(CreditTier.TIER_1.name());
        assertThat(response.getMaxLimit()).isEqualTo(CreditTier.TIER_1.getMaxCap());
        assertThat(response.getAvailableLimit()).isEqualTo(CreditTier.TIER_1.getMaxCap());
        assertThat(response.getActiveBillAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getSuccessfulLoansCount()).isZero();
        assertThat(response.getRequiredForNextTier()).isEqualTo(CreditTier.TIER_2.getMinSuccessfulLoansRequired());
        assertThat(response.getDueDate()).isNull();
        assertThat(response.getActiveLoanId()).isNull();

        verifyNoInteractions(customerRepository, loanApplicationRepository, trxPaymentRepository);
    }

    @Test
    @DisplayName("getDashboardData - Throws IllegalArgumentException when customer is not found")
    void getDashboardData_customerNotFound_throwsException() {
        UUID customerId = UUID.randomUUID();
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> homeService.getDashboardData(customerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer tidak ditemukan");

        verify(customerRepository).findById(customerId);
        verifyNoInteractions(loanApplicationRepository, trxPaymentRepository);
    }

    // ==================================================================== //
    //  No Active Loan / Non-DISBURSED Loan Scenarios                       //
    // ==================================================================== //

    @Test
    @DisplayName("getDashboardData - Default values when customer has null credit tier, null success count, and no active loan")
    void getDashboardData_noActiveLoan_defaultsTierAndCount() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(null); // Will default to TIER_1
        customer.setSuccessfulLoansCount(null); // Will default to 0

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.empty());

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        assertThat(response).isNotNull();
        assertThat(response.getLoanStatus()).isEqualTo(LoanStatus.CLOSED.name());
        assertThat(response.getCreditTier()).isEqualTo(CreditTier.TIER_1.name());
        assertThat(response.getAvailableLimit()).isEqualTo(CreditTier.TIER_1.getMaxCap());
        assertThat(response.getActiveBillAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getSuccessfulLoansCount()).isZero();
        assertThat(response.getRequiredForNextTier()).isEqualTo(CreditTier.TIER_2.getMinSuccessfulLoansRequired());
        assertThat(response.getActiveLoanId()).isNull();

        verify(customerRepository).findById(customerId);
        verify(loanApplicationRepository).findTopByCustomer_IdOrderBySubmittedAtDesc(customerId);
        verifyNoInteractions(trxPaymentRepository);
    }

    @Test
    @DisplayName("getDashboardData - Handles invalid status string and falls back to IN_REVIEW")
    void getDashboardData_invalidLoanStatus_fallsBackToInReview() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_1);

        LoanApplication loan = new LoanApplication();
        loan.setStatus("INVALID_STATUS_STRING");

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.of(loan));

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        assertThat(response.getLoanStatus()).isEqualTo(LoanStatus.IN_REVIEW.name());
        verifyNoInteractions(trxPaymentRepository);
    }

    // ==================================================================== //
    //  DISBURSED Loan Scenarios & Bill Calculation                         //
    // ==================================================================== //

    @Test
    @DisplayName("getDashboardData - Disbursed loan with remaining balance greater than monthly installment")
    void getDashboardData_disbursedLoan_standardBillCalculation() {
        UUID customerId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_1);
        customer.setSuccessfulLoansCount(1);

        LoanApplication loan = new LoanApplication();
        loan.setId(loanId);
        loan.setStatus("DISBURSED");
        loan.setTotalRepayment(new BigDecimal("12000000"));
        loan.setMonthlyInstallment(new BigDecimal("1000000"));
        loan.setSubmittedAt(Instant.parse("2026-01-15T00:00:00Z"));

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.of(loan));
        given(trxPaymentRepository.sumSuccessfulPaymentsByLoanId(loanId)).willReturn(new BigDecimal("2000000"));
        given(trxPaymentRepository.countSuccessfulPaymentsByLoanId(loanId)).willReturn(2L);

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        // remaining = 12M - 2M = 10M > 1M, so activeBill = 1M
        assertThat(response.getLoanStatus()).isEqualTo(LoanStatus.DISBURSED.name());
        assertThat(response.getActiveBillAmount()).isEqualTo(new BigDecimal("1000000"));
        assertThat(response.getAvailableLimit()).isEqualTo(CreditTier.TIER_1.getMaxCap().subtract(new BigDecimal("1000000")));
        assertThat(response.getActiveLoanId()).isEqualTo(loanId.toString());
        // startDate = 2026-01-15, paidInstallments = 2 -> dueDate = startDate.plusMonths(3) = 2026-04-15
        assertThat(response.getDueDate()).isEqualTo(LocalDate.of(2026, 4, 15));
    }

    @Test
    @DisplayName("getDashboardData - Disbursed loan with remaining balance less than monthly installment (Final Repayment)")
    void getDashboardData_disbursedLoan_finalRepaymentBillCalculation() {
        UUID customerId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_2);
        customer.setSuccessfulLoansCount(3);

        LoanApplication loan = new LoanApplication();
        loan.setId(loanId);
        loan.setStatus("DISBURSED");
        loan.setTotalRepayment(new BigDecimal("10000000"));
        loan.setMonthlyInstallment(new BigDecimal("2000000"));
        loan.setSubmittedAt(Instant.parse("2026-01-01T00:00:00Z"));

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.of(loan));
        given(trxPaymentRepository.sumSuccessfulPaymentsByLoanId(loanId)).willReturn(new BigDecimal("9500000"));
        given(trxPaymentRepository.countSuccessfulPaymentsByLoanId(loanId)).willReturn(4L);

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        // remaining = 10M - 9.5M = 500k < 2M, so activeBill = 500k
        assertThat(response.getActiveBillAmount()).isEqualTo(new BigDecimal("500000"));
        assertThat(response.getAvailableLimit()).isEqualTo(CreditTier.TIER_2.getMaxCap().subtract(new BigDecimal("500000")));
        assertThat(response.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    @DisplayName("getDashboardData - Disbursed loan with overpayment caps remaining balance to zero")
    void getDashboardData_disbursedLoan_overpaymentCapsToZero() {
        UUID customerId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_1);

        LoanApplication loan = new LoanApplication();
        loan.setId(loanId);
        loan.setStatus("DISBURSED");
        loan.setTotalRepayment(new BigDecimal("5000000"));
        loan.setMonthlyInstallment(new BigDecimal("1000000"));
        loan.setSubmittedAt(Instant.parse("2026-01-01T00:00:00Z"));

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.of(loan));
        // Overpaid: Total paid (6M) > Total Repayment (5M)
        given(trxPaymentRepository.sumSuccessfulPaymentsByLoanId(loanId)).willReturn(new BigDecimal("6000000"));
        given(trxPaymentRepository.countSuccessfulPaymentsByLoanId(loanId)).willReturn(5L);

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        assertThat(response.getActiveBillAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getAvailableLimit()).isEqualTo(CreditTier.TIER_1.getMaxCap());
    }

    // ==================================================================== //
    //  Credit Tier Next Level Requirements Test Cases                      //
    // ==================================================================== //

    @Test
    @DisplayName("calculateRequiredForNextTier - Tier 3 returns 0 requirement")
    void getDashboardData_tier3_returnsZeroRequiredForNextTier() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_3);
        customer.setSuccessfulLoansCount(10);

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.empty());

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        assertThat(response.getCreditTier()).isEqualTo(CreditTier.TIER_3.name());
        assertThat(response.getRequiredForNextTier()).isZero();
    }

    @Test
    @DisplayName("calculateRequiredForNextTier - Returns 0 when current success count exceeds or equals requirement")
    void getDashboardData_successCountExceedsRequirement_returnsZero() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setCreditTier(CreditTier.TIER_1);
        customer.setSuccessfulLoansCount(100); // Exceeds Tier 2 requirement

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId)).willReturn(Optional.empty());

        HomeDashboardResponse response = homeService.getDashboardData(customerId);

        assertThat(response.getRequiredForNextTier()).isZero();
    }
}