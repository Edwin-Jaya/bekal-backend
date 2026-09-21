package org.edwin.bekal.domain.customer.service;

import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.auth.dto.UserCheckResponse;
import org.edwin.bekal.domain.customer.dto.*;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    List<CustomerResponse> getAllCustomer();
    CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request);
    void deleteCustomer(UUID id);
    CustomerResponse getCurrentCustomer(String email);
    // Method baru untuk login customer mobile
    JwtResponse loginCustomer(CustomerLoginRequest request);
    CustomerResponse registerCustomer(RegisterCustomerRequest request);
    UserCheckResponse checkCustomerByEmail(String email);
}
