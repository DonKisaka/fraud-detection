package com.donald.fraud_detection.service;

import com.donald.fraud_detection.model.FraudResult;
import com.donald.fraud_detection.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final ChatClient chatClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.fraud-results}")
    private String fraudResultsTopic;

    public void analyze(OrderEvent order) {
        try {
            String prompt = buildPrompt(order);
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            FraudResult result = parseResponse(aiResponse, order.getOrderId());
            String resultJson = objectMapper.writeValueAsString(result);

            kafkaTemplate.send(fraudResultsTopic, order.getOrderId(), resultJson);
            log.info("Fraud result published for order {}: fraudulent={}",
                    order.getOrderId(), result.isFraudulent());

        } catch (Exception e) {
            log.error("Failed to analyze order: {}", order.getOrderId(), e);
        }
    }

    private String buildPrompt(OrderEvent order) {
        return String.format("""
                  Analyze this order for fraud and respond in JSON only. No explanation outside JSON.

                  Order details:
                  - Order ID: %s
                  - Customer ID: %s
                  - Amount: $%.2f
                  - Location: %s
                  - Item: %s

                  Respond in this exact format:
                  {
                    "orderId": "%s",
                    "fraudulent": true or false,
                    "reason": "your reason here",
                    "confidenceScore": 0.0 to 1.0
                  }
                  """,
                order.getOrderId(),
                order.getCustomerId(),
                order.getAmount(),
                order.getLocation(),
                order.getItemDescription(),
                order.getOrderId()
        );
    }

    private FraudResult parseResponse(String aiResponse, String orderId) {
        try {
            String cleaned = aiResponse.trim();
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}") + 1;
            String json = cleaned.substring(start, end);
            return objectMapper.readValue(json, FraudResult.class);
        } catch (Exception e) {
            log.warn("Could not parse AI response, returning default result");
            return new FraudResult(orderId, false, "Could not parse AI response", 0.0);
        }
    }
}
