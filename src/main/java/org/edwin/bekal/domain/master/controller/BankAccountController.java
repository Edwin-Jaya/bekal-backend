package org.edwin.bekal.domain.master.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.BankAccountResponse;
import org.edwin.bekal.domain.master.dto.CreateBankAccountRequest;
import org.edwin.bekal.domain.master.dto.UpdateBankAccountRequest;
import org.edwin.bekal.domain.master.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> createBankAccount(@Valid @RequestBody CreateBankAccountRequest request) {
        BankAccountResponse response = bankAccountService.createBankAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank Account Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getAllBankAccounts() {
        List<BankAccountResponse> responses = bankAccountService.getAllBankAccounts();
        return ResponseEntity.ok(ApiResponse.success("Bank Accounts Fetched Successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountById(@PathVariable UUID id) {
        BankAccountResponse response = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Bank Account Fetched Successfully", response));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountByCustomerId(@PathVariable UUID customerId) {
        BankAccountResponse response = bankAccountService.getBankAccountByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Bank Account Fetched Successfully", response));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateBankAccount(@PathVariable UUID id, @Valid @RequestBody UpdateBankAccountRequest request) {
        BankAccountResponse response = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank Account Updated Successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> deleteBankAccount(@PathVariable UUID id) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }
}
