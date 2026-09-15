package org.edwin.bekal.domain.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.ProcessRepaymentRequest;
import org.edwin.bekal.domain.application.service.LoanLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final LoanLifecycleService loanLifecycleService;

    @PostMapping("/repay")
    public ResponseEntity<Map<String, Object>> processRepayment(@Valid @RequestBody ProcessRepaymentRequest request) {
        loanLifecycleService.processFinalRepayment(request.getCustomerId(), request.getLoanApplicationId());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Pelunasan berhasil diproses dan tier nasabah diperbarui otomatis."
        ));
    }
}
