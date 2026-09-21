package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DsrCalculatorServiceImplTest {

    @Mock
    private EmploymentRepository employmentRepository;

    @InjectMocks
    private DsrCalculatorServiceImpl dsrCalculatorService;

    @Test
    @DisplayName("calculateDsrCapacity - Throws IllegalArgumentException when employment data is not found")
    void calculateDsrCapacity_employmentNotFound_throwsException() {
        UUID customerId = UUID.randomUUID();
        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> dsrCalculatorService.calculateDsrCapacity(customerId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data pekerjaan belum diisi");

        verify(employmentRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("calculateDsrCapacity - Uses verified income when present")
    void calculateDsrCapacity_usesVerifiedIncome_success() {
        UUID customerId = UUID.randomUUID();
        Employment employment = new Employment();
        employment.setCustomerVerifiedIncome(new BigDecimal("10000000"));
        employment.setCustomerDeclaredIncome(new BigDecimal("8000000"));

        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.of(employment));

        BigDecimal result = dsrCalculatorService.calculateDsrCapacity(customerId);

        // 10,000,000 * 0.40 = 4,000,000.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("4000000.00"));
        verify(employmentRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("calculateDsrCapacity - Fallback to declared income when verified income is null")
    void calculateDsrCapacity_usesDeclaredIncomeWhenVerifiedNull_success() {
        UUID customerId = UUID.randomUUID();
        Employment employment = new Employment();
        employment.setCustomerVerifiedIncome(null);
        employment.setCustomerDeclaredIncome(new BigDecimal("5000000"));

        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.of(employment));

        BigDecimal result = dsrCalculatorService.calculateDsrCapacity(customerId);

        // 5,000,000 * 0.40 = 2,000,000.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("2000000.00"));
        verify(employmentRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("calculateDsrCapacity - Returns zero when both verified and declared incomes are null")
    void calculateDsrCapacity_incomeNull_returnsZero() {
        UUID customerId = UUID.randomUUID();
        Employment employment = new Employment();
        employment.setCustomerVerifiedIncome(null);
        employment.setCustomerDeclaredIncome(null);

        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.of(employment));

        BigDecimal result = dsrCalculatorService.calculateDsrCapacity(customerId);

        assertThat(result).isEqualTo(BigDecimal.ZERO);
        verify(employmentRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("calculateDsrCapacity - Returns zero when income is zero or negative")
    void calculateDsrCapacity_incomeZeroOrNegative_returnsZero() {
        UUID customerId = UUID.randomUUID();

        // Income Zero
        Employment zeroIncomeEmployment = new Employment();
        zeroIncomeEmployment.setCustomerVerifiedIncome(BigDecimal.ZERO);

        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.of(zeroIncomeEmployment));

        BigDecimal resultZero = dsrCalculatorService.calculateDsrCapacity(customerId);
        assertThat(resultZero).isEqualTo(BigDecimal.ZERO);

        // Income Negative
        Employment negativeIncomeEmployment = new Employment();
        negativeIncomeEmployment.setCustomerVerifiedIncome(new BigDecimal("-5000000"));

        given(employmentRepository.findByCustomerId(customerId)).willReturn(Optional.of(negativeIncomeEmployment));

        BigDecimal resultNegative = dsrCalculatorService.calculateDsrCapacity(customerId);
        assertThat(resultNegative).isEqualTo(BigDecimal.ZERO);
    }
}