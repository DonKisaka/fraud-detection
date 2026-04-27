package com.donald.fraud_detection.producer;

import com.donald.fraud_detection.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    public void sendOrder(OrderEvent order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send(ordersTopic, order.getOrderId(), orderJson);
            log.info("Order sent to Kafka: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to serialize order: {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to send order to Kafka", e);
        }
    }
}
