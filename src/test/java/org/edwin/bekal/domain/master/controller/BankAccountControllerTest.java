package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.BankAccountResponse;
import org.edwin.bekal.domain.master.dto.CreateBankAccountRequest;
import org.edwin.bekal.domain.master.dto.UpdateBankAccountRequest;
import org.edwin.bekal.domain.master.service.BankAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    @Nested
    @DisplayName("createBankAccount Tests")
    class CreateBankAccountTests {

        @Test
        @DisplayName("Should create bank account successfully and return CREATED")
        void createBankAccount_success() {
            CreateBankAccountRequest request = new CreateBankAccountRequest();
            BankAccountResponse responseDto = BankAccountResponse.builder()
                    .bankName("BCA")
                    .bankAccountNumber("123456")
                    .build();

            given(bankAccountService.createBankAccount(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BankAccountResponse>> response = bankAccountController.createBankAccount(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bank Account Created Successfully");
            assertThat(response.getBody().getData().getBankName()).isEqualTo("BCA");
            assertThat(response.getBody().getData().getBankAccountNumber()).isEqualTo("123456");
        }
    }

    @Nested
    @DisplayName("getBankAccounts Tests")
    class GetBankAccountsTests {

        @Test
        @DisplayName("Should return all bank accounts successfully")
        void getAllBankAccounts_success() {
            List<BankAccountResponse> list = List.of(
                    BankAccountResponse.builder().bankName("BCA").bankAccountNumber("11111").build(),
                    BankAccountResponse.builder().bankName("Mandiri").bankAccountNumber("22222").build()
            );

            given(bankAccountService.getAllBankAccounts()).willReturn(list);

            ResponseEntity<ApiResponse<List<BankAccountResponse>>> response = bankAccountController.getAllBankAccounts();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bank Accounts Fetched Successfully");
            assertThat(response.getBody().getData()).hasSize(2);
        }

        @Test
        @DisplayName("Should return bank account by ID successfully")
        void getBankAccountById_success() {
            UUID id = UUID.randomUUID();
            BankAccountResponse responseDto = BankAccountResponse.builder()
                    .id(id)
                    .bankName("BCA")
                    .bankAccountNumber("123456")
                    .build();

            given(bankAccountService.getBankAccountById(id)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BankAccountResponse>> response = bankAccountController.getBankAccountById(id);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bank Account Fetched Successfully");
            assertThat(response.getBody().getData().getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should return bank account by customer ID successfully")
        void getBankAccountByCustomerId_success() {
            UUID customerId = UUID.randomUUID();
            BankAccountResponse responseDto = BankAccountResponse.builder()
                    .bankName("BCA")
                    .bankAccountNumber("123456")
                    .build();

            given(bankAccountService.getBankAccountByCustomerId(customerId)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BankAccountResponse>> response = bankAccountController.getBankAccountByCustomerId(customerId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bank Account Fetched Successfully");
            assertThat(response.getBody().getData().getBankName()).isEqualTo("BCA");
        }
    }

    @Nested
    @DisplayName("updateBankAccount Tests")
    class UpdateBankAccountTests {

        @Test
        @DisplayName("Should update bank account successfully and return CREATED")
        void updateBankAccount_success() {
            UUID id = UUID.randomUUID();
            UpdateBankAccountRequest request = new UpdateBankAccountRequest();
            BankAccountResponse responseDto = BankAccountResponse.builder()
                    .id(id)
                    .bankName("BNI")
                    .bankAccountNumber("654321")
                    .build();

            given(bankAccountService.updateBankAccount(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BankAccountResponse>> response = bankAccountController.updateBankAccount(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Bank Account Updated Successfully");
            assertThat(response.getBody().getData().getBankName()).isEqualTo("BNI");
        }
    }

    @Nested
    @DisplayName("deleteBankAccount Tests")
    class DeleteBankAccountTests {

        @Test
        @DisplayName("Should delete bank account successfully and return NO_CONTENT")
        void deleteBankAccount_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<BankAccountResponse>> response = bankAccountController.deleteBankAccount(id);

            verify(bankAccountService).deleteBankAccount(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}