package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.edwin.bekal.domain.application.dto.LoanBalanceResponse;
import org.edwin.bekal.domain.application.dto.PaymentHistoryResponse;
import org.edwin.bekal.domain.application.dto.RepaymentRequest;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.PaymentTransaction;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PaymentTransactionRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.LoanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private LoanApplicationRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentTransactionRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Nested
    @DisplayName("processRepayment Tests")
    class ProcessRepaymentTests {

        @Test
        @DisplayName("Should throw IllegalStateException when transaction reference already exists")
        void processRepayment_duplicateReference_throwsException() {
            RepaymentRequest request = new RepaymentRequest();
            request.setTransactionReference("REF-123");

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(true);

            assertThatThrownBy(() -> paymentService.processRepayment(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Transaksi dengan referensi ini sudah pernah diproses");

            verify(paymentRepository).existsByTransactionReference("REF-123");
            verifyNoInteractions(loanRepository, customerRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when loan does not exist")
        void processRepayment_loanNotFound_throwsException() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.processRepayment(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Pinjaman tidak ditemukan");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when loan status is CLOSED")
        void processRepayment_loanClosed_throwsException() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");

            LoanApplication loan = new LoanApplication();
            loan.setStatus(LoanStatus.CLOSED.getValue());

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));

            assertThatThrownBy(() -> paymentService.processRepayment(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Pinjaman ini sudah lunas");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when loan status is not DISBURSED")
        void processRepayment_loanNotDisbursed_throwsException() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");

            LoanApplication loan = new LoanApplication();
            loan.setStatus("PENDING");

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));

            assertThatThrownBy(() -> paymentService.processRepayment(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Pembayaran hanya dapat dilakukan untuk pinjaman yang sudah dicairkan (DISBURSED)");
        }

        @Test
        @DisplayName("Should process partial payment and keep loan status DISBURSED")
        void processRepayment_partialPayment_success() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");
            request.setAmountPaid(new BigDecimal("500000"));
            request.setPaymentMethod("VA_BCA");

            Customer customer = new Customer();
            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setStatus(LoanStatus.DISBURSED.getValue());
            loan.setCustomer(customer);
            loan.setTotalRepayment(new BigDecimal("1000000"));

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));
            given(paymentRepository.save(any(PaymentTransaction.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(paymentRepository.sumAmountPaidByLoanId(loanId)).willReturn(new BigDecimal("500000"));

            PaymentTransaction result = paymentService.processRepayment(request);

            assertThat(result).isNotNull();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.DISBURSED.getValue());
            verify(customerRepository, never()).save(any());
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("Should process full payment, close loan, and upgrade customer credit tier")
        void processRepayment_fullPayment_closesLoanAndUpgradesTier() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");
            request.setAmountPaid(new BigDecimal("1000000"));

            Customer customer = new Customer();
            customer.setSuccessfulLoansCount(1);

            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setStatus(LoanStatus.DISBURSED.getValue());
            loan.setCustomer(customer);
            loan.setTotalRepayment(new BigDecimal("1000000"));

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));
            given(paymentRepository.save(any(PaymentTransaction.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(paymentRepository.sumAmountPaidByLoanId(loanId)).willReturn(new BigDecimal("1000000"));

            PaymentTransaction result = paymentService.processRepayment(request);

            assertThat(result).isNotNull();
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED.getValue());
            assertThat(customer.getSuccessfulLoansCount()).isEqualTo(2);
            assertThat(customer.getCreditTier()).isNotNull();

            verify(customerRepository).save(customer);
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("Should use amountRequested if totalRepayment is null when checking full payment")
        void processRepayment_totalRepaymentNull_usesAmountRequested() {
            UUID loanId = UUID.randomUUID();
            RepaymentRequest request = new RepaymentRequest();
            request.setLoanId(loanId);
            request.setTransactionReference("REF-123");
            request.setAmountPaid(new BigDecimal("1000000"));

            Customer customer = new Customer();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setStatus(LoanStatus.DISBURSED.getValue());
            loan.setCustomer(customer);
            loan.setTotalRepayment(null);
            loan.setAmountRequested(new BigDecimal("1000000"));

            given(paymentRepository.existsByTransactionReference("REF-123")).willReturn(false);
            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));
            given(paymentRepository.save(any(PaymentTransaction.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(paymentRepository.sumAmountPaidByLoanId(loanId)).willReturn(new BigDecimal("1000000"));

            paymentService.processRepayment(request);

            assertThat(loan.getStatus()).isEqualTo(LoanStatus.CLOSED.getValue());
            assertThat(customer.getSuccessfulLoansCount()).isEqualTo(1);
            verify(customerRepository).save(customer);
        }
    }

    @Nested
    @DisplayName("getLoanBalance Tests")
    class GetLoanBalanceTests {

        @Test
        @DisplayName("Should return accurate balance response")
        void getLoanBalance_success() {
            UUID loanId = UUID.randomUUID();
            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setApplicationNumber("APP-001");
            loan.setTotalRepayment(new BigDecimal("2000000"));
            loan.setMonthlyInstallment(new BigDecimal("500000"));
            loan.setTenorMonths(4);
            loan.setStatus(LoanStatus.DISBURSED.getValue());

            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));
            given(paymentRepository.sumAmountPaidByLoanId(loanId)).willReturn(new BigDecimal("500000"));

            LoanBalanceResponse balance = paymentService.getLoanBalance(loanId);

            assertThat(balance).isNotNull();
            assertThat(balance.getLoanId()).isEqualTo(loanId);
            assertThat(balance.getTotalPaidSoFar()).isEqualTo(new BigDecimal("500000"));
            assertThat(balance.getRemainingBalance()).isEqualTo(new BigDecimal("1500000"));
            assertThat(balance.isFullyPaid()).isFalse();
        }

        @Test
        @DisplayName("Should cap remaining balance to zero if total paid exceeds total payable")
        void getLoanBalance_overpaid_remainingBalanceIsZero() {
            UUID loanId = UUID.randomUUID();
            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setTotalRepayment(new BigDecimal("1000000"));
            loan.setStatus(LoanStatus.CLOSED.getValue());

            given(loanRepository.findById(loanId)).willReturn(Optional.of(loan));
            given(paymentRepository.sumAmountPaidByLoanId(loanId)).willReturn(new BigDecimal("1200000"));

            LoanBalanceResponse balance = paymentService.getLoanBalance(loanId);

            assertThat(balance.getRemainingBalance()).isEqualTo(BigDecimal.ZERO);
            assertThat(balance.isFullyPaid()).isTrue();
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when loan does not exist")
        void getLoanBalance_loanNotFound_throwsException() {
            UUID loanId = UUID.randomUUID();
            given(loanRepository.findById(loanId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getLoanBalance(loanId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Pinjaman tidak ditemukan");
        }
    }

    @Nested
    @DisplayName("getPaymentHistoryByCustomer Tests")
    class GetPaymentHistoryByCustomerTests {

        @Test
        @DisplayName("Should return mapped payment history list for customer")
        void getPaymentHistoryByCustomer_success() {
            UUID customerId = UUID.randomUUID();
            UUID loanId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            // 1. Inisialisasi object Customer yang kurang
            Customer customer = new Customer();
            customer.setId(customerId);

            LoanApplication loan = new LoanApplication();
            loan.setId(loanId);
            loan.setApplicationNumber("APP-999");

            // 2. Sekarang 'customer' sudah terdefinisi
            PaymentTransaction tx = PaymentTransaction.builder()
                    .loan(loan)
                    .customer(customer) // Variable customer sekarang sudah dikenali
                    .amountPaid(new BigDecimal("250000"))
                    .paymentMethod("BANK_TRANSFER")
                    .transactionReference("TX-100")
                    .paymentDate(LocalDateTime.now())
                    .status("SUCCESS")
                    .build();

            // Jika id tidak ada di builder (karena inheritance), set via setter:
            tx.setId(paymentId);

            given(paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId)).willReturn(List.of(tx));

            List<PaymentHistoryResponse> history = paymentService.getPaymentHistoryByCustomer(customerId);

            assertThat(history).hasSize(1);
            PaymentHistoryResponse item = history.get(0);
            assertThat(item.getPaymentId()).isEqualTo(paymentId);
            assertThat(item.getLoanId()).isEqualTo(loanId);
            assertThat(item.getApplicationNumber()).isEqualTo("APP-999");
            assertThat(item.getAmountPaid()).isEqualTo(new BigDecimal("250000"));
            assertThat(item.getTransactionReference()).isEqualTo("TX-100");
        }
    }
}