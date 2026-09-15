package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.service.DsrCalculatorService;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DsrCalculatorServiceImpl implements DsrCalculatorService {

    private final EmploymentRepository employmentRepository;
    private static final BigDecimal DSR_LIMIT_PERCENTAGE = new BigDecimal("0.30"); // 30% DSR Cap

    public BigDecimal calculateDsrCapacity(UUID customerId) {
        Employment employment = employmentRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Data pekerjaan belum diisi"));

        BigDecimal income = employment.getCustomerVerifiedIncome() != null
                ? employment.getCustomerVerifiedIncome()
                : employment.getCustomerDeclaredIncome();

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Kapasitas Cicilan Maksimal Bulanan = Gaji x 30%
        return income.multiply(DSR_LIMIT_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);
    }
}
