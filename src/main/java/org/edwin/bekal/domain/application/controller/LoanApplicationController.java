package org.edwin.bekal.domain.application.controller;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.CreateLoanApplicationRequest;
import org.edwin.bekal.domain.application.dto.LoanApplicationResponse;
import org.edwin.bekal.domain.application.dto.LoanReviewDetail;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
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
public class LoanApplicationController {
    private final LoanApplicationService loanApplicationService;
    private final LoanReviewDetailServiceImpl loanReviewDetailService;
    private final LoanDisbursementService loanDisbursementService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<LoanApplicationResponse>> getLoanApplicationByCustomer(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getLoanApplicationByCustomer(customerId, page, size));
    }

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(
            @RequestBody CreateLoanApplicationRequest request) {
        return ResponseEntity.ok(loanApplicationService.createLoanApplication(request));
    }

    @GetMapping
    public ResponseEntity<Page<LoanApplicationResponse>> getLoanApplication(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getLoanApplication(page, size));
    }

    @GetMapping("/pending-reviews")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplication(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplication(page, size));
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplicationApproval(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplicationApproval(page, size));
    }

    @GetMapping("/pending-disbursement")
    public ResponseEntity<Page<LoanApplicationResponse>> getPendingLoanApplicationDisbursement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(loanApplicationService.getPendingLoanApplicationDisbursement(page, size));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<LoanReviewDetail> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(loanReviewDetailService.getDetail(id));
    }

    @GetMapping("/{id}/detail-approval")
    public ResponseEntity<LoanReviewDetail> getDetailApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(loanReviewDetailService.getDetail(id));
    }

    @GetMapping("/{id}/detail-disbursement")
    public ResponseEntity<LoanReviewDetail> getDetailDisbursement(@PathVariable UUID id) {
        return ResponseEntity.ok(loanDisbursementService.getDetailDisbursement(id));
    }
}
