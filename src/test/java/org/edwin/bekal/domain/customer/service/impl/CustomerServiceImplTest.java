package org.edwin.bekal.domain.customer.service.impl;

import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.auth.dto.UserCheckResponse;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.enums.CustomerGender;
import org.edwin.bekal.enums.CustomerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private EmploymentRepository employmentRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Nested
    @DisplayName("registerCustomer Tests")
    class RegisterCustomerTests {

        @Test
        @DisplayName("Should register customer and employment successfully")
        void registerCustomer_withEmployment_success() {
            RegisterCustomerRequest request = new RegisterCustomerRequest();
            request.setEmail("new@example.com");
            request.setNik("1234567890123456");
            request.setFullName("John Register");
            request.setPassword("secret");
            request.setDateOfBirth(LocalDate.of(1995, 5, 20));
            request.setGender("FEMALE");
            request.setCompanyName("PT Bekal Jaya");

            given(customerRepository.existsByCustomerEmail("new@example.com")).willReturn(false);
            given(customerRepository.existsByCustomerNik("1234567890123456")).willReturn(false);
            given(passwordEncoder.encode("secret")).willReturn("encodedSecret");

            given(customerRepository.save(any(Customer.class))).willAnswer(inv -> {
                Customer c = inv.getArgument(0);
                c.setId(UUID.randomUUID());
                return c;
            });

            CustomerResponse response = customerService.registerCustomer(request);

            assertThat(response).isNotNull();
            assertThat(response.getCustomerFullName()).isEqualTo("John Register");
            assertThat(response.getCustomerGender()).isEqualTo("WANITA");
            verify(employmentRepository).save(any(Employment.class));
        }

        @Test
        @DisplayName("Should throw exception when email already registered")
        void registerCustomer_duplicateEmail_throwsException() {
            RegisterCustomerRequest request = new RegisterCustomerRequest();
            request.setEmail("duplicate@example.com");

            given(customerRepository.existsByCustomerEmail("duplicate@example.com")).willReturn(true);

            assertThatThrownBy(() -> customerService.registerCustomer(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email sudah terdaftar");
        }

        @Test
        @DisplayName("Should throw exception when NIK already registered")
        void registerCustomer_duplicateNik_throwsException() {
            RegisterCustomerRequest request = new RegisterCustomerRequest();
            request.setEmail("new@example.com");
            request.setNik("1234567890123456");

            given(customerRepository.existsByCustomerEmail("new@example.com")).willReturn(false);
            given(customerRepository.existsByCustomerNik("1234567890123456")).willReturn(true);

            assertThatThrownBy(() -> customerService.registerCustomer(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("NIK sudah terdaftar");
        }
    }

    @Nested
    @DisplayName("loginCustomer Tests")
    class LoginCustomerTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void loginCustomer_success() {
            CustomerLoginRequest request = new CustomerLoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("password123");
            request.setDeviceToken("device-xyz");

            Customer customer = new Customer();
            customer.setCustomerEmail("user@example.com");
            customer.setCustomerPasswordHash("encodedHash");
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            given(customerRepository.findByCustomerEmail("user@example.com")).willReturn(Optional.of(customer));
            given(passwordEncoder.matches("password123", "encodedHash")).willReturn(true);
            given(tokenProvider.generateTokenForCustomer(customer)).willReturn("jwt-token");

            JwtResponse response = customerService.loginCustomer(request);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(customer.getCustomerDeviceToken()).isEqualTo("device-xyz");
            assertThat(customer.getCustomerLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when customer account is inactive")
        void loginCustomer_inactive_throwsException() {
            CustomerLoginRequest request = new CustomerLoginRequest();
            request.setEmail("inactive@example.com");

            Customer customer = new Customer();
            customer.setCustomerStatus(CustomerStatus.INACTIVE);

            given(customerRepository.findByCustomerEmail("inactive@example.com")).willReturn(Optional.of(customer));

            assertThatThrownBy(() -> customerService.loginCustomer(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Akun pelanggan tidak aktif");
        }

        @Test
        @DisplayName("Should throw exception when password does not match")
        void loginCustomer_wrongPassword_throwsException() {
            CustomerLoginRequest request = new CustomerLoginRequest();
            request.setEmail("user@example.com");
            request.setPassword("wrongPassword");

            Customer customer = new Customer();
            customer.setCustomerPasswordHash("encodedHash");
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            given(customerRepository.findByCustomerEmail("user@example.com")).willReturn(Optional.of(customer));
            given(passwordEncoder.matches("wrongPassword", "encodedHash")).willReturn(false);

            assertThatThrownBy(() -> customerService.loginCustomer(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email atau kata sandi salah");
        }
    }

    @Nested
    @DisplayName("getCurrentCustomer Tests")
    class GetCurrentCustomerTests {

        @Test
        @DisplayName("Should return customer profile for valid email")
        void getCurrentCustomer_success() {
            Customer customer = new Customer();
            customer.setCustomerEmail("active@example.com");
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            given(customerRepository.findByCustomerEmail("active@example.com")).willReturn(Optional.of(customer));

            CustomerResponse response = customerService.getCurrentCustomer("active@example.com");

            assertThat(response.getCustomerEmail()).isEqualTo("active@example.com");
        }

        @Test
        @DisplayName("Should throw exception if customer status is inactive")
        void getCurrentCustomer_inactive_throwsException() {
            Customer customer = new Customer();
            customer.setCustomerStatus(CustomerStatus.INACTIVE);

            given(customerRepository.findByCustomerEmail("inactive@example.com")).willReturn(Optional.of(customer));

            assertThatThrownBy(() -> customerService.getCurrentCustomer("inactive@example.com"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Akun pelanggan tidak aktif");
        }
    }

    @Nested
    @DisplayName("updateCustomer Tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer fields when validated")
        void updateCustomer_success() {
            UUID id = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setId(id);
            customer.setCustomerEmail("old@example.com");
            customer.setCustomerNik("1111111111111111");

            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.setCustomerEmail("new@example.com");
            request.setCustomerFullName("Updated Name");

            given(customerRepository.findById(id)).willReturn(Optional.of(customer));
            given(customerRepository.existsByCustomerEmail("new@example.com")).willReturn(false);

            CustomerResponse response = customerService.updateCustomer(id, request);

            assertThat(customer.getCustomerEmail()).isEqualTo("new@example.com");
            assertThat(customer.getCustomerFullName()).isEqualTo("Updated Name");
            assertThat(response.getCustomerFullName()).isEqualTo("Updated Name");
        }
    }

    @Nested
    @DisplayName("deleteCustomer Tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should soft delete customer by setting status to INACTIVE")
        void deleteCustomer_success() {
            UUID id = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setCustomerStatus(CustomerStatus.ACTIVE);

            given(customerRepository.findById(id)).willReturn(Optional.of(customer));

            customerService.deleteCustomer(id);

            assertThat(customer.getCustomerStatus()).isEqualTo(CustomerStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("checkCustomerByEmail and Query Tests")
    class QueryTests {

        @Test
        @DisplayName("checkCustomerByEmail - Should return true when email exists")
        void checkCustomerByEmail_exists() {
            given(customerRepository.existsByCustomerEmail("exists@example.com")).willReturn(true);

            UserCheckResponse response = customerService.checkCustomerByEmail("exists@example.com");

            assertThat(response.isExists()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Email sudah terdaftar");
        }

        @Test
        @DisplayName("getAllCustomer - Should return all customers mapped to response")
        void getAllCustomer_success() {
            Customer c1 = new Customer();
            c1.setCustomerFullName("Customer One");
            Customer c2 = new Customer();
            c2.setCustomerFullName("Customer Two");

            given(customerRepository.findAll()).willReturn(List.of(c1, c2));

            List<CustomerResponse> result = customerService.getAllCustomer();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCustomerFullName()).isEqualTo("Customer One");
        }
    }
}