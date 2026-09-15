package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.customer.entity.Customer;

import java.math.BigDecimal;

public interface PlafondEngineService {
    BigDecimal calculateApprovedPlafond(Customer customer, BigDecimal requestedAmount, BigDecimal dsrCapacity);
}
