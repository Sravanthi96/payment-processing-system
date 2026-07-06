package com.example.payment.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound event emitted to {@code payment.failed} when a payment is declined.
 */
public record PaymentFailedEvent(
        String paymentId,
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        String reason,
        Instant occurredAt
) {
}
