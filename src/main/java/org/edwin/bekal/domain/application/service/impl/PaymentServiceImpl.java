package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.LoanBalanceResponse;
import org.edwin.bekal.domain.application.dto.PaymentHistoryResponse;
import org.edwin.bekal.domain.application.dto.RepaymentRequest;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.entity.PaymentTransaction;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PaymentTransactionRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.edwin.bekal.enums.LoanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final LoanApplicationRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final PaymentTransactionRepository paymentRepository;

    @Transactional
    public PaymentTransaction processRepayment(RepaymentRequest request) {
        // 1. Idempotency Check via Payment Gateway Reference
        if (paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            throw new IllegalStateException("Transaksi dengan referensi ini sudah pernah diproses");
        }

        // 2. Fetch Loan Application
        LoanApplication loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new EntityNotFoundException("Pinjaman tidak ditemukan"));

        // Validasi Status Pinjaman menggunakan Enum LoanStatus
        if (LoanStatus.CLOSED.getValue().equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Pinjaman ini sudah lunas");
        }

        if (!LoanStatus.DISBURSED.getValue().equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Pembayaran hanya dapat dilakukan untuk pinjaman yang sudah dicairkan (DISBURSED)");
        }

        Customer customer = loan.getCustomer();

        // 3. Save Payment History Log (Auditing)
        PaymentTransaction payment = PaymentTransaction.builder()
                .loan(loan)
                .customer(customer)
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .paymentDate(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        PaymentTransaction savedPayment = paymentRepository.save(payment);

        // 4. Hitung Akumulasi Total Pembayaran yang Sudah Masuk
        BigDecimal totalPaidSoFar = paymentRepository.sumAmountPaidByLoanId(loan.getId());
        BigDecimal totalPayable = loan.getTotalRepayment() != null
                ? loan.getTotalRepayment()
                : loan.getAmountRequested();

        boolean isFullyPaid = totalPaidSoFar.compareTo(totalPayable) >= 0;

        // 5. Update Status Pinjaman & Upgrade Tier Hanya Jika Lunas
        if (isFullyPaid) {
            loan.setStatus(LoanStatus.CLOSED.getValue());

            // Increment count & auto-upgrade CreditTier
            int newLoanCount = (customer.getSuccessfulLoansCount() != null ? customer.getSuccessfulLoansCount() : 0) + 1;
            customer.setSuccessfulLoansCount(newLoanCount);
            customer.setCreditTier(CreditTier.resolveTier(newLoanCount));
            customerRepository.save(customer);
        } else {
            loan.setStatus(LoanStatus.DISBURSED.getValue());
        }

        loanRepository.save(loan);

        return savedPayment;
    }

    @Transactional(readOnly = true)
    public LoanBalanceResponse getLoanBalance(UUID loanId) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Pinjaman tidak ditemukan"));

        BigDecimal totalPaidSoFar = paymentRepository.sumAmountPaidByLoanId(loanId);
        BigDecimal totalPayable = loan.getTotalRepayment() != null
                ? loan.getTotalRepayment()
                : loan.getAmountRequested();

        BigDecimal remainingBalance = totalPayable.subtract(totalPaidSoFar);
        if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
            remainingBalance = BigDecimal.ZERO;
        }

        return LoanBalanceResponse.builder()
                .loanId(loan.getId())
                .applicationNumber(loan.getApplicationNumber())
                .totalRepayment(totalPayable)
                .totalPaidSoFar(totalPaidSoFar)
                .remainingBalance(remainingBalance)
                .monthlyInstallment(loan.getMonthlyInstallment())
                .tenorMonths(loan.getTenorMonths())
                .status(loan.getStatus())
                .isFullyPaid(LoanStatus.CLOSED.getValue().equalsIgnoreCase(loan.getStatus()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistoryByCustomer(UUID customerId) {
        List<PaymentTransaction> transactions = paymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);

        return transactions.stream()
                .map(tx -> PaymentHistoryResponse.builder()
                        .paymentId(tx.getId())
                        .loanId(tx.getLoan().getId())
                        .applicationNumber(tx.getLoan().getApplicationNumber())
                        .amountPaid(tx.getAmountPaid())
                        .paymentMethod(tx.getPaymentMethod())
                        .transactionReference(tx.getTransactionReference())
                        .paymentDate(tx.getPaymentDate())
                        .status(tx.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}