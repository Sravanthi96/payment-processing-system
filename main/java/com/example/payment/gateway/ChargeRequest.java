package com.example.payment.gateway;

import java.math.BigDecimal;

/** Command passed to a {@link PaymentGateway} to attempt a charge. */
public record ChargeRequest(
        String orderId,
        String customerId,
        BigDecimal amount,
        String currency
) {
}
