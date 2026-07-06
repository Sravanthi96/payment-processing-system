package com.example.payment.service;

import com.example.payment.domain.Payment;
import com.example.payment.event.OrderCreatedEvent;
import com.example.payment.event.PaymentCompletedEvent;
import com.example.payment.event.PaymentFailedEvent;
import com.example.payment.gateway.ChargeRequest;
import com.example.payment.gateway.ChargeResult;
import com.example.payment.gateway.PaymentGateway;
import com.example.payment.messaging.PaymentEventPublisher;
import com.example.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Core payment workflow: persist a payment, charge the gateway, record the
 * outcome, and emit the corresponding domain event.
 *
 * <p>Idempotent by {@code orderId} so that redelivered {@link OrderCreatedEvent}
 * messages (Kafka is at-least-once) do not double-charge.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentGateway paymentGateway,
                          PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void processOrder(OrderCreatedEvent event) {
        if (paymentRepository.existsByOrderId(event.orderId())) {
            log.info("Order {} already processed; skipping (idempotent).", event.orderId());
            return;
        }

        Payment payment = Payment.initiate(
                event.orderId(), event.customerId(), event.amount(), event.currency());
        payment = paymentRepository.save(payment);

        ChargeResult result = paymentGateway.charge(new ChargeRequest(
                event.orderId(), event.customerId(), event.amount(), event.currency()));

        if (result.approved()) {
            payment.markCompleted(result.transactionRef());
            paymentRepository.save(payment);
            eventPublisher.publishCompleted(new PaymentCompletedEvent(
                    payment.getId().toString(),
                    payment.getOrderId(),
                    payment.getCustomerId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getTransactionRef(),
                    Instant.now()));
        } else {
            payment.markFailed(result.declineReason());
            paymentRepository.save(payment);
            eventPublisher.publishFailed(new PaymentFailedEvent(
                    payment.getId().toString(),
                    payment.getOrderId(),
                    payment.getCustomerId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getFailureReason(),
                    Instant.now()));
        }
    }
}
