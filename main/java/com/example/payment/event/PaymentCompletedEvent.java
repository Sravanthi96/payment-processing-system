package com.example.payment.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound event emitted to {@code payment.completed} when a payment succeeds.
 */
public record PaymentCompletedEvent(
        String paymentId,
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String transactionRef,
        Instant occurredAt
) {
}
