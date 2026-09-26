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
@Tag(name = "Loan Approvals", description = "Endpoints for Branch Manager (BM) to approve or reject reviewed loan applications")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class LoanApprovalController {
    private final LoanApprovalService loanApprovalService;
    private final InternalUserRepository internalUserRepository;

    @Operation(summary = "Submit Approval Decision", description = "Submits the Branch Manager's final approval or rejection decision for a loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval decision processed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Application or approver not found")
    })
    @PostMapping("/submit")
    public ResponseEntity<LoanApprovalResponse> submitReview(
            @Valid @RequestBody SubmitReviewRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanApprovalResponse response = loanApprovalService.submitApproval(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Branch Manager Approval History", description = "Retrieves the history of loan application decisions made by the Branch Manager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval history retrieved successfully")
    })
    @GetMapping("/history")
    public ResponseEntity<Page<LoanHistoryResponse>> getBranchManagerHistory(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(loanApprovalService.getApplicationApprovalHistory(page, size));
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
        return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}
