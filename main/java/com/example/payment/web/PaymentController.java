package com.example.payment.web;

import com.example.payment.exception.PaymentNotFoundException;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.web.dto.PaymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only query surface over processed payments. Payments are created
 * asynchronously by the Kafka consumer, not through this API.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{orderId}")
    public PaymentResponse getByOrderId(@PathVariable String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
    }

    @GetMapping
    public List<PaymentResponse> list(@RequestParam(required = false) String customerId) {
        List<com.example.payment.domain.Payment> payments = (customerId != null)
                ? paymentRepository.findByCustomerId(customerId)
                : paymentRepository.findAll();
        return payments.stream().map(PaymentResponse::from).toList();
    }
}
