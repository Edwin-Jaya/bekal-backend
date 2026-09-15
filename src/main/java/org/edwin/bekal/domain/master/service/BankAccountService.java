package org.edwin.bekal.domain.master.service;

import org.edwin.bekal.domain.master.dto.BankAccountResponse;
import org.edwin.bekal.domain.master.dto.CreateBankAccountRequest;
import org.edwin.bekal.domain.master.dto.UpdateBankAccountRequest;

import java.util.List;
import java.util.UUID;

public interface BankAccountService {
    BankAccountResponse createBankAccount(CreateBankAccountRequest request);
    List<BankAccountResponse> getAllBankAccounts();
    BankAccountResponse getBankAccountById(UUID id);
    BankAccountResponse getBankAccountByCustomerId(UUID customerId);
    BankAccountResponse updateBankAccount(UUID id, UpdateBankAccountRequest request);
    void deleteBankAccount(UUID id);
}
