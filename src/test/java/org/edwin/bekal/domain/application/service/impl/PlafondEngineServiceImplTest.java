package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.enums.CreditTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PlafondEngineServiceImplTest {

    private final PlafondEngineServiceImpl plafondEngineService = new PlafondEngineServiceImpl();

    @Test
    @DisplayName("Should return requested amount when requested amount is the minimum cap")
    void calculateApprovedPlafond_requestedAmountIsLowest() {
        Customer customer = new Customer();
        customer.setCreditTier(CreditTier.TIER_3); // Misal Max Cap = 10,000,000

        BigDecimal requestedAmount = new BigDecimal("3000000");
        BigDecimal dsrCapacity = new BigDecimal("5000000");

        BigDecimal approved = plafondEngineService.calculateApprovedPlafond(customer, requestedAmount, dsrCapacity);

        assertThat(approved).isEqualByComparingTo(requestedAmount);
    }

    @Test
    @DisplayName("Should return DSR capacity when DSR capacity is the minimum cap")
    void calculateApprovedPlafond_dsrCapacityIsLowest() {
        Customer customer = new Customer();
        customer.setCreditTier(CreditTier.TIER_3);

        BigDecimal requestedAmount = new BigDecimal("8000000");
        BigDecimal dsrCapacity = new BigDecimal("4000000");

        BigDecimal approved = plafondEngineService.calculateApprovedPlafond(customer, requestedAmount, dsrCapacity);

        assertThat(approved).isEqualByComparingTo(dsrCapacity);
    }

    @Test
    @DisplayName("Should return Credit Tier Max Cap when Tier Cap is the minimum cap")
    void calculateApprovedPlafond_tierCapIsLowest() {
        Customer customer = new Customer();
        customer.setCreditTier(CreditTier.TIER_1); // Max Cap = Tier 1 Cap

        BigDecimal tierCap = CreditTier.TIER_1.getMaxCap();
        BigDecimal requestedAmount = tierCap.add(new BigDecimal("2000000"));
        BigDecimal dsrCapacity = tierCap.add(new BigDecimal("3000000"));

        BigDecimal approved = plafondEngineService.calculateApprovedPlafond(customer, requestedAmount, dsrCapacity);

        assertThat(approved).isEqualByComparingTo(tierCap);
    }
}