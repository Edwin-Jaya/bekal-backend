package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.service.PlafondEngineService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlafondEngineServiceImpl implements PlafondEngineService {

    @Override
    public BigDecimal calculateApprovedPlafond(Customer customer, BigDecimal requestedAmount, BigDecimal dsrCapacity) {
        BigDecimal currentTierCap = customer.getCreditTier().getMaxCap();

        // Rumus Dual-Cap: MIN(Requested, DSR Capacity, Tier Cap)
        return requestedAmount
                .min(dsrCapacity)
                .min(currentTierCap);
    }
}
