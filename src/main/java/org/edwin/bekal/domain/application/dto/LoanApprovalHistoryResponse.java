package org.edwin.bekal.domain.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApprovalHistoryResponse (
            UUID applicationId,
            String applicationNumber,
            String customerName,
            BigDecimal amountRequested,
            String currentStatus,    // e.g. review_approved, in_approval, review_rejected
            String approvalResult,     // APPROVED / REJECTED
            String approvalNotes,      // Catatan dari marketing
            Instant approvedAt
    ) {}
