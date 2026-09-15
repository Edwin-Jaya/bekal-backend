package org.edwin.bekal.domain.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDashboardResponse {
    private String loanStatus;           // "PRE_APPLICATION", "IN_REVIEW", "ACTIVE_REPAYMENT"
    private String creditTier;           // "TIER_1", "TIER_2", "TIER_3"
    private BigDecimal maxLimit;         // Limit maksimal tier
    private BigDecimal availableLimit;   // Limit tersisa (Max Limit - Active Bill)
    private BigDecimal activeBillAmount; // Total tagihan berjalan
    private Integer successfulLoansCount;// Jumlah pinjaman lunas
    private Integer requiredForNextTier; // Syarat lunas untuk tier berikutnya

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}