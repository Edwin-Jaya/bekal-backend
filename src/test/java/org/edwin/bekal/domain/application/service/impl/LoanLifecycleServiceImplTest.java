package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanLifecycleServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanApplicationRepository loanRepository;

    @InjectMocks
    private LoanLifecycleServiceImpl loanLifecycleService;

    @Nested
    @DisplayName("processFinalRepayment Tests")
    class ProcessFinalRepaymentTests {

        @Test
        @DisplayName("Should return early without modifying entities when loan status is already CLOSED (Idempotency)")
        void processFinalRepayment_alreadyClosed_returnsEarly() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanApplicationId);
            loan.setStatus("CLOSED");

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.of(loan));

            loanLifecycleService.processFinalRepayment(customerId, loanApplicationId);

            verify(loanRepository).findById(loanApplicationId);
            verifyNoMoreInteractions(loanRepository);
            verifyNoInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should return early when loan status is closed in lower case (Case-Insensitive Idempotency)")
        void processFinalRepayment_alreadyClosedLowerCase_returnsEarly() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanApplicationId);
            loan.setStatus("closed");

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.of(loan));

            loanLifecycleService.processFinalRepayment(customerId, loanApplicationId);

            verify(loanRepository).findById(loanApplicationId);
            verifyNoMoreInteractions(loanRepository);
            verifyNoInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when LoanApplication is not found")
        void processFinalRepayment_loanNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanLifecycleService.processFinalRepayment(customerId, loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Pinjaman tidak ditemukan");

            verify(loanRepository).findById(loanApplicationId);
            verifyNoInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when Customer is not found")
        void processFinalRepayment_customerNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanApplicationId);
            loan.setStatus("ACTIVE");

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.of(loan));
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> loanLifecycleService.processFinalRepayment(customerId, loanApplicationId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Customer tidak ditemukan");

            verify(loanRepository).findById(loanApplicationId);
            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).save(any());
            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update successful loan count from null to 1, resolve credit tier, and set status to CLOSED")
        void processFinalRepayment_nullSuccessfulLoansCount_success() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanApplicationId);
            loan.setStatus("ACTIVE");

            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setSuccessfulLoansCount(null);

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.of(loan));
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            loanLifecycleService.processFinalRepayment(customerId, loanApplicationId);

            assertThat(customer.getSuccessfulLoansCount()).isEqualTo(1);
            assertThat(customer.getCreditTier()).isNotNull();
            assertThat(loan.getStatus()).isEqualTo("CLOSED");

            verify(customerRepository).save(customer);
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("Should increment existing successful loan count, update credit tier, and save changes")
        void processFinalRepayment_existingSuccessfulLoansCount_success() {
            UUID customerId = UUID.randomUUID();
            UUID loanApplicationId = UUID.randomUUID();

            LoanApplication loan = new LoanApplication();
            loan.setId(loanApplicationId);
            loan.setStatus("IN_REPAYMENT");

            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setSuccessfulLoansCount(2);

            given(loanRepository.findById(loanApplicationId)).willReturn(Optional.of(loan));
            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            loanLifecycleService.processFinalRepayment(customerId, loanApplicationId);

            assertThat(customer.getSuccessfulLoansCount()).isEqualTo(3);
            assertThat(customer.getCreditTier()).isNotNull();
            assertThat(loan.getStatus()).isEqualTo("CLOSED");

            verify(customerRepository).save(customer);
            verify(loanRepository).save(loan);
        }
    }
}