package com.example.payment.gateway;

/**
 * Abstraction over a payment provider. The current implementation is a
 * {@link SimulatedPaymentGateway}; a real provider (Stripe, Adyen, ...) can be
 * dropped in behind this interface without touching the rest of the service.
 */
public interface PaymentGateway {

    ChargeResult charge(ChargeRequest request);
}
