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
import org.edwin.bekal.domain.master.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Menus", description = "Endpoints for SUPER_ADMIN to manage dynamic navigation menus and route hierarchies")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "Create Menu", description = "Creates a new navigation menu item. Requires SUPER_ADMIN role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Menu created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody CreateMenuRequest request){
        MenuResponse response = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu Created Successfully", response));
    }

    @Operation(summary = "Update Menu", description = "Updates an existing navigation menu item by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Menu updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Menu not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @Parameter(description = "Menu UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMenuRequest request){
        MenuResponse response = menuService.updateMenu(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu Updated Successfully", response));
    }

    @Operation(summary = "Delete Menu", description = "Deletes a menu item by UUID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Menu deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Menu not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> deleteMenu(
            @Parameter(description = "Menu UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id){
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Menus (Paginated)", description = "Retrieves a paginated list of menu items, filterable by active status.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Menus retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - SUPER_ADMIN role required")
    })
    @GetMapping
    public ResponseEntity<CacheablePage<MenuResponse>> getMenu(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by status (true/false)")
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(menuService.getMenu(page, size, status));
    }
}
