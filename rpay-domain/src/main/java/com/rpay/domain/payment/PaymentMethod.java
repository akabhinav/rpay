package com.rpay.domain.payment;

/**
 * Supported payment methods in the system.
 * Extensible design allows easy addition of new methods.
 */
public enum PaymentMethod {
    /**
     * Unified Payments Interface (most popular in India)
     */
    UPI("upi", "UPI", true),

    /**
     * Credit/Debit Cards
     */
    CARD("card", "Card", true),

    /**
     * Net Banking
     */
    NETBANKING("netbanking", "Net Banking", true),

    /**
     * Digital Wallets (Paytm, PhonePe, etc.)
     */
    WALLET("wallet", "Wallet", true),

    /**
     * EMI (Equated Monthly Installments)
     */
    EMI("emi", "EMI", true),

    /**
     * Cardless EMI
     */
    CARDLESS_EMI("cardless_emi", "Cardless EMI", true),

    /**
     * Pay Later services
     */
    PAYLATER("paylater", "Pay Later", true),

    /**
     * Bank Transfer / NEFT / RTGS
     */
    BANK_TRANSFER("bank_transfer", "Bank Transfer", false);

    private final String code;
    private final String displayName;
    private final boolean instantSettlement;

    PaymentMethod(String code, String displayName, boolean instantSettlement) {
        this.code = code;
        this.displayName = displayName;
        this.instantSettlement = instantSettlement;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isInstantSettlement() {
        return instantSettlement;
    }

    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown payment method code: " + code);
    }
}
