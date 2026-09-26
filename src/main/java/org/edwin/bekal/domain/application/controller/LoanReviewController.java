package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
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
@Tag(name = "Loan Reviews", description = "Endpoints for Marketing & Back Office credit analysis, survey notes, and verification scoring")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class LoanReviewController {
    private final LoanReviewDetailService loanReviewService;
    private final InternalUserRepository internalUserRepository;
    private final LoanReviewDetailService loanReviewDetailService;

    @Operation(summary = "Submit Loan Review", description = "Submits review decision, credit score evaluation, and survey verification details for a loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "404", description = "Application or reviewer not found")
    })
    @PostMapping("/submit")
    public ResponseEntity<LoanReviewResponse> submitReview(
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanReviewResponse response = loanReviewService.submitReview(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Marketing Review History", description = "Retrieves review history records completed by Marketing / Reviewers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History records retrieved successfully")
    })
    @GetMapping("/history")
    public ResponseEntity<Page<LoanHistoryResponse>> getMarketingHistory(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanReviewDetailService.getApplicationReviewHistory(page, size));
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
         return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                 .orElseThrow(() -> new EntityNotFoundException("User not found"))
                 .getId();
    }
}