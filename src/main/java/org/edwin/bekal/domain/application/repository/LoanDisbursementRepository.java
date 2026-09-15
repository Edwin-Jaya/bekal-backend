package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanDisbursement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Integer> {
    @Query("SELECT r FROM LoanDisbursement r " +
            "JOIN r.loanApplicationId a " +
            "WHERE LOWER(a.status) IN ('disbursed', 'cancelled') " +
            "ORDER BY r.disbursedAt DESC")
    Page<LoanDisbursement> findDisbursementHistory(Pageable pageable);
}
