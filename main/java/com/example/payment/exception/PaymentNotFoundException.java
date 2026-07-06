package com.example.payment.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String orderId) {
        super("No payment found for order " + orderId);
    }
}
