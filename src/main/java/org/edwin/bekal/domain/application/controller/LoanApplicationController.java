package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.*;
import org.edwin.bekal.domain.application.service.LoanApplicationService;
import org.edwin.bekal.domain.application.service.LoanDisbursementService;
import org.edwin.bekal.domain.application.service.impl.LoanReviewDetailServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Applications", description = "Endpoints for submitting, managing, and tracking loan applications and workflow queues")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class LoanApplicationController {
    private final LoanApplicationService loanApplicationService;
    private final LoanReviewDetailServiceImpl loanReviewDetailService;
    private final LoanDisbursementService loanDisbursementService;

    @Operation(summary = "Get Customer Loan Applications", description = "Retrieves loan applications belonging to a specific customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer applications retrieved")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CacheablePage<LoanApplicationResponse>> getLoanApplicationByCustomer(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                loanApplicationService.getLoanApplicationByCustomer(customerId, page, size)
        );
    }

    @Operation(summary = "Create Loan Application", description = "Submits a new loan application including loan amount, tenor, and asset/collateral info.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or insufficient plafond")
    })
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(
            @RequestBody CreateLoanApplicationRequest request) {
        return ResponseEntity.ok(loanApplicationService.createLoanApplication(request));
    }

    @Operation(summary = "Get All Loan Applications", description = "Retrieves a paginated list of all loan applications across the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<LoanApplicationResponse>> getLoanApplication(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getLoanApplication(page, size));
    }

    @Operation(summary = "Get Pending Loan Reviews Queue", description = "Retrieves applications awaiting Marketing/Credit Review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending review queue retrieved")
    })
    @GetMapping("/pending-reviews")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplication(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplication(page, size));
    }

    @Operation(summary = "Get Pending Approvals Queue", description = "Retrieves applications awaiting Branch Manager approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending approvals queue retrieved")
    })
    @GetMapping("/pending-approvals")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplicationApproval(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplicationApproval(page, size));
    }

    @Operation(summary = "Get Pending Disbursements Queue", description = "Retrieves approved applications ready for loan fund disbursement.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending disbursements queue retrieved")
    })
    @GetMapping("/pending-disbursement")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplicationDisbursement(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplicationDisbursement(page, size));
    }

    @Operation(summary = "Get Application Review Detail", description = "Retrieves comprehensive application detail including customer profile, documents, and credit scoring.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detail retrieved"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}/detail")
    public ResponseEntity<LoanReviewDetail> getDetail(
            @Parameter(description = "Loan Application UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanReviewDetailService.getDetail(id));
    }

    @Operation(summary = "Get Application Detail for Approval", description = "Retrieves application detail with review recommendations for Branch Manager decision.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approval detail retrieved"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}/detail-approval")
    public ResponseEntity<LoanReviewDetail> getDetailApproval(
            @Parameter(description = "Loan Application UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanReviewDetailService.getDetail(id));
    }

    @Operation(summary = "Get Application Detail for Disbursement", description = "Retrieves application details and bank account information for disbursement execution.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disbursement detail retrieved"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}/detail-disbursement")
    public ResponseEntity<LoanReviewDetail> getDetailDisbursement(
            @Parameter(description = "Loan Application UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        return ResponseEntity.ok(loanDisbursementService.getDetailDisbursement(id));
    }
}
