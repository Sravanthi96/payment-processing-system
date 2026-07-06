package com.example.payment.integration;

import com.example.payment.domain.PaymentStatus;
import com.example.payment.event.OrderCreatedEvent;
import com.example.payment.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
class PaymentFlowIntegrationTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("payments")
                    .withUsername("payments")
                    .withPassword("payments");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void approvedOrderProducesCompletedPaymentAndEvent() {
        String orderId = "order-approved-1";
        kafkaTemplate.send("order.created", orderId,
                new OrderCreatedEvent(orderId, "cust-1", new BigDecimal("120.00"), "USD", Instant.now()));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(paymentRepository.findByOrderId(orderId))
                        .isPresent()
                        .hasValueSatisfying(p -> {
                            assertThat(p.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
                            assertThat(p.getTransactionRef()).isNotBlank();
                        }));

        ConsumerRecord<String, String> record = pollFor("payment.completed");
        assertThat(record.key()).isEqualTo(orderId);
        assertThat(record.value()).contains(orderId).contains("transactionRef");
    }

    @Test
    void declinedOrderProducesFailedPaymentAndEvent() {
        String orderId = "order-declined-1";
        kafkaTemplate.send("order.created", orderId,
                new OrderCreatedEvent(orderId, "cust-1", new BigDecimal("50000.00"), "USD", Instant.now()));

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(paymentRepository.findByOrderId(orderId))
                        .isPresent()
                        .hasValueSatisfying(p ->
                                assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED)));

        ConsumerRecord<String, String> record = pollFor("payment.failed");
        assertThat(record.key()).isEqualTo(orderId);
        assertThat(record.value()).contains("limit exceeded");
    }

    private ConsumerRecord<String, String> pollFor(String topic) {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps(
                KAFKA.getBootstrapServers(), "test-" + topic, "true"));
        props.put("auto.offset.reset", "earliest");
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer());
        consumer = cf.createConsumer();
        consumer.subscribe(java.util.List.of(topic));

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(30));
        assertThat(records.count()).isGreaterThanOrEqualTo(1);
        return records.iterator().next();
    }
}
