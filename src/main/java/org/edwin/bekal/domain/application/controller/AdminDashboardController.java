package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.AdminDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.service.impl.AdminDashboardServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints for administrator analytics, KPIs, portfolio summaries, and recent activity monitoring")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminDashboardController {
    private final AdminDashboardServiceImpl adminDashboardServiceimpl;

    @Operation(summary = "Get Dashboard Overview", description = "Provides high-level dashboard metrics, total disbursed loans, pending queues, and active customers count.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard overview metrics retrieved")
    })
    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardResponse> getOverview() {
        return ResponseEntity.ok(adminDashboardServiceimpl.getDashboardOverview());
    }

    @Operation(summary = "Get Recent Dashboard Activities", description = "Retrieves a paginated stream of recent loan application activities and status changes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent activities retrieved")
    })
    @GetMapping("/recent-activities")
    public ResponseEntity<Page<LoanApplication>> getRecentActivities(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminDashboardServiceimpl.getRecentActivities(page, size));
    }
}
