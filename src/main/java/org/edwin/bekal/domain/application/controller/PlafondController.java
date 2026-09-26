package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.service.PlafondService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plafonds")
@RequiredArgsConstructor
@Tag(name = "Credit Plafond", description = "Endpoints for customer credit limit and plafond inquiries")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class PlafondController {

    private final PlafondService plafondService;

    @Operation(summary = "Get Active Customer Plafond", description = "Retrieves current active credit plafond limit and available balance for a customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active plafond retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer or plafond not found")
    })
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<PlafondResponse> getActivePlafond(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(plafondService.getActivePlafond(customerId));
    }
}