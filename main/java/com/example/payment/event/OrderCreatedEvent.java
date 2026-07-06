package com.example.payment.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Inbound event published by the Order service when a new order is placed.
 * Consumed from the {@code order.created} topic.
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
