package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.LoanBalanceResponse;
import org.edwin.bekal.domain.application.dto.PaymentHistoryResponse;
import org.edwin.bekal.domain.application.dto.RepaymentRequest;
import org.edwin.bekal.domain.application.entity.PaymentTransaction;
import org.edwin.bekal.domain.application.service.impl.PaymentServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments & Repayments", description = "Endpoints for loan installment payments, loan balance checks, and customer repayment history")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @Operation(summary = "Process Loan Repayment", description = "Executes an installment or full loan repayment and returns the updated loan balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment amount or validation error"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @PostMapping("/repay")
    public ResponseEntity<LoanBalanceResponse> repayLoan(@Valid @RequestBody RepaymentRequest request) {
        PaymentTransaction payment = paymentService.processRepayment(request);
        LoanBalanceResponse updatedBalance = paymentService.getLoanBalance(payment.getLoan().getId());
        return ResponseEntity.ok(updatedBalance);
    }

    @Operation(summary = "Get Loan Balance", description = "Retrieves remaining loan balance, principal, interest, and next due date for a loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @GetMapping("/balance/{loanId}")
    public ResponseEntity<LoanBalanceResponse> getLoanBalance(
            @Parameter(description = "Loan Application UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID loanId) {
        LoanBalanceResponse balance = paymentService.getLoanBalance(loanId);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Get Customer Payment History", description = "Retrieves all completed payment transactions for a specific customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment history retrieved successfully")
    })
    @GetMapping("/history/customer/{customerId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getCustomerPaymentHistory(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId) {
        List<PaymentHistoryResponse> history = paymentService.getPaymentHistoryByCustomer(customerId);
        return ResponseEntity.ok(history);
    }
}