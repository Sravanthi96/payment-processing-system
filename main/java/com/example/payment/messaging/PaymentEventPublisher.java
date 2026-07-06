package com.example.payment.messaging;

import com.example.payment.config.KafkaTopicProperties;
import com.example.payment.event.PaymentCompletedEvent;
import com.example.payment.event.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes payment outcome events. The order id is used as the message key so
 * that all events for an order land on the same partition (ordering guarantee).
 */
@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties topics;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    public void publishCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompleted for order {} (payment {})", event.orderId(), event.paymentId());
        kafkaTemplate.send(topics.getTopics().getPaymentCompleted(), event.orderId(), event);
    }

    public void publishFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailed for order {} (payment {}): {}",
                event.orderId(), event.paymentId(), event.reason());
        kafkaTemplate.send(topics.getTopics().getPaymentFailed(), event.orderId(), event);
    }
}
