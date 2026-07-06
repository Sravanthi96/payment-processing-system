package com.example.payment.gateway;

import com.example.payment.config.PaymentGatewayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Deterministic, rule-based stand-in for a real payment provider.
 *
 * <p>Decline rules:
 * <ul>
 *   <li>non-positive amount &rarr; {@code invalid amount}</li>
 *   <li>amount above the configured max &rarr; {@code limit exceeded}</li>
 * </ul>
 * Everything else is approved with a generated transaction reference.
 */
@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    private final PaymentGatewayProperties properties;

    public SimulatedPaymentGateway(PaymentGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public ChargeResult charge(ChargeRequest request) {
        BigDecimal amount = request.amount();

        if (amount == null || amount.signum() <= 0) {
            log.info("Declining charge for order {}: invalid amount {}", request.orderId(), amount);
            return ChargeResult.declined("invalid amount");
        }

        if (amount.compareTo(properties.getMaxAmount()) > 0) {
            log.info("Declining charge for order {}: amount {} exceeds limit {}",
                    request.orderId(), amount, properties.getMaxAmount());
            return ChargeResult.declined("limit exceeded");
        }

        String transactionRef = "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        log.info("Approved charge for order {} with transactionRef {}", request.orderId(), transactionRef);
        return ChargeResult.approved(transactionRef);
    }
}
