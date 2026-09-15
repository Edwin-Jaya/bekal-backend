package org.edwin.bekal.domain.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanHistoryResponse(
        UUID applicationId,
        String applicationNumber,
        String customerName,
        BigDecimal amountRequested,
        String currentStatus,    // e.g. review_approved, in_approval, review_rejected
        String reviewResult,     // APPROVED / REJECTED
        String reviewNotes,      // Catatan dari marketing
       Instant reviewedAt
) {}