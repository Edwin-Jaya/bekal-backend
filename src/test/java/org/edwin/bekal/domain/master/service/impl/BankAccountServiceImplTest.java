package org.edwin.bekal.domain.master.service.impl;

import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.master.dto.BankAccountResponse;
import org.edwin.bekal.domain.master.dto.CreateBankAccountRequest;
import org.edwin.bekal.domain.master.dto.UpdateBankAccountRequest;
import org.edwin.bekal.domain.master.entity.BankAccount;
import org.edwin.bekal.domain.master.repository.BankAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Nested
    @DisplayName("createBankAccount Tests")
    class CreateBankAccountTests {

        @Test
        @DisplayName("Should create bank account successfully when customer exists")
        void createBankAccount_success() {
            UUID customerId = UUID.randomUUID();
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setCustomerId(customerId);
            request.setBankName("BCA");
            request.setBankAccountNumber("123456789");
            request.setBankAccountHolder("Edwin");
            request.setIsPrimary(true);

            Customer customer = new Customer();
            customer.setId(customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(bankAccountRepository.save(any(BankAccount.class))).willAnswer(inv -> {
                BankAccount acc = inv.getArgument(0);
                acc.setId(UUID.randomUUID());
                return acc;
            });

            BankAccountResponse response = bankAccountService.createBankAccount(request);

            assertThat(response).isNotNull();
            assertThat(response.getBankName()).isEqualTo("BCA");
            assertThat(response.getBankAccountNumber()).isEqualTo("123456789");
            assertThat(response.getIsPrimary()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when customer not found during creation")
        void createBankAccount_customerNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            request.setCustomerId(customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.createBankAccount(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Customer not found with id: " + customerId);
        }
    }

    @Nested
    @DisplayName("getAllBankAccounts Tests")
    class GetAllBankAccountsTests {

        @Test
        @DisplayName("Should return all bank accounts successfully")
        void getAllBankAccounts_success() {
            BankAccount acc1 = new BankAccount();
            acc1.setBankName("BCA");
            BankAccount acc2 = new BankAccount();
            acc2.setBankName("Mandiri");

            given(bankAccountRepository.findAll()).willReturn(List.of(acc1, acc2));

            List<BankAccountResponse> results = bankAccountService.getAllBankAccounts();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBankName()).isEqualTo("BCA");
            assertThat(results.get(1).getBankName()).isEqualTo("Mandiri");
        }
    }

    @Nested
    @DisplayName("getBankAccountById Tests")
    class GetBankAccountByIdTests {

        @Test
        @DisplayName("Should return bank account when ID exists")
        void getBankAccountById_success() {
            UUID id = UUID.randomUUID();
            BankAccount acc = new BankAccount();
            acc.setId(id);
            acc.setBankName("BCA");

            given(bankAccountRepository.findById(id)).willReturn(acc);

            BankAccountResponse response = bankAccountService.getBankAccountById(id);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getBankName()).isEqualTo("BCA");
        }

        @Test
        @DisplayName("Should throw exception when bank account ID does not exist")
        void getBankAccountById_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            given(bankAccountRepository.findById(id)).willReturn(null);

            assertThatThrownBy(() -> bankAccountService.getBankAccountById(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bank account not found with id: " + id);
        }
    }

    @Nested
    @DisplayName("getBankAccountByCustomerId Tests")
    class GetBankAccountByCustomerIdTests {

        @Test
        @DisplayName("Should return bank account when customer ID exists")
        void getBankAccountByCustomerId_success() {
            UUID customerId = UUID.randomUUID();
            BankAccount acc = new BankAccount();
            acc.setBankName("BCA");

            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.of(acc));

            BankAccountResponse response = bankAccountService.getBankAccountByCustomerId(customerId);

            assertThat(response).isNotNull();
            assertThat(response.getBankName()).isEqualTo("BCA");
        }

        @Test
        @DisplayName("Should throw exception when customer ID has no bank account")
        void getBankAccountByCustomerId_notFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            given(bankAccountRepository.findByCustomerId(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> bankAccountService.getBankAccountByCustomerId(customerId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bank account not found for customer id: " + customerId);
        }
    }

    @Nested
    @DisplayName("updateBankAccount Tests")
    class UpdateBankAccountTests {

        @Test
        @DisplayName("Should update bank account successfully")
        void updateBankAccount_success() {
            UUID id = UUID.randomUUID();
            UpdateBankAccountRequest request = new UpdateBankAccountRequest();
            request.setBankName("BNI");
            request.setStatus("inactive");

            BankAccount existing = new BankAccount();
            existing.setId(id);
            existing.setBankName("BCA");
            existing.setStatus("active");

            given(bankAccountRepository.findById(id)).willReturn(existing);
            given(bankAccountRepository.save(any(BankAccount.class))).willAnswer(inv -> inv.getArgument(0));

            BankAccountResponse response = bankAccountService.updateBankAccount(id, request);

            assertThat(response).isNotNull();
            assertThat(response.getBankName()).isEqualTo("BNI");
            assertThat(response.getStatus()).isEqualTo("inactive");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent bank account")
        void updateBankAccount_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            UpdateBankAccountRequest request = new UpdateBankAccountRequest();

            given(bankAccountRepository.findById(id)).willReturn(null);

            assertThatThrownBy(() -> bankAccountService.updateBankAccount(id, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bank account not found with id: " + id);
        }
    }

    @Nested
    @DisplayName("deleteBankAccount Tests")
    class DeleteBankAccountTests {

        @Test
        @DisplayName("Should delete bank account successfully")
        void deleteBankAccount_success() {
            UUID id = UUID.randomUUID();
            BankAccount acc = new BankAccount();
            acc.setId(id);

            given(bankAccountRepository.findById(id)).willReturn(acc);

            bankAccountService.deleteBankAccount(id);

            verify(bankAccountRepository).delete(acc);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent bank account")
        void deleteBankAccount_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            given(bankAccountRepository.findById(id)).willReturn(null);

            assertThatThrownBy(() -> bankAccountService.deleteBankAccount(id))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Bank account not found with id: " + id);
        }
    }
}