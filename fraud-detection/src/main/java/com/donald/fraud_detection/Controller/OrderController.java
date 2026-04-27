package com.donald.fraud_detection.Controller;

import com.donald.fraud_detection.model.OrderEvent;
import com.donald.fraud_detection.producer.OrderProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderProducer orderProducer;

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderEvent order) {
        orderProducer.sendOrder(order);
        return ResponseEntity.ok("Order submitted for fraud analysis: " + order.getOrderId());
    }
}
