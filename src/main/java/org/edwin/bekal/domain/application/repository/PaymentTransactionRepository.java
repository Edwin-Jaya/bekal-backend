package org.edwin.bekal.domain.application.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.edwin.bekal.domain.application.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    boolean existsByTransactionReference(String transactionReference);
    List<PaymentTransaction> findByCustomerIdOrderByPaymentDateDesc(UUID customerId);
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM PaymentTransaction p WHERE p.loan.id = :loanId AND p.status = 'SUCCESS'")
    BigDecimal sumAmountPaidByLoanId(@Param("loanId") UUID loanId);
    @Query("""
    SELECT COALESCE(SUM(p.amountPaid), 0)
    FROM PaymentTransaction p
    WHERE p.loan.id = :loanId AND LOWER(p.status) = 'success'
""")
    BigDecimal sumSuccessfulPaymentsByLoanId(@Param("loanId") UUID loanId);

    @Query("""
    SELECT COUNT(p)
    FROM PaymentTransaction p
    WHERE p.loan.id = :loanId AND LOWER(p.status) = 'success'
""")
    long countSuccessfulPaymentsByLoanId(@Param("loanId") UUID loanId);
}