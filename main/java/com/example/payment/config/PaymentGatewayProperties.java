package com.example.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Tuning for the simulated payment gateway, bound from {@code app.payment.gateway.*}.
 */
@ConfigurationProperties(prefix = "app.payment.gateway")
public class PaymentGatewayProperties {

    /** Charges strictly above this amount are declined as "limit exceeded". */
    private BigDecimal maxAmount = new BigDecimal("10000.00");

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
}
