package org.edwin.bekal.domain.application.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface DsrCalculatorService {
    BigDecimal calculateDsrCapacity(UUID customerId);
}
