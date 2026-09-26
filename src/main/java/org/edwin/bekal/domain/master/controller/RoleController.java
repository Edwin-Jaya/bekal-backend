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
import org.edwin.bekal.domain.master.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Roles", description = "Endpoints for managing system user roles and authority levels")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create Role", description = "Defines a new role in the system. Requires SUPER_ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate role name"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request){
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role Created Successfully", response));
    }

    @Operation(summary = "Get All Roles", description = "Fetches a full unpaginated list of all system roles.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRole(){
        List<RoleResponse> responses = roleService.getAllRole();
        return ResponseEntity.ok(ApiResponse.success("Roles Fetched Successfully", responses));
    }

    @Operation(summary = "Get Roles (Paginated)", description = "Retrieves a paginated list of system roles, filterable by status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping
    public ResponseEntity<CacheablePage<RoleResponse>> getRole(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by status (true/false)")
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(roleService.getRole(page, size, status));
    }

    @Operation(summary = "Update Role", description = "Updates the role name or attributes.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "Role UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request){
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role Updated Successfully", response));
    }

    @Operation(summary = "Delete Role", description = "Deletes a role by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Role deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> deleteRole(
            @Parameter(description = "Role UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id){
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
