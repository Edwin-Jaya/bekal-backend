package org.edwin.bekal.domain.application.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PlafondResponse implements Serializable {
    UUID id;
    BigDecimal plafondAmount;
    BigDecimal usedAmount;
    BigDecimal availableAmount;
    BigDecimal interestRate;
    Integer maxTenorMonths;
    String status;
    LocalDate validFrom;
    LocalDate validUntil;
}