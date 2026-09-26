package org.edwin.bekal.domain.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.OpenApiConfig;
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
@Tag(name = "Bank Accounts", description = "Endpoints for customer bank account management, disbursement destination, and verification")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Operation(
            summary = "Create Bank Account",
            description = "Registers a new bank account for a customer.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Bank account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid bank code")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> createBankAccount(@Valid @RequestBody CreateBankAccountRequest request) {
        BankAccountResponse response = bankAccountService.createBankAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank Account Created Successfully", response));
    }

    @Operation(
            summary = "Get All Bank Accounts",
            description = "Retrieves all registered bank accounts.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bank accounts retrieved")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getAllBankAccounts() {
        List<BankAccountResponse> responses = bankAccountService.getAllBankAccounts();
        return ResponseEntity.ok(ApiResponse.success("Bank Accounts Fetched Successfully", responses));
    }

    @Operation(
            summary = "Get Bank Account by ID",
            description = "Retrieves bank account details by account UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bank account retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountById(
            @Parameter(description = "Bank Account UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        BankAccountResponse response = bankAccountService.getBankAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Bank Account Fetched Successfully", response));
    }

    @Operation(
            summary = "Get Bank Account by Customer ID",
            description = "Retrieves active bank accounts associated with a customer UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bank account retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer bank account not found")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountByCustomerId(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId
    ) {
        BankAccountResponse response = bankAccountService.getBankAccountByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Bank Account Fetched Successfully", response));
    }

    @Operation(
            summary = "Update Bank Account",
            description = "Updates bank account details such as account number, bank name, or holder name.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Bank account updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> updateBankAccount(
            @Parameter(description = "Bank Account UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBankAccountRequest request
    ) {
        BankAccountResponse response = bankAccountService.updateBankAccount(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bank Account Updated Successfully", response));
    }

    @Operation(
            summary = "Delete Bank Account",
            description = "Deletes a bank account record by UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Bank account deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bank account not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> deleteBankAccount(
            @Parameter(description = "Bank Account UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        bankAccountService.deleteBankAccount(id);
        return ResponseEntity.noContent().build();
    }
}
