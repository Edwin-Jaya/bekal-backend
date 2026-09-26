package org.edwin.bekal.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.customer.dto.CreateEmploymentRequest;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateEmploymentRequest;
import org.edwin.bekal.domain.customer.service.EmploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employment")
@RequiredArgsConstructor
@Tag(name = "Customer Employment", description = "Endpoints for customer employment profile, employer data, and monthly income details")
public class EmploymentController {

    private final EmploymentService employmentService;

    @Operation(
            summary = "Create Employment Info",
            description = "Creates employment and income records for a customer.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employment record created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<EmploymentResponse>> createEmployment(@Valid @RequestBody CreateEmploymentRequest request){
        EmploymentResponse response = employmentService.createEmployment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment Created Successfully", response));
    }

    @Operation(
            summary = "Get All Employment Records",
            description = "Fetches all customer employment records.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employment records retrieved")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmploymentResponse>>> getAllEmployment(){
        List<EmploymentResponse> responses = employmentService.getAllEmployment();
        return ResponseEntity.ok(ApiResponse.success("Employment Fetched Successfully", responses));
    }

    @Operation(
            summary = "Update Employment Info",
            description = "Updates employment information for a customer.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employment updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employment record not found")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<EmploymentResponse>> updateEmployment(
            @Parameter(description = "Employment UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmploymentRequest request
    ){
        EmploymentResponse response = employmentService.updateEmployment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment Updated Successfully", response));
    }

    @Operation(
            summary = "Delete Employment Record",
            description = "Deletes an employment record by UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Employment deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employment record not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<EmploymentResponse>> deleteEmployment(
            @Parameter(description = "Employment UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ){
        employmentService.deleteEmployment(id);
        return ResponseEntity.noContent().build();
    }
}
