package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.application.repository.PlafondRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlafondServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PlafondRepository plafondRepository;

    @InjectMocks
    private PlafondServiceImpl plafondService;

    @Nested
    @DisplayName("getActivePlafond Tests")
    class GetActivePlafondTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when customer does not exist")
        void getActivePlafond_customerNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> plafondService.getActivePlafond(customerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Customer tidak ditemukan");
        }

        @Test
        @DisplayName("Should create default plafond when no active plafond exists for customer")
        void getActivePlafond_noExistingPlafond_createsDefaultPlafond() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setCreditTier(CreditTier.TIER_2);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(plafondRepository.findFirstByCustomer_IdAndStatus(customerId, "ACTIVE")).willReturn(Optional.empty());
            given(plafondRepository.save(any(Plafond.class))).willAnswer(inv -> inv.getArgument(0));

            PlafondResponse response = plafondService.getActivePlafond(customerId);

            assertThat(response).isNotNull();
            assertThat(response.getPlafondAmount()).isEqualByComparingTo(CreditTier.TIER_2.getMaxCap());
            assertThat(response.getUsedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getAvailableAmount()).isEqualByComparingTo(CreditTier.TIER_2.getMaxCap());
            assertThat(response.getCreditTier()).isEqualTo("TIER_2");
            assertThat(response.getStatus()).isEqualTo("ACTIVE");

            verify(plafondRepository).save(any(Plafond.class));
        }

        @Test
        @DisplayName("Should update plafondAmount if it differs from customer's current CreditTier max cap")
        void getActivePlafond_tierUpdated_updatesPlafondAmount() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setCreditTier(CreditTier.TIER_3);

            Plafond existingPlafond = new Plafond();
            existingPlafond.setCustomer(customer);
            existingPlafond.setPlafondAmount(CreditTier.TIER_1.getMaxCap()); // Outdated amount
            existingPlafond.setUsedAmount(new BigDecimal("1000000"));
            existingPlafond.setStatus("ACTIVE");

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(plafondRepository.findFirstByCustomer_IdAndStatus(customerId, "ACTIVE")).willReturn(Optional.of(existingPlafond));
            given(plafondRepository.save(existingPlafond)).willReturn(existingPlafond);

            PlafondResponse response = plafondService.getActivePlafond(customerId);

            assertThat(response.getPlafondAmount()).isEqualByComparingTo(CreditTier.TIER_3.getMaxCap());
            verify(plafondRepository).save(existingPlafond);
        }

        @Test
        @DisplayName("Should cap available amount at ZERO if usedAmount exceeds plafondAmount")
        void getActivePlafond_overusedAmount_availableAmountCappedAtZero() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setCreditTier(CreditTier.TIER_1);

            Plafond existingPlafond = new Plafond();
            existingPlafond.setCustomer(customer);
            existingPlafond.setPlafondAmount(CreditTier.TIER_1.getMaxCap());
            existingPlafond.setUsedAmount(CreditTier.TIER_1.getMaxCap().add(new BigDecimal("500000")));
            existingPlafond.setStatus("ACTIVE");

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(plafondRepository.findFirstByCustomer_IdAndStatus(customerId, "ACTIVE")).willReturn(Optional.of(existingPlafond));

            PlafondResponse response = plafondService.getActivePlafond(customerId);

            assertThat(response.getAvailableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should fallback to TIER_1 when customer creditTier is null")
        void getActivePlafond_nullCreditTier_fallsBackToTier1() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setCreditTier(null);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(plafondRepository.findFirstByCustomer_IdAndStatus(customerId, "ACTIVE")).willReturn(Optional.empty());
            given(plafondRepository.save(any(Plafond.class))).willAnswer(inv -> inv.getArgument(0));

            PlafondResponse response = plafondService.getActivePlafond(customerId);

            assertThat(response.getCreditTier()).isEqualTo(CreditTier.TIER_1.name());
        }
    }
}