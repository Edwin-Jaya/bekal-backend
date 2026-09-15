package org.edwin.bekal.domain.application.controller;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApprovalService;
import org.edwin.bekal.domain.application.service.LoanDisbursementService;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-disbursement")
public class LoanDisbursementController {
    private final LoanApprovalService loanApprovalService;
    private final LoanDisbursementService loanDisbursementService;
    private final InternalUserRepository internalUserRepository;

    public LoanDisbursementController(LoanApprovalService loanApprovalService, LoanDisbursementService loanDisbursementService, InternalUserRepository internalUserRepository) {
        this.loanApprovalService = loanApprovalService;
        this.loanDisbursementService = loanDisbursementService;
        this.internalUserRepository = internalUserRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity<LoanDisbursementResponse> submitDisbursement(
            @Valid @RequestBody SubmitDisbursementRequest request,
            @AuthenticationPrincipal UserDetails currentUser // Ambil ID reviewer dari Security Context
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanDisbursementResponse response = loanDisbursementService.submitDisbursement(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
        return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }

    @GetMapping("/history")
    public ResponseEntity<Page<LoanDisbursementHistoryResponse>> getBackOfficeHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanDisbursementService.getApplicationDisbursementHistory(page, size));
    }
}
