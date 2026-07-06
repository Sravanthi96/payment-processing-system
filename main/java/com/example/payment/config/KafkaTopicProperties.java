package com.example.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Topic names and provisioning settings, bound from {@code app.kafka.*}.
 */
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicProperties {

    private Topics topics = new Topics();
    private int partitions = 3;
    private short replicas = 1;

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public short getReplicas() {
        return replicas;
    }

    public void setReplicas(short replicas) {
        this.replicas = replicas;
    }

    public static class Topics {
        private String orderCreated = "order.created";
        private String paymentCompleted = "payment.completed";
        private String paymentFailed = "payment.failed";

        public String getOrderCreated() {
            return orderCreated;
        }

        public void setOrderCreated(String orderCreated) {
            this.orderCreated = orderCreated;
        }

        public String getPaymentCompleted() {
            return paymentCompleted;
        }

        public void setPaymentCompleted(String paymentCompleted) {
            this.paymentCompleted = paymentCompleted;
        }

        public String getPaymentFailed() {
            return paymentFailed;
        }

        public void setPaymentFailed(String paymentFailed) {
            this.paymentFailed = paymentFailed;
        }
    }
}
