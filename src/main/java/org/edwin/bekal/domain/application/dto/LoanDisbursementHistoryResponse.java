package org.edwin.bekal.domain.application.dto;
import lombok.Builder;
import lombok.Data;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanDisbursementHistoryResponse (
        UUID applicationId,
        String applicationNumber,
        BigDecimal amountRequested,
        String customerName,
        String currentStatus,
        Instant disbursedAt){}
