package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.projection.DashboardProjections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {
    Optional<LoanApplication> findById(UUID id);

    // nanti hapus submittednya (untuk mengingatkan diri)
    @Query("SELECT l FROM LoanApplication l WHERE LOWER(l.status) IN ('submitted', 'in_review')")
    Page<LoanApplication> findPendingMarketingReviews(Pageable pageable);

    @Query("SELECT l FROM LoanApplication l WHERE LOWER(l.status) IN ('in_approval')")
    Page<LoanApplication> findPendingBranchMarketingApproval(Pageable pageable);

    @Query("SELECT l FROM LoanApplication l WHERE LOWER(l.status) IN ('in_disbursement')")
    Page<LoanApplication> findPendingBackOfficeDisbursement(Pageable pageable);

    @Query(value = """
    SELECT 
      COUNT(id) AS totalFiles,
      ISNULL(SUM(amount_requested), 0) AS totalPengajuanAmount,
      COUNT(CASE WHEN status = 'in_review' THEN 1 END) AS antreanReview,
      COUNT(CASE WHEN status = 'in_approval' THEN 1 END) AS antreanApproval,
      COUNT(CASE WHEN status = 'in_disbursement' THEN 1 END) AS antreanPencairan,
      ISNULL(SUM(CASE WHEN status = 'disbursed' THEN amount_requested ELSE 0 END), 0) AS totalDicairkanAmount
    FROM TRX_LOAN_APPLICATIONS
    WHERE status != 'cancelled'
    """, nativeQuery = true)
    DashboardProjections.Metrics getDashboardMetrics();

    @Query(value = """
    SELECT 
      FORMAT(created_at, 'MMM') AS month,
      DATEPART(month, created_at) AS monthNum,
      ISNULL(SUM(amount_requested), 0) AS submissionAmount,
      ISNULL(SUM(CASE WHEN status = 'disbursed' THEN amount_requested ELSE 0 END), 0) AS disbursedAmount
    FROM TRX_LOAN_APPLICATIONS
    WHERE created_at >= DATEADD(month, -6, SYSUTCDATETIME())
    GROUP BY FORMAT(created_at, 'MMM'), DATEPART(month, created_at)
    ORDER BY monthNum ASC
    """, nativeQuery = true)
    List<DashboardProjections.OperationalTrend> getOperationalTrend();

    @Query(value = """
    SELECT 
      status AS stage,
      COUNT(1) AS count
    FROM TRX_LOAN_APPLICATIONS
    WHERE status IN ('in_review', 'review_rejected', 'in_approval', 'approval_rejected', 'in_disbursement', 'disbursed')
    GROUP BY status
    """, nativeQuery = true)
    List<DashboardProjections.BottleneckStatus> getBottleneckStatus();

    @Query(value = """
    SELECT TOP 5 
      FORMAT(updated_at, 'dd MMM yyyy') AS date,
      applicant_name AS applicant,
      amount_requested AS amount,
      status AS status
    FROM TRX_LOAN_APPLICATIONS
    ORDER BY updated_at DESC
    """, nativeQuery = true)
    List<DashboardProjections.RecentActivity> getRecentActivities();

    Page<LoanApplication> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<LoanApplication> findByCustomer_Id(UUID customerId, Pageable pageable);

    Optional<LoanApplication> findTopByCustomer_IdOrderBySubmittedAtDesc(UUID customerId);
}
