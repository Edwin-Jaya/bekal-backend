package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.service.HomeService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final CustomerRepository customerRepository;

    @Override
    public HomeDashboardResponse getDashboardData(UUID customerId) {
        if (customerId == null) {
            return HomeDashboardResponse.builder()
                    .loanStatus("PRE_APPLICATION")
                    .creditTier(CreditTier.TIER_1.name())
                    .maxLimit(CreditTier.TIER_1.getMaxCap())
                    .availableLimit(CreditTier.TIER_1.getMaxCap())
                    .activeBillAmount(BigDecimal.ZERO)
                    .successfulLoansCount(0)
                    .requiredForNextTier(CreditTier.TIER_2.getMinSuccessfulLoansRequired())
                    .dueDate(null)
                    .build();
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        CreditTier currentTier = customer.getCreditTier() != null ? customer.getCreditTier() : CreditTier.TIER_1;
        BigDecimal maxLimit = currentTier.getMaxCap();

        // Hitung active bill (dapat disesuaikan dengan query LoanApplication jika ada)
        BigDecimal activeBillAmount = BigDecimal.ZERO;
        BigDecimal availableLimit = maxLimit.subtract(activeBillAmount);

        // Menentukan sisa pelunasan untuk naik tier
        int successfulCount = customer.getSuccessfulLoansCount() != null ? customer.getSuccessfulLoansCount() : 0;
        int nextTierRequirement = calculateRequiredForNextTier(currentTier, successfulCount);

        return HomeDashboardResponse.builder()
                .loanStatus("PRE_APPLICATION")
                .creditTier(currentTier.name())
                .maxLimit(maxLimit)
                .availableLimit(availableLimit)
                .activeBillAmount(activeBillAmount)
                .successfulLoansCount(successfulCount)
                .requiredForNextTier(nextTierRequirement)
                .dueDate(null)
                .build();
    }

    private int calculateRequiredForNextTier(CreditTier currentTier, int currentSuccessCount) {
        if (currentTier == CreditTier.TIER_3) {
            return 0; // Sudah di tier tertinggi
        }
        if (currentTier == CreditTier.TIER_1) {
            return Math.max(0, CreditTier.TIER_2.getMinSuccessfulLoansRequired() - currentSuccessCount);
        }
        return Math.max(0, CreditTier.TIER_3.getMinSuccessfulLoansRequired() - currentSuccessCount);
    }
}