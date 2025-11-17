package com.rpay.domain.merchant;

/**
 * Configuration for merchant settlement preferences
 */
public record SettlementConfig(
    SettlementCycle cycle,
    String bankAccountNumber,
    String ifscCode,
    String accountHolderName
) {
    public static SettlementConfig defaultConfig() {
        return new SettlementConfig(SettlementCycle.T_PLUS_2, null, null, null);
    }

    public enum SettlementCycle {
        INSTANT,      // Immediate settlement (higher fees)
        T_PLUS_1,     // Next business day
        T_PLUS_2,     // 2 business days (standard)
        T_PLUS_3      // 3 business days
    }
}
