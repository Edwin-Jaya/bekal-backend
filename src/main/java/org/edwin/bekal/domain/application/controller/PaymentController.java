package org.edwin.bekal.domain.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @PostMapping("/repay")
    public ResponseEntity<LoanBalanceResponse> repayLoan(@Valid @RequestBody RepaymentRequest request) {
        PaymentTransaction payment = paymentService.processRepayment(request);
        LoanBalanceResponse updatedBalance = paymentService.getLoanBalance(payment.getLoan().getId());
        return ResponseEntity.ok(updatedBalance);
    }

    @GetMapping("/balance/{loanId}")
    public ResponseEntity<LoanBalanceResponse> getLoanBalance(@PathVariable UUID loanId) {
        LoanBalanceResponse balance = paymentService.getLoanBalance(loanId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/history/customer/{customerId}")
    public ResponseEntity<List<PaymentHistoryResponse>> getCustomerPaymentHistory(@PathVariable UUID customerId) {
        List<PaymentHistoryResponse> history = paymentService.getPaymentHistoryByCustomer(customerId);
        return ResponseEntity.ok(history);
    }
}