package org.edwin.bekal.domain.application.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApprovalService;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-approval")
@RequiredArgsConstructor
public class LoanApprovalController {
    private final LoanApprovalService loanApprovalService;
    private final InternalUserRepository internalUserRepository;
    @PostMapping("/submit")
    public ResponseEntity<LoanApprovalResponse> submitReview(
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser // Ambil ID reviewer dari Security Context
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanApprovalResponse response = loanApprovalService.submitApproval(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
        return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }

    @GetMapping("/history")
    public ResponseEntity<Page<LoanHistoryResponse>> getBranchManagerHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanApprovalService.getApplicationApprovalHistory(page, size));
    }

}
