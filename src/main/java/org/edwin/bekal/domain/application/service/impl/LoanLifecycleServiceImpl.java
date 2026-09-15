package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.service.LoanLifecycleService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.enums.CreditTier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanLifecycleServiceImpl implements LoanLifecycleService {

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanRepository;

    @Transactional
    public void processFinalRepayment(UUID customerId, UUID loanApplicationId) {
        LoanApplication loan = loanRepository.findById(loanApplicationId)
                .orElseThrow(() -> new EntityNotFoundException("Pinjaman tidak ditemukan"));

        // Proteksi Idempotency: Jika status sudah CLOSED, jangan naikkan tier lagi
        if ("CLOSED".equalsIgnoreCase(loan.getStatus())) {
            return;
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer tidak ditemukan"));

        // 1. Update counter pinjaman sukses
        int currentCount = customer.getSuccessfulLoansCount() != null ? customer.getSuccessfulLoansCount() : 0;
        int newLoanCount = currentCount + 1;
        customer.setSuccessfulLoansCount(newLoanCount);

        // 2. Evaluasi & upgrade Credit Tier
        CreditTier updatedTier = CreditTier.resolveTier(newLoanCount);
        customer.setCreditTier(updatedTier);

        // 3. Update status aplikasi pinjaman
        loan.setStatus("CLOSED");

        // 4. Simpan ke database
        customerRepository.save(customer);
        loanRepository.save(loan);
    }
}
