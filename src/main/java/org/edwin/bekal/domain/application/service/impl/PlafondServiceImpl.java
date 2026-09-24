package org.edwin.bekal.domain.application.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.application.repository.PlafondRepository;
import org.edwin.bekal.domain.application.service.PlafondService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlafondServiceImpl implements PlafondService {

    private final CustomerRepository customerRepository;
    private final PlafondRepository plafondRepository;

    @Override
    @Transactional
    public PlafondResponse getActivePlafond(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer tidak ditemukan"));

        CreditTier currentTier = customer.getCreditTier() != null ? customer.getCreditTier() : CreditTier.TIER_1;

        Plafond plafond = plafondRepository
                .findFirstByCustomer_IdAndStatus(customerId, "ACTIVE")
                .orElseGet(() -> createDefaultPlafondForCustomer(customer, currentTier));

        if (plafond.getPlafondAmount().compareTo(currentTier.getMaxCap()) != 0) {
            plafond.setPlafondAmount(currentTier.getMaxCap());
            plafond = plafondRepository.save(plafond);
        }

        if (plafond.getInterestRate() == null || plafond.getInterestRate().compareTo(new BigDecimal("1.0")) != 0) {
            plafond.setInterestRate(new BigDecimal("1.0"));
            plafond = plafondRepository.save(plafond);
        }

        BigDecimal availableAmount = plafond.getPlafondAmount().subtract(plafond.getUsedAmount());
        if (availableAmount.compareTo(BigDecimal.ZERO) < 0) {
            availableAmount = BigDecimal.ZERO;
        }

        System.out.println(currentTier.name());

        return PlafondResponse.builder()
                .id(plafond.getId())
                .plafondAmount(plafond.getPlafondAmount())
                .usedAmount(plafond.getUsedAmount())
                .availableAmount(availableAmount)
                .interestRate(plafond.getInterestRate())
                .maxTenorMonths(plafond.getMaxTenorMonths())
                .status(plafond.getStatus())
                .creditTier(currentTier.name())
                .build();
    }

    private Plafond createDefaultPlafondForCustomer(Customer customer, CreditTier tier) {
        Plafond plafond = new Plafond();
        plafond.setCustomer(customer);
        plafond.setPlafondAmount(tier.getMaxCap());
        plafond.setUsedAmount(BigDecimal.ZERO);
        plafond.setInterestRate(new BigDecimal("1.0"));
        plafond.setMaxTenorMonths(24);
        plafond.setStatus("ACTIVE");

        return plafondRepository.save(plafond);
    }
}