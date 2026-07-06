package com.example.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares topics so they are auto-created via {@code KafkaAdmin} in local/dev
 * environments. In production topics are typically provisioned externally.
 */
@Configuration
public class KafkaTopicConfig {

    private final KafkaTopicProperties properties;

    public KafkaTopicConfig(KafkaTopicProperties properties) {
        this.properties = properties;
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return build(properties.getTopics().getOrderCreated());
    }

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return build(properties.getTopics().getOrderCreated() + ".DLT");
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return build(properties.getTopics().getPaymentCompleted());
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return build(properties.getTopics().getPaymentFailed());
    }

    private NewTopic build(String name) {
        return TopicBuilder.name(name)
                .partitions(properties.getPartitions())
                .replicas(properties.getReplicas())
                .build();
    }
}
