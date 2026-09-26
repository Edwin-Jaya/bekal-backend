package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.CreateRoleMenuAccessRequest;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.application.service.RoleMenuAccessService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role-menu-access")
@Tag(name = "Role Menu Access", description = "Endpoints for managing role-to-menu permissions and authorization matrix")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class RoleMenuAccessController {

    private final RoleMenuAccessService roleMenuAccessService;

    public RoleMenuAccessController(RoleMenuAccessService roleMenuAccessService) {
        this.roleMenuAccessService = roleMenuAccessService;
    }

    @Operation(summary = "Get Role Menu Access (Paginated)", description = "Retrieves all role menu access mappings paginated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role menu mappings retrieved")
    })
    @GetMapping
    public ResponseEntity<Page<RoleMenuAccessResponse>> getAccess(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(roleMenuAccessService.getAccess(page, size));
    }

    @Operation(summary = "Get Active Menus for Roles", description = "Fetches active navigation menu items for a list of assigned role IDs (used by UI sidebar).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active menus retrieved successfully")
    })
    @GetMapping("/active-menus")
    public ResponseEntity<List<RoleMenuAccess>> getActiveMenusByRoleIds(
            @Parameter(description = "List of Role UUIDs")
            @RequestParam List<UUID> roleIds) {
        List<RoleMenuAccess> activeMenus = roleMenuAccessService.getActiveMenusByRoleIds(roleIds);
        return ResponseEntity.ok(activeMenus);
    }

    @Operation(summary = "Check Role Menu Permission", description = "Checks whether a specific role has permission to access a specific menu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission check completed")
    })
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAccess(
            @Parameter(description = "Role UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID roleId,
            @Parameter(description = "Menu UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID menuId
    ) {
        boolean exists = roleMenuAccessService.existsByRoleIdAndMenuId(roleId, menuId);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Delete Menu Access by Role ID", description = "Removes all menu access permissions configured for a role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Access mappings removed")
    })
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity<Void> deleteByRoleId(
            @Parameter(description = "Role UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID roleId) {
        roleMenuAccessService.deleteByRoleId(roleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create Role Menu Access Mapping", description = "Adds a single role-to-menu permission mapping.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mapping saved")
    })
    @PostMapping
    public ResponseEntity<RoleMenuAccess> create(@RequestBody RoleMenuAccess roleMenuAccess) {
        RoleMenuAccess saved = roleMenuAccessService.save(roleMenuAccess);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Get Permission Matrix by Role ID", description = "Retrieves paginated menu access matrix for a specific role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matrix retrieved")
    })
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Page<RoleMenuAccessResponse>> getMatrixByRole(
            @Parameter(description = "Role UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID roleId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RoleMenuAccessResponse> response = roleMenuAccessService.getMatrixByRoleId(roleId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign/Sync Menu Permissions to Role", description = "Bulk assigns or synchronizes menu permissions for a role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions assigned successfully")
    })
    @PostMapping("/assign/{roleId}")
    public ResponseEntity<List<RoleMenuAccessResponse>> assignMenusToRole(
            @Parameter(description = "Role UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID roleId,
            @RequestBody List<CreateRoleMenuAccessRequest> requests
    ) {
        List<RoleMenuAccessResponse> response = roleMenuAccessService.assignMenusToRole(roleId, requests);
        return ResponseEntity.ok(response);
    }
}
