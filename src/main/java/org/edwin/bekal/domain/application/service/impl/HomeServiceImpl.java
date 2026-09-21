package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.PaymentTransactionRepository;
import org.edwin.bekal.domain.application.service.HomeService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.edwin.bekal.enums.LoanStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final PaymentTransactionRepository trxPaymentRepository;

    @Override
    public HomeDashboardResponse getDashboardData(UUID customerId) {
        if (customerId == null) {
            return buildPreApplicationResponse();
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        CreditTier currentTier = customer.getCreditTier() != null ? customer.getCreditTier() : CreditTier.TIER_1;
        BigDecimal maxLimit = currentTier.getMaxCap();

        Optional<LoanApplication> activeLoanOpt =
                loanApplicationRepository.findTopByCustomer_IdOrderBySubmittedAtDesc(customerId);

        LoanStatus status = activeLoanOpt
                .map(loan -> safeParseStatus(loan.getStatus()))
                .orElse(LoanStatus.CLOSED);

        // ⬇️ deklarasi awal — INI YANG KEMARIN HILANG
        BigDecimal activeBillAmount = BigDecimal.ZERO;
        LocalDate dueDate = null;
        String activeLoanId = null;

        if (status == LoanStatus.DISBURSED && activeLoanOpt.isPresent()) {
            LoanApplication loan = activeLoanOpt.get();
            activeLoanId = loan.getId().toString();

            BigDecimal totalPaid = trxPaymentRepository.sumSuccessfulPaymentsByLoanId(loan.getId());
            long paidInstallments = trxPaymentRepository.countSuccessfulPaymentsByLoanId(loan.getId());

            BigDecimal remainingBalance = loan.getTotalRepayment().subtract(totalPaid);
            if (remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            // Tagihan bulan ini = cicilan bulanan, kecuali sisa utang < 1x cicilan (pelunasan terakhir)
            activeBillAmount = remainingBalance.min(loan.getMonthlyInstallment());

            LocalDate startDate = loan.getSubmittedAt().atZone(ZoneOffset.UTC).toLocalDate();
            dueDate = startDate.plusMonths(paidInstallments + 1);
        }

        BigDecimal availableLimit = maxLimit.subtract(activeBillAmount);

        int successfulCount = customer.getSuccessfulLoansCount() != null ? customer.getSuccessfulLoansCount() : 0;
        int nextTierRequirement = calculateRequiredForNextTier(currentTier, successfulCount);

        return HomeDashboardResponse.builder()
                .loanStatus(status.name())
                .creditTier(currentTier.name())
                .maxLimit(maxLimit)
                .availableLimit(availableLimit)
                .activeBillAmount(activeBillAmount)
                .successfulLoansCount(successfulCount)
                .requiredForNextTier(nextTierRequirement)
                .dueDate(dueDate != null ? LocalDate.parse(dueDate.toString()) : null)
                .activeLoanId(activeLoanId)
                .build();
    }

    private LoanStatus safeParseStatus(String rawStatus) {
        try {
            return LoanStatus.valueOf(rawStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return LoanStatus.IN_REVIEW;
        }
    }

    private HomeDashboardResponse buildPreApplicationResponse() {
        return HomeDashboardResponse.builder()
                .loanStatus(LoanStatus.CLOSED.name())
                .creditTier(CreditTier.TIER_1.name())
                .maxLimit(CreditTier.TIER_1.getMaxCap())
                .availableLimit(CreditTier.TIER_1.getMaxCap())
                .activeBillAmount(BigDecimal.ZERO)
                .successfulLoansCount(0)
                .requiredForNextTier(CreditTier.TIER_2.getMinSuccessfulLoansRequired())
                .dueDate(null)
                .activeLoanId(null)
                .build();
    }

    private int calculateRequiredForNextTier(CreditTier currentTier, int currentSuccessCount) {
        if (currentTier == CreditTier.TIER_3) return 0;
        if (currentTier == CreditTier.TIER_1) {
            return Math.max(0, CreditTier.TIER_2.getMinSuccessfulLoansRequired() - currentSuccessCount);
        }
        return Math.max(0, CreditTier.TIER_3.getMinSuccessfulLoansRequired() - currentSuccessCount);
    }
}