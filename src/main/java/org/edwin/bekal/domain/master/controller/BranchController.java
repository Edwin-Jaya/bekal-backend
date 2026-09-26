package org.edwin.bekal.domain.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.service.BranchService;
import org.edwin.bekal.enums.BranchStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Endpoints for managing BCA Finance branch offices and operating locations")
public class BranchController {

    private final BranchService branchService;
    private final BranchRepository branchRepository;

    @Operation(summary = "Get Active Branches", description = "Retrieves a public list of all currently active company branch offices.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active branches retrieved")
    })
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

    @Operation(
            summary = "Create Branch",
            description = "Registers a new branch office into the system.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Branch created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate branch code")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody CreateBranchRequest request){
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch Created Successfully", response));
    }

    @Operation(
            summary = "Get All Branches",
            description = "Retrieves all branches including inactive branches.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Branches fetched successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getAllBranches(){
        List<BranchResponse> responses = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches Fetched Successfully", responses));
    }

    @Operation(
            summary = "Update Branch",
            description = "Updates details and status of an existing branch office.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Branch updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @Parameter(description = "Branch UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBranchRequest request
    ){
        BranchResponse response = branchService.updateBranch(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch Updated Successfully", response));
    }

    @Operation(
            summary = "Delete Branch",
            description = "Deletes a branch office by UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Branch deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Branch not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> deleteBranch(
            @Parameter(description = "Branch UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ){
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
