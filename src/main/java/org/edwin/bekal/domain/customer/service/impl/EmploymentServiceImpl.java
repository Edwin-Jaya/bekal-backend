package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.customer.service.EmploymentService;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmploymentServiceImpl implements EmploymentService {

    private final CustomerRepository customerRepository;
    private final EmploymentRepository employmentRepository;
    private final InternalUserRepository internalUserRepository;

    @Override
    @Transactional
    public EmploymentResponse createEmployment(CreateEmploymentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomer())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomer()));

        Employment employment = new Employment();
        employment.setCustomer(customer);
        employment.setCustomerEmploymentType(request.getCustomerEmploymentType());
        employment.setCustomerCompanyName(request.getCustomerCompanyName());
        employment.setCustomerJobTitle(request.getCustomerJobTitle());
        employment.setCustomerIndustry(request.getCustomerIndustry());
        employment.setCustomerDeclaredIncome(request.getCustomerDeclaredIncome());
        employment.setCustomerOtherIncome(request.getCustomerOtherIncome());
        employment.setCustomerEmploymentStartDate(request.getCustomerEmploymentStartDate());
        employment.setCustomerIsCurrent(true);

        // Handle Income Verification if passed during creation
        if (request.getIncomeVerifiedById() != null) {
            InternalUser verifier = internalUserRepository.findById(request.getIncomeVerifiedById())
                    .orElseThrow(() -> new IllegalArgumentException("Internal User not found with ID: " + request.getIncomeVerifiedById()));
            employment.setInternalUser(verifier);
        }

        employment.setCustomerVerifiedIncome(request.getCustomerVerifiedIncome());

        if (request.getCustomerVerifiedIncome() != null || request.getIncomeVerifiedById() != null) {
            Date verifiedAt = request.getCustomerIncomeVerifiedAt() != null ? request.getCustomerIncomeVerifiedAt() : new Date();
            employment.setCustomerIncomeVerifiedAt(verifiedAt.toInstant());
        }

        Employment saved = employmentRepository.saveAndFlush(employment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmploymentResponse updateEmployment(UUID id, UpdateEmploymentRequest request) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employment not found with ID: " + id));

        if (request.getCustomerEmploymentType() != null) employment.setCustomerEmploymentType(request.getCustomerEmploymentType());
        if (request.getCustomerCompanyName() != null) employment.setCustomerCompanyName(request.getCustomerCompanyName());
        if (request.getCustomerJobTitle() != null) employment.setCustomerJobTitle(request.getCustomerJobTitle());
        if (request.getCustomerIndustry() != null) employment.setCustomerIndustry(request.getCustomerIndustry());
        if (request.getCustomerDeclaredIncome() != null) employment.setCustomerDeclaredIncome(request.getCustomerDeclaredIncome());
        if (request.getCustomerOtherIncome() != null) employment.setCustomerOtherIncome(request.getCustomerOtherIncome());
        if (request.getCustomerEmploymentStartDate() != null) employment.setCustomerEmploymentStartDate(request.getCustomerEmploymentStartDate());

        // Handle Verification Updates
        if (request.getIncomeVerifiedById() != null) {
            InternalUser verifier = internalUserRepository.findById(request.getIncomeVerifiedById())
                    .orElseThrow(() -> new IllegalArgumentException("Internal User not found with ID: " + request.getIncomeVerifiedById()));
            employment.setInternalUser(verifier);
        }

        if (request.getCustomerVerifiedIncome() != null) {
            employment.setCustomerVerifiedIncome(request.getCustomerVerifiedIncome());
        }

        if (request.getCustomerIncomeVerifiedAt() != null) {
            employment.setCustomerIncomeVerifiedAt(request.getCustomerIncomeVerifiedAt().toInstant());
        } else if (request.getCustomerVerifiedIncome() != null || request.getIncomeVerifiedById() != null) {
            // Auto-stamp current date if verification fields are updated without passing a explicit timestamp
            employment.setCustomerIncomeVerifiedAt(new Date().toInstant());
        }

        Employment updated = employmentRepository.saveAndFlush(employment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEmployment(UUID id) {
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employment not found with ID: " + id));

        employment.setCustomerIsCurrent(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmploymentResponse> getAllEmployment() {
        return employmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public EmploymentResponse mapToResponse(Employment employment) {
        return EmploymentResponse.builder()
                .id(employment.getId())
                .customer(employment.getCustomer())
                .customerEmploymentType(employment.getCustomerEmploymentType())
                .customerCompanyName(employment.getCustomerCompanyName())
                .customerJobTitle(employment.getCustomerJobTitle())
                .customerIndustry(employment.getCustomerIndustry())
                .customerDeclaredIncome(employment.getCustomerDeclaredIncome())
                .customerVerifiedIncome(employment.getCustomerVerifiedIncome())
                .customerOtherIncome(employment.getCustomerOtherIncome())
                .internalUser(employment.getInternalUser())
                .customerIncomeVerifiedAt(employment.getCustomerIncomeVerifiedAt())
                .customerEmploymentStartDate(employment.getCustomerEmploymentStartDate())
                .customerIsCurrent(employment.getCustomerIsCurrent())
                .createdAt(employment.getCreatedAt())
                .updatedAt(employment.getUpdatedAt())
                .build();
    }
}