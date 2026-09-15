package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.entity.LoanReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LoanReviewRepository extends JpaRepository<LoanReview, Integer> {
    Optional<LoanReview> findTopByIdOrderByReviewedAtDesc(UUID loanApplicationId);
    Optional<LoanReview> findTopByLoanApplicationId_IdOrderByReviewedAtDesc(UUID loanApplicationId);
    @Query("SELECT r FROM LoanReview r " +
            "JOIN r.loanApplicationId a " +
            "WHERE LOWER(a.status) NOT IN ('in_review') " +
            "ORDER BY r.reviewedAt DESC")
    Page<LoanReview> findMarketingHistory(Pageable pageable);
}
