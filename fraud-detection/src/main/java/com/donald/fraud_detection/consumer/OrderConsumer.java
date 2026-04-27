package com.donald.fraud_detection.consumer;

import com.donald.fraud_detection.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {
    private final ObjectMapper objectMapper;
    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(topics = "${app.kafka.topics.orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrder(String message) {
        try {
            OrderEvent order = objectMapper.readValue(message, OrderEvent.class);
            log.info("Order received from Kafka: {}", order.getOrderId());
            fraudDetectionService.analyze(order);
        } catch (Exception e) {
            log.error("Failed to process order message: {}", message, e);
        }
    }
}
