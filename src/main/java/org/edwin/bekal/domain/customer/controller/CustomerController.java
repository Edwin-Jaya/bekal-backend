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
import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.auth.dto.UserCheckResponse;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Endpoints for customer registration, authentication, profile inspection, and lifecycle operations")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Check Customer Existence", description = "Checks whether a customer account with the specified email already exists.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Check completed successfully")
    })
    @GetMapping("/check")
    public ResponseEntity<UserCheckResponse> checkUserByEmail(
            @Parameter(description = "Customer email address to verify", example = "customer@example.com")
            @RequestParam("email") String email
    ) {
        UserCheckResponse response = customerService.checkCustomerByEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Customer Direct Login", description = "Authenticates a customer and returns a JWT access token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> customerLogin(@Valid @RequestBody CustomerLoginRequest request) {
        JwtResponse response = customerService.loginCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(
            summary = "Get Current Customer Profile",
            description = "Retrieves the profile of the currently authenticated customer based on JWT token subject.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid or expired token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - customer role required")
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentCustomer(Authentication authentication) {
        String email = authentication.getName(); // Mengambil email dari subject JWT
        CustomerResponse response = customerService.getCurrentCustomer(email);
        return ResponseEntity.ok(ApiResponse.success("Successfully fetched customer profile", response));
    }

    @Operation(
            summary = "Create Customer (Admin)",
            description = "Creates a new customer record administratively.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate data")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request){
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Created Successfully", response));
    }

    @Operation(
            summary = "Get All Customers",
            description = "Retrieves the list of all registered customers.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customers fetched successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomer(){
        List<CustomerResponse> responses = customerService.getAllCustomer();
        return ResponseEntity.ok(ApiResponse.success("Customer Fetched Successfully", responses));
    }

    @Operation(
            summary = "Update Customer",
            description = "Updates specific details of an existing customer by ID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer Updated Successfully", response));
    }

    @Operation(
            summary = "Delete Customer",
            description = "Deletes a customer account by UUID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> deleteCustomer(
            @Parameter(description = "Customer UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ){
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Register New Customer", description = "Registers a new customer account in the system.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate email/phone/NIK")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Registered Successfully", response));
    }
}
