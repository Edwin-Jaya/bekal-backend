package org.edwin.bekal.domain.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.auth.dto.JwtResponse;
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
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> customerLogin(@Valid @RequestBody CustomerLoginRequest request) {
        JwtResponse response = customerService.loginCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentCustomer(Authentication authentication) {
        String email = authentication.getName(); // Mengambil email dari subject JWT
        CustomerResponse response = customerService.getCurrentCustomer(email);
        return ResponseEntity.ok(ApiResponse.success("Successfully fetched customer profile", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request){
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomer(){
        List<CustomerResponse> responses = customerService.getAllCustomer();
        return ResponseEntity.ok(ApiResponse.success("Customer Fetched Successfully", responses));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request){
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> deleteCustomer(@PathVariable UUID id){
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerResponse>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Registered Successfully", response));
    }
}
