package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.auth.dto.UserCheckResponse;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.customer.service.CustomerService;
import org.edwin.bekal.enums.CustomerGender;
import org.edwin.bekal.enums.CustomerStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmploymentRepository employmentRepository;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {
        validateUniqueEmail(request.getEmail());
        validateUniqueNik(request.getNik());

        Customer customer = new Customer();
        customer.setCustomerFullName(request.getFullName());
        customer.setCustomerEmail(request.getEmail());
        customer.setCustomerPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setCustomerPhoneNumber(request.getPhoneNumber());
        customer.setCustomerNik(request.getNik());

        if (request.getDateOfBirth() != null) {
            customer.setCustomerDateOfBirth(java.sql.Date.valueOf(request.getDateOfBirth()));
        }
        customer.setCustomerAddress(request.getAddress());
        customer.setCustomerGender(parseGender(request.getGender()));
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            Employment employment = new Employment();
            employment.setCustomer(savedCustomer);
            employment.setCustomerEmploymentType(request.getEmploymentType());
            employment.setCustomerCompanyName(request.getCompanyName());
            employment.setCustomerJobTitle(request.getJobTitle());
            employment.setCustomerIndustry(request.getIndustry());
            employment.setCustomerDeclaredIncome(request.getDeclaredIncome());
            employment.setCustomerOtherIncome(request.getOtherIncome());

            if (request.getEmploymentStartDate() != null) {
                employment.setCustomerEmploymentStartDate(java.sql.Date.valueOf(request.getEmploymentStartDate()));
            }
            employment.setCustomerIsCurrent(true);
            employmentRepository.save(employment);
        }

        return mapToResponse(savedCustomer);
    }

    @Override
    @Transactional
    public JwtResponse loginCustomer(CustomerLoginRequest request) {
        Customer customer = customerRepository.findByCustomerEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email atau kata sandi salah"));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Akun pelanggan tidak aktif");
        }

        if (!passwordEncoder.matches(request.getPassword(), customer.getCustomerPasswordHash())) {
            throw new IllegalArgumentException("Email atau kata sandi salah");
        }

        if (request.getDeviceToken() != null) {
            customer.setCustomerDeviceToken(request.getDeviceToken());
        }
        customer.setCustomerLastLoginAt(Instant.now());

        String jwt = tokenProvider.generateTokenForCustomer(customer);
        return new JwtResponse(jwt, "Bearer");
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomer(String email) {
        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Pelanggan tidak ditemukan"));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Akun pelanggan tidak aktif");
        }

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pelanggan tidak ditemukan dengan ID: " + id));

        // 1. Validasi Unik terlebih dahulu sebelum mengubah state entity
        boolean isEmailChanged = request.getCustomerEmail() != null
                && !request.getCustomerEmail().isBlank()
                && !customer.getCustomerEmail().equalsIgnoreCase(request.getCustomerEmail());

        if (isEmailChanged) {
            validateUniqueEmail(request.getCustomerEmail());
        }

        boolean isNikChanged = request.getCustomerNik() != null
                && !request.getCustomerNik().isBlank()
                && !customer.getCustomerNik().equalsIgnoreCase(request.getCustomerNik());

        if (isNikChanged) {
            validateUniqueNik(request.getCustomerNik());
        }

        // 2. Terapkan perubahan entity setelah semua query validasi selesai
        if (isEmailChanged) {
            customer.setCustomerEmail(request.getCustomerEmail());
        }
        if (isNikChanged) {
            customer.setCustomerNik(request.getCustomerNik());
        }
        if (request.getCustomerFullName() != null && !request.getCustomerFullName().isBlank()) {
            customer.setCustomerFullName(request.getCustomerFullName());
        }
        if (request.getCustomerAddress() != null && !request.getCustomerAddress().isBlank()) {
            customer.setCustomerAddress(request.getCustomerAddress());
        }
        if (request.getCustomerDateOfBirth() != null) {
            customer.setCustomerDateOfBirth(request.getCustomerDateOfBirth());
        }
        if (request.getCustomerPhoneNumber() != null && !request.getCustomerPhoneNumber().isBlank()) {
            customer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
        }
        if (request.getCustomerGender() != null && !request.getCustomerGender().isBlank()) {
            customer.setCustomerGender(parseGender(request.getCustomerGender()));
        }

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pelanggan tidak ditemukan dengan ID: " + id));

        customer.setCustomerStatus(CustomerStatus.INACTIVE);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        validateUniqueEmail(request.getCustomerEmail());
        validateUniqueNik(request.getCustomerNik());

        Customer customer = new Customer();
        customer.setCustomerFullName(request.getCustomerFullName());
        customer.setCustomerAddress(request.getCustomerAddress());
        customer.setCustomerEmail(request.getCustomerEmail());
        customer.setCustomerNik(request.getCustomerNik());
        customer.setCustomerDateOfBirth(request.getCustomerDateOfBirth());
        customer.setCustomerGender(parseGender(request.getCustomerGender()));
        customer.setCustomerPasswordHash(passwordEncoder.encode(request.getCustomerPasswordHash()));
        customer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCheckResponse checkCustomerByEmail(String email) {
        boolean exists = customerRepository.existsByCustomerEmail(email);
        String message = exists ? "Email sudah terdaftar" : "Email tersedia";
        return new UserCheckResponse(exists, message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomer() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerFullName(customer.getCustomerFullName())
                .customerAddress(customer.getCustomerAddress())
                .customerNik(customer.getCustomerNik())
                .customerEmail(customer.getCustomerEmail())
                .customerDateOfBirth(customer.getCustomerDateOfBirth())
                .customerGender(customer.getCustomerGender() != null ? customer.getCustomerGender().name() : null)
                .customerPhoneNumber(customer.getCustomerPhoneNumber())
                .customerStatus(customer.getCustomerStatus() != null ? customer.getCustomerStatus().name() : null)
                .customerPasswordHash(customer.getCustomerPasswordHash())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    // --- Private Helper Methods ---

    private void validateUniqueEmail(String email) {
        if (customerRepository.existsByCustomerEmail(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar: " + email);
        }
    }

    private void validateUniqueNik(String nik) {
        if (customerRepository.existsByCustomerNik(nik)) {
            throw new IllegalArgumentException("NIK sudah terdaftar: " + nik);
        }
    }

    private CustomerGender parseGender(String genderInput) {
        if (genderInput == null || genderInput.isBlank()) {
            return CustomerGender.PRIA;
        }
        String normalized = genderInput.trim().toUpperCase();
        if (normalized.equals("FEMALE") || normalized.equals("WANITA")) {
            return CustomerGender.WANITA;
        }
        return CustomerGender.PRIA;
    }
}