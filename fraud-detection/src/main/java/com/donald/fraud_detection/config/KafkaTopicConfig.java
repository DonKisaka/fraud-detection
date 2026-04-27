package com.donald.fraud_detection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    @Value("${app.kafka.topics.fraud-results}")
    private String fraudResultsTopic;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(ordersTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fraudResultsTopic() {
        return TopicBuilder.name(fraudResultsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
