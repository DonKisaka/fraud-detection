package com.donald.fraud_detection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
        private String orderId;
        private String customerId;
        private double amount;
        private String location;
        private String itemDescription;
}

