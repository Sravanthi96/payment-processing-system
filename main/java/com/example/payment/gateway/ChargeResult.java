package com.example.payment.gateway;

/**
 * Outcome of a charge attempt. On success {@code transactionRef} is set;
 * on decline {@code declineReason} explains why.
 */
public record ChargeResult(
        boolean approved,
        String transactionRef,
        String declineReason
) {

    public static ChargeResult approved(String transactionRef) {
        return new ChargeResult(true, transactionRef, null);
    }

    public static ChargeResult declined(String reason) {
        return new ChargeResult(false, null, reason);
    }
}
