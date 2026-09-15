package org.edwin.bekal.enums;

import java.math.BigDecimal;

public enum CreditTier {
    TIER_1(new BigDecimal("20000000"), 0),  // Maks Rp 20 Juta (Baru daftar)
    TIER_2(new BigDecimal("35000000"), 1),  // Maks Rp 35 Juta (1x lunas)
    TIER_3(new BigDecimal("50000000"), 3);  // Maks Rp 50 Juta (3x lunas)

    private final BigDecimal maxCap;
    private final int minSuccessfulLoansRequired;

    CreditTier(BigDecimal maxCap, int minSuccessfulLoansRequired) {
        this.maxCap = maxCap;
        this.minSuccessfulLoansRequired = minSuccessfulLoansRequired;
    }

    public BigDecimal getMaxCap() { return maxCap; }
    public int getMinSuccessfulLoansRequired() { return minSuccessfulLoansRequired; }

    // Logika penentuan tier berdasarkan histori pelunasan
    public static CreditTier resolveTier(int successfulLoansCount) {
        if (successfulLoansCount >= TIER_3.minSuccessfulLoansRequired) return TIER_3;
        if (successfulLoansCount >= TIER_2.minSuccessfulLoansRequired) return TIER_2;
        return TIER_1;
    }
}