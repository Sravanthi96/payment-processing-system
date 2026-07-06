package com.example.payment.gateway;

import com.example.payment.config.PaymentGatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedPaymentGatewayTest {

    private SimulatedPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        PaymentGatewayProperties properties = new PaymentGatewayProperties();
        properties.setMaxAmount(new BigDecimal("10000.00"));
        gateway = new SimulatedPaymentGateway(properties);
    }

    @Test
    void approvesNormalCharge() {
        ChargeResult result = gateway.charge(
                new ChargeRequest("order-1", "cust-1", new BigDecimal("42.00"), "USD"));

        assertThat(result.approved()).isTrue();
        assertThat(result.transactionRef()).startsWith("txn_");
        assertThat(result.declineReason()).isNull();
    }

    @Test
    void declinesNonPositiveAmount() {
        ChargeResult result = gateway.charge(
                new ChargeRequest("order-2", "cust-1", BigDecimal.ZERO, "USD"));

        assertThat(result.approved()).isFalse();
        assertThat(result.declineReason()).isEqualTo("invalid amount");
    }

    @Test
    void declinesAmountOverLimit() {
        ChargeResult result = gateway.charge(
                new ChargeRequest("order-3", "cust-1", new BigDecimal("10000.01"), "USD"));

        assertThat(result.approved()).isFalse();
        assertThat(result.declineReason()).isEqualTo("limit exceeded");
    }

    @Test
    void approvesAmountExactlyAtLimit() {
        ChargeResult result = gateway.charge(
                new ChargeRequest("order-4", "cust-1", new BigDecimal("10000.00"), "USD"));

        assertThat(result.approved()).isTrue();
    }
}
