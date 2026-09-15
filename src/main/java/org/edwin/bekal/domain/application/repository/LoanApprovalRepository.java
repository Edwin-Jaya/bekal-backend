package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.entity.LoanApproval;
import org.edwin.bekal.domain.application.entity.LoanReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanApprovalRepository extends JpaRepository <LoanApproval, Integer> {
    @Query("SELECT r FROM LoanApproval r " +
            "JOIN r.loanApplicationId a " +
            "WHERE LOWER(a.status) IN ('approval_rejected', 'in_disbursement', 'disbursed', 'cancelled') " +
            "ORDER BY r.approvedAt DESC")
    Page<LoanApproval> findApprovalHistory(Pageable pageable);
    Optional<LoanApproval> findTopByLoanApplicationId_IdOrderByApprovedAtDesc(UUID loanApplicationId);
}
