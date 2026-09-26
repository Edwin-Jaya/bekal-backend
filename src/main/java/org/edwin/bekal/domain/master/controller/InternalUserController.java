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
import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.service.InternalUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal-user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Internal Users", description = "Endpoints for SUPER_ADMIN to manage internal employee accounts, credentials, and assigned roles")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class InternalUserController {

    private final InternalUserService internalUserService;

    @Operation(summary = "Create Internal User", description = "Creates a new internal employee (BM, BO, Marketing, Super Admin). Requires SUPER_ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate username/email"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<InternalUserResponse>> createInternalUser(@Valid @RequestBody CreateInternalUserRequest request){
        InternalUserResponse response = internalUserService.createInternalUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User Created Successfully", response));
    }

    @Operation(summary = "Get Internal Users (Paginated)", description = "Retrieves a paginated list of internal employee users, filterable by active status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Internal users retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping
    public ResponseEntity<CacheablePage<InternalUserResponse>> getInternalUsers(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by active status (true/false)")
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(internalUserService.getInternalUser(page, size, status));
    }

    @Operation(summary = "Update Internal User", description = "Updates details, branch assignment, or role of an internal employee.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> updateInternalUser(
            @Parameter(description = "Internal User UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInternalUserRequest request){
        InternalUserResponse response = internalUserService.updateInternalUser(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User Updated Successfully", response));
    }

    @Operation(summary = "Delete Internal User", description = "Deactivates or deletes an internal employee user by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> deleteInternalUser(
            @Parameter(description = "Internal User UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id){
        internalUserService.deleteInternalUser(id);
        return ResponseEntity.noContent().build();
    }
}
