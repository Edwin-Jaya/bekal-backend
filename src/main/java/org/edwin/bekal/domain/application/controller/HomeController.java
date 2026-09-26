package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.service.HomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Customer Home", description = "Endpoints for customer home screen dashboard, active financing summary, and promo banners")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "Get Customer Home Dashboard Data", description = "Retrieves personalized home screen data for customer app including active loan, quick actions, and plafond.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>> getDashboard(
            @Parameter(description = "Customer UUID (optional, inferred from token context if omitted)", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID customerId) {
        HomeDashboardResponse response = homeService.getDashboardData(customerId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched successfully", response));
    }
}
