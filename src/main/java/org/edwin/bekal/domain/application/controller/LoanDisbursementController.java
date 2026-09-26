package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.edwin.bekal.config.OpenApiConfig;
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
@Tag(name = "Loan Disbursement", description = "Endpoints for Back Office to disburse approved loan funds to customer accounts")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class LoanDisbursementController {
    private final LoanApprovalService loanApprovalService;
    private final LoanDisbursementService loanDisbursementService;
    private final InternalUserRepository internalUserRepository;

    public LoanDisbursementController(LoanApprovalService loanApprovalService, LoanDisbursementService loanDisbursementService, InternalUserRepository internalUserRepository) {
        this.loanApprovalService = loanApprovalService;
        this.loanDisbursementService = loanDisbursementService;
        this.internalUserRepository = internalUserRepository;
    }

    @Operation(summary = "Submit Loan Disbursement", description = "Disburses the approved loan amount to the customer's verified bank account and updates loan status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disbursement processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid disbursement request"),
            @ApiResponse(responseCode = "404", description = "Application or user not found")
    })
    @PostMapping("/submit")
    public ResponseEntity<LoanDisbursementResponse> submitDisbursement(
            @Valid @RequestBody SubmitDisbursementRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        UUID reviewerUserId = extractUserIdFromDetails(currentUser);
        LoanDisbursementResponse response = loanDisbursementService.submitDisbursement(request, reviewerUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Back Office Disbursement History", description = "Retrieves history of loan disbursements executed by the Back Office.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disbursement history retrieved successfully")
    })
    @GetMapping("/history")
    public ResponseEntity<Page<LoanDisbursementHistoryResponse>> getBackOfficeHistory(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }

        return ResponseEntity.ok(loanDisbursementService.getApplicationDisbursementHistory(page, size));
    }

    private UUID extractUserIdFromDetails(UserDetails userDetails) {
        return internalUserRepository.findByInternalUserEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}
