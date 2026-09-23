package org.edwin.bekal.domain.application.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlafondResponse implements Serializable {
    UUID id;
    BigDecimal plafondAmount;
    BigDecimal usedAmount;
    BigDecimal availableAmount;
    BigDecimal interestRate;
    Integer maxTenorMonths;
    String status;
    String creditTier;
    LocalDate validFrom;
    LocalDate validUntil;
}