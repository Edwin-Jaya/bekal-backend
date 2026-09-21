package org.edwin.bekal.domain.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.master.dto.BankAccountResponse;
import org.edwin.bekal.domain.master.dto.CreateBankAccountRequest;
import org.edwin.bekal.domain.master.dto.UpdateBankAccountRequest;
import org.edwin.bekal.domain.master.entity.BankAccount;
import org.edwin.bekal.domain.master.repository.BankAccountRepository;
import org.edwin.bekal.domain.master.service.BankAccountService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;

    @Override
    @CacheEvict(value = "bankAccounts", allEntries = true) // ✅ evict on create
    public BankAccountResponse createBankAccount(CreateBankAccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));

        BankAccount bankAccount = new BankAccount();
        bankAccount.setCustomer(customer);
        bankAccount.setBankName(request.getBankName());
        bankAccount.setBankAccountNumber(request.getBankAccountNumber());
        bankAccount.setBankAccountHolder(request.getBankAccountHolder());
        bankAccount.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        bankAccount.setIsVerified(false);
        bankAccount.setStatus("active");

        BankAccount savedAccount = bankAccountRepository.save(bankAccount);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bankAccounts", key = "'all'")
    public List<BankAccountResponse> getAllBankAccounts() {
        return bankAccountRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bankAccounts", key = "'id_' + #id.toString()")
    public BankAccountResponse getBankAccountById(UUID id) {
        BankAccount bankAccount = bankAccountRepository.findById(id);
        if (bankAccount == null) {
            throw new RuntimeException("Bank account not found with id: " + id);
        }
        return mapToResponse(bankAccount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "bankAccounts", key = "'customer_' + #customerId.toString()")
    public BankAccountResponse getBankAccountByCustomerId(UUID customerId) {
        BankAccount bankAccount = bankAccountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Bank account not found for customer id: " + customerId));
        return mapToResponse(bankAccount);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "bankAccounts", key = "'id_' + #id.toString()"),
            @CacheEvict(value = "bankAccounts", key = "'all'")
    }) // ✅ evict specific + list cache
    public BankAccountResponse updateBankAccount(UUID id, UpdateBankAccountRequest request) {
        BankAccount bankAccount = bankAccountRepository.findById(id);
        if (bankAccount == null) {
            throw new RuntimeException("Bank account not found with id: " + id);
        }

        if (request.getBankName() != null) bankAccount.setBankName(request.getBankName());
        if (request.getBankAccountNumber() != null) bankAccount.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankAccountHolder() != null) bankAccount.setBankAccountHolder(request.getBankAccountHolder());
        if (request.getIsPrimary() != null) bankAccount.setIsPrimary(request.getIsPrimary());
        if (request.getIsVerified() != null) bankAccount.setIsVerified(request.getIsVerified());
        if (request.getVerifiedAt() != null) bankAccount.setVerifiedAt(request.getVerifiedAt());
        if (request.getStatus() != null) bankAccount.setStatus(request.getStatus());

        BankAccount updatedAccount = bankAccountRepository.save(bankAccount);
        return mapToResponse(updatedAccount);
    }

    @Override
    @CacheEvict(value = "bankAccounts", allEntries = true) // ✅ evict all on delete
    public void deleteBankAccount(UUID id) {
        BankAccount bankAccount = bankAccountRepository.findById(id);
        if (bankAccount == null) {
            throw new RuntimeException("Bank account not found with id: " + id);
        }
        bankAccountRepository.delete(bankAccount);
    }

    public BankAccountResponse mapToResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomer() != null ? account.getCustomer().getId() : null)
                .bankName(account.getBankName())
                .bankAccountNumber(account.getBankAccountNumber())
                .bankAccountHolder(account.getBankAccountHolder())
                .isPrimary(account.getIsPrimary())
                .isVerified(account.getIsVerified())
                .verifiedAt(account.getVerifiedAt())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}