package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.security.JwtTokenProvider;
import org.edwin.bekal.domain.auth.dto.JwtResponse;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
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
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {

        // 1. Validasi Unik Email & NIK
        if (customerRepository.existsByCustomerEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }
        if (customerRepository.existsByCustomerNik(request.getNik())) {
            throw new IllegalArgumentException("NIK already exists!");
        }

        // 2. Simpan Customer
        Customer customer = new Customer();
        customer.setCustomerFullName(request.getFullName());
        customer.setCustomerEmail(request.getEmail());
        customer.setCustomerPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setCustomerPhoneNumber(request.getPhoneNumber());
        customer.setCustomerNik(request.getNik());

        // Mapping Date (LocalDate ke Date jika entity masih java.util.Date)
        if (request.getDateOfBirth() != null) {
            customer.setCustomerDateOfBirth(java.sql.Date.valueOf(request.getDateOfBirth()));
        }
        customer.setCustomerAddress(request.getAddress());

        // Handling Gender "MALE" / "FEMALE" -> Enum CustomerGender
        if (request.getGender() != null) {
            if (request.getGender().equalsIgnoreCase("MALE")) {
                customer.setCustomerGender(CustomerGender.PRIA);
            } else if (request.getGender().equalsIgnoreCase("FEMALE")) {
                customer.setCustomerGender(CustomerGender.WANITA);
            }
        } else {
            customer.setCustomerGender(CustomerGender.PRIA);
        }

        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        Customer savedCustomer = customerRepository.save(customer);

        // 3. Simpan Employment jika companyName dikirim dari Android
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), customer.getCustomerPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Update metadata device & last login
        if (request.getDeviceToken() != null) {
            customer.setCustomerDeviceToken(request.getDeviceToken());
        }
        customer.setCustomerLastLoginAt(Instant.now());
        customerRepository.save(customer);

        // Generate token
        String jwt = tokenProvider.generateTokenForCustomer(customer); // Sesuaikan method di JwtTokenProvider kamu

        return new JwtResponse(jwt, "Bearer");
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomer(String email) {
        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found"));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is inactive");
        }

        return mapToResponse(customer); // sesuaikan dengan helper mapper Anda
    }


    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID" + id));

        if (!customer.getCustomerEmail().equalsIgnoreCase(request.getCustomerEmail())) {
            if (customerRepository.existsByCustomerEmail(request.getCustomerEmail())) {
                throw new IllegalArgumentException("Email " + request.getCustomerEmail() + " already exists!");
            }
            customer.setCustomerEmail(request.getCustomerEmail());
        }

        if (!customer.getCustomerNik().equalsIgnoreCase(request.getCustomerNik())) {
            if (customerRepository.existsByCustomerNik(request.getCustomerNik())) {
                throw new IllegalArgumentException("NIK " + request.getCustomerNik() + " already exists!");
            }
            customer.setCustomerNik(request.getCustomerNik());
        }

        customer.setCustomerFullName(request.getCustomerFullName());
        customer.setCustomerAddress(request.getCustomerAddress());
        customer.setCustomerDateOfBirth(request.getCustomerDateOfBirth());
        customer.setCustomerGender(CustomerGender.PRIA);
        customer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        Customer updated = customerRepository.saveAndFlush(customer);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID" + id));

        customer.setCustomerStatus(CustomerStatus.INACTIVE);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request){
        if(customerRepository.existsByCustomerEmail(request.getCustomerEmail())){
            throw new IllegalArgumentException("Email already exists!");
        }
        if(customerRepository.existsByCustomerNik(request.getCustomerNik())){
            throw new IllegalArgumentException("NIK already exists!");
        }

        Customer customer = new Customer();
        customer.setCustomerFullName(request.getCustomerFullName());
        customer.setCustomerAddress(request.getCustomerAddress());
        customer.setCustomerEmail(request.getCustomerEmail());
        customer.setCustomerNik(request.getCustomerNik());
        customer.setCustomerDateOfBirth(request.getCustomerDateOfBirth());
        customer.setCustomerGender(CustomerGender.PRIA);
        customer.setCustomerPasswordHash(passwordEncoder.encode(request.getCustomerPasswordHash()));
        customer.setCustomerPhoneNumber(request.getCustomerPhoneNumber());
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        Customer saved = customerRepository.saveAndFlush(customer);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomer(){
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }


    public CustomerResponse mapToResponse(Customer customer){
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerFullName(customer.getCustomerFullName())
                .customerAddress(customer.getCustomerAddress())
                .customerNik(customer.getCustomerNik())
                .customerEmail(customer.getCustomerEmail())
                .customerDateOfBirth(customer.getCustomerDateOfBirth())
                .customerGender(customer.getCustomerGender() != null ? customer.getCustomerGender().name() : null)
                .customerPhoneNumber(customer.getCustomerPhoneNumber())
                .customerStatus(customer.getCustomerStatus().name())
                .customerPasswordHash(customer.getCustomerPasswordHash())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
