package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.customer.dto.*;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.customer.service.EmploymentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmploymentServiceImpl implements EmploymentService {

    private final CustomerRepository customerRepository;
    private final EmploymentRepository employmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmploymentResponse updateEmployment(UUID id, UpdateEmploymentRequest request){
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employment not found with ID" + id));

        employment.setCustomerEmploymentType(request.getCustomerEmploymentType());
        employment.setCustomerCompanyName(request.getCustomerCompanyName());
        employment.setCustomerJobTitle(request.getCustomerJobTitle());
        employment.setCustomerIndustry(request.getCustomerIndustry());
        employment.setCustomerDeclaredIncome(request.getCustomerDeclaredIncome());
        //verified
        employment.setCustomerOtherIncome(request.getCustomerOtherIncome());
        //income verified by
        //income verified at
        employment.setCustomerEmploymentStartDate(request.getCustomerEmploymentStartDate());

        Employment updated = employmentRepository.saveAndFlush(employment);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteEmployment(UUID id){
        Employment employment = employmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employment not found with ID" + id));

        employment.setCustomerIsCurrent(false);
    }

    @Override
    @Transactional
    public EmploymentResponse createEmployment(CreateEmploymentRequest request){

        Customer customer = customerRepository.findById(request.getCustomer())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomer()));

        Employment employment = new Employment();

        employment.setCustomer(customer);
        employment.setCustomerEmploymentType(request.getCustomerEmploymentType());
        employment.setCustomerCompanyName(request.getCustomerCompanyName());
        employment.setCustomerJobTitle(request.getCustomerJobTitle());
        employment.setCustomerIndustry(request.getCustomerIndustry());
        employment.setCustomerDeclaredIncome(request.getCustomerDeclaredIncome());
        //verified
        employment.setCustomerOtherIncome(request.getCustomerOtherIncome());
        //income verified by
        //income verified at
        employment.setCustomerEmploymentStartDate(request.getCustomerEmploymentStartDate());
        employment.setCustomerIsCurrent(true);

        Employment saved = employmentRepository.saveAndFlush(employment);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmploymentResponse> getAllEmployment(){
        return employmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }


    public EmploymentResponse mapToResponse(Employment employment){
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
