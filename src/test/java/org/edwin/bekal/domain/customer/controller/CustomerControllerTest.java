package org.edwin.bekal.domain.customer.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.auth.dto.UserCheckResponse;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @Nested
    @DisplayName("checkUserByEmail Tests")
    class CheckUserByEmailTests {

        @Test
        @DisplayName("Should return UserCheckResponse")
        void checkUserByEmail_success() {
            String email = "customer@example.com";
            UserCheckResponse expectedResponse = new UserCheckResponse(true, "Email sudah terdaftar");
            given(customerService.checkCustomerByEmail(email)).willReturn(expectedResponse);

            ResponseEntity<UserCheckResponse> response = customerController.checkUserByEmail(email);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isExists()).isTrue();
            assertThat(response.getBody().getMessage()).isEqualTo("Email sudah terdaftar");
        }
    }

    @Nested
    @DisplayName("customerLogin Tests")
    class CustomerLoginTests {

        @Test
        @DisplayName("Should login customer successfully")
        void customerLogin_success() {
            CustomerLoginRequest request = new CustomerLoginRequest();
            request.setEmail("customer@example.com");
            request.setPassword("password123");

            JwtResponse jwtResponse = new JwtResponse("jwt-token", "Bearer");
            given(customerService.loginCustomer(request)).willReturn(jwtResponse);

            ResponseEntity<ApiResponse<JwtResponse>> response = customerController.customerLogin(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getToken()).isEqualTo("jwt-token");
        }
    }

    @Nested
    @DisplayName("getCurrentCustomer Tests")
    class GetCurrentCustomerTests {

        @Test
        @DisplayName("Should return current authenticated customer details")
        void getCurrentCustomer_success() {
            Authentication authentication = mock(Authentication.class);
            given(authentication.getName()).willReturn("customer@example.com");

            CustomerResponse customerResponse = CustomerResponse.builder()
                    .customerEmail("customer@example.com")
                    .customerFullName("John Doe")
                    .build();

            given(customerService.getCurrentCustomer("customer@example.com")).willReturn(customerResponse);

            ResponseEntity<ApiResponse<CustomerResponse>> response = customerController.getCurrentCustomer(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerEmail()).isEqualTo("customer@example.com");
        }
    }

    @Nested
    @DisplayName("CRUD Customer Tests")
    class CrudCustomerTests {

        @Test
        @DisplayName("createCustomer - Should return CREATED status and CustomerResponse")
        void createCustomer_success() {
            CreateCustomerRequest request = new CreateCustomerRequest();
            CustomerResponse customerResponse = CustomerResponse.builder().customerFullName("Jane Doe").build();

            given(customerService.createCustomer(request)).willReturn(customerResponse);

            ResponseEntity<ApiResponse<CustomerResponse>> response = customerController.createCustomer(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerFullName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("getAllCustomer - Should return list of customers")
        void getAllCustomer_success() {
            List<CustomerResponse> responses = List.of(
                    CustomerResponse.builder().customerFullName("User 1").build(),
                    CustomerResponse.builder().customerFullName("User 2").build()
            );
            given(customerService.getAllCustomer()).willReturn(responses);

            ResponseEntity<ApiResponse<List<CustomerResponse>>> response = customerController.getAllCustomer();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(2);
        }

        @Test
        @DisplayName("updateCustomer - Should return updated customer profile")
        void updateCustomer_success() {
            UUID id = UUID.randomUUID();
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            CustomerResponse updatedResponse = CustomerResponse.builder().id(id).customerFullName("Updated Name").build();

            given(customerService.updateCustomer(id, request)).willReturn(updatedResponse);

            ResponseEntity<ApiResponse<CustomerResponse>> response = customerController.updateCustomer(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("deleteCustomer - Should delete customer and return NO_CONTENT")
        void deleteCustomer_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<CustomerResponse>> response = customerController.deleteCustomer(id);

            verify(customerService).deleteCustomer(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("registerCustomer - Should register customer and return CREATED status")
        void registerCustomer_success() {
            RegisterCustomerRequest request = new RegisterCustomerRequest();
            CustomerResponse registeredResponse = CustomerResponse.builder().customerFullName("New Customer").build();

            given(customerService.registerCustomer(request)).willReturn(registeredResponse);

            ResponseEntity<ApiResponse<CustomerResponse>> response = customerController.registerCustomer(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerFullName()).isEqualTo("New Customer");
        }
    }
}