package org.edwin.bekal.domain.application.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.LoanHistoryResponse;
import org.edwin.bekal.domain.application.dto.LoanReviewResponse;
import org.edwin.bekal.domain.application.dto.SubmitReviewRequest;
import org.edwin.bekal.domain.application.service.LoanReviewDetailService;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/loan-reviews")
@RequiredArgsConstructor
public class LoanReviewController {
    private final LoanReviewDetailService loanReviewService;
    private final InternalUserRepository internalUserRepository;
    private final LoanReviewDetailService loanReviewDetailService;

    @PostMapping("/submit")
    public ResponseEntity<LoanReviewResponse> submitReview(
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser // Ambil ID reviewer dari Security Context
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanReviewResponse response = loanReviewService.submitReview(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Page<LoanHistoryResponse>> getMarketingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanReviewDetailService.getApplicationReviewHistory(page, size));
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
         return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                 .orElseThrow(() -> new EntityNotFoundException("User not found"))
                 .getId();
    }
}