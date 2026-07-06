package com.example.payment.service;

import com.example.payment.domain.Payment;
import com.example.payment.domain.PaymentStatus;
import com.example.payment.event.OrderCreatedEvent;
import com.example.payment.event.PaymentCompletedEvent;
import com.example.payment.event.PaymentFailedEvent;
import com.example.payment.gateway.ChargeRequest;
import com.example.payment.gateway.ChargeResult;
import com.example.payment.gateway.PaymentGateway;
import com.example.payment.messaging.PaymentEventPublisher;
import com.example.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private OrderCreatedEvent order(BigDecimal amount) {
        return new OrderCreatedEvent("order-1", "cust-1", amount, "USD", Instant.now());
    }

    @Test
    void completesPaymentAndPublishesCompletedEvent() {
        when(paymentRepository.existsByOrderId("order-1")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentGateway.charge(any(ChargeRequest.class)))
                .thenReturn(ChargeResult.approved("txn_abc"));

        paymentService.processOrder(order(new BigDecimal("50.00")));

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(saved.getValue().getTransactionRef()).isEqualTo("txn_abc");

        ArgumentCaptor<PaymentCompletedEvent> event = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(eventPublisher).publishCompleted(event.capture());
        assertThat(event.getValue().orderId()).isEqualTo("order-1");
        assertThat(event.getValue().transactionRef()).isEqualTo("txn_abc");
        verify(eventPublisher, never()).publishFailed(any());
    }

    @Test
    void failsPaymentAndPublishesFailedEvent() {
        when(paymentRepository.existsByOrderId("order-1")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(paymentGateway.charge(any(ChargeRequest.class)))
                .thenReturn(ChargeResult.declined("limit exceeded"));

        paymentService.processOrder(order(new BigDecimal("99999.00")));

        ArgumentCaptor<PaymentFailedEvent> event = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventPublisher).publishFailed(event.capture());
        assertThat(event.getValue().reason()).isEqualTo("limit exceeded");
        verify(eventPublisher, never()).publishCompleted(any());
    }

    @Test
    void skipsAlreadyProcessedOrder() {
        when(paymentRepository.existsByOrderId("order-1")).thenReturn(true);

        paymentService.processOrder(order(new BigDecimal("50.00")));

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(paymentGateway, eventPublisher);
    }
}
