package org.edwin.bekal.domain.master.repository;

import org.edwin.bekal.domain.master.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    Optional<BankAccount> findByCustomerId(UUID customerId);
    BankAccount findById(UUID customerBankId);
}
