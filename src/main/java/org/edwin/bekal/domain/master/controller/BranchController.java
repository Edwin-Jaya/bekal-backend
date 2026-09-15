package org.edwin.bekal.domain.master.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.service.BranchService;
import org.edwin.bekal.enums.BranchStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;
    private final BranchRepository branchRepository;

    @GetMapping("/active")
    public ResponseEntity<List<BranchResponse>> getActiveBranches() {
        List<BranchResponse> branches = branchRepository.findByBranchStatus(BranchStatus.ACTIVE)
                .stream()
                .map(b -> BranchResponse.builder()
                        .id(b.getId())
                        .branchCode(b.getBranchCode())
                        .branchName(b.getBranchName())
                        .branchCity(b.getBranchCity())
                        .build())
                .toList();
        return ResponseEntity.ok(branches);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request){
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches(){
        List<BranchResponse> responses = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches Fetched Successfully", responses));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(@PathVariable UUID id, @Valid @RequestBody UpdateBranchRequest request){
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> deleteBranch(@PathVariable UUID id){
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
