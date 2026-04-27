package com.donald.fraud_detection.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudResult {
    private String orderId;
    private boolean fraudulent;
    private String reason;
    private double confidenceScore;
}
