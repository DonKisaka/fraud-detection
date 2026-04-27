# AI Fraud Detection Pipeline

An event-driven fraud detection system that uses AI to analyze orders in real time — asynchronously, without blocking the user.

---

## The Problem

E-commerce platforms process thousands of orders per minute. Manually reviewing each order for fraud is impossible. Rule-based systems miss sophisticated fraud and generate too many false positives.

The real challenge is not just detection — it is doing it **without slowing down the customer experience**. A customer placing an order should not wait 15 seconds for a fraud check to complete before getting a confirmation.

---

## The Solution

Decouple order placement from fraud analysis using an **event-driven architecture**:

1. Customer places an order → system responds immediately
2. Order event is published to a Kafka topic in the background
3. AI service picks up the event, analyzes it using a local LLM
4. Fraud result is published to a separate Kafka topic

The customer gets an instant response. The fraud analysis happens asynchronously.

---

## Architecture

```
POST /api/orders
      │
      ▼
OrderController  ──────────────────────────────► HTTP 200 (immediate)
      │
      ▼
OrderProducer
      │
      ▼
[orders topic - Kafka]
      │
      ▼
OrderConsumer
      │
      ▼
FraudDetectionService (Ollama AI)
      │
      ▼
[fraud-results topic - Kafka]
```

---

## Tech Stack

| Technology | Role |
|------------|------|
| Spring Boot 4.0.5 | Application framework |
| Spring AI 2.0.0-M4 | AI integration layer |
| Apache Kafka | Event streaming |
| Ollama (llama3.2) | Local LLM — zero API costs |
| Docker | Infrastructure (Kafka + Zookeeper) |

---

## Project Structure

```
src/main/java/com/donald/fraud_detection/
│
├── model/
│   ├── OrderEvent.java              # Incoming order data
│   └── FraudResult.java             # AI analysis result
│
├── config/
│   ├── KafkaTopicConfig.java        # Auto-creates Kafka topics on startup
│   └── ChatClientConfig.java        # Spring AI ChatClient bean
│
├── producer/
│   └── OrderProducer.java           # Publishes order events to Kafka
│
├── consumer/
│   └── OrderConsumer.java           # Listens for orders, triggers AI analysis
│
├── service/
│   └── FraudDetectionService.java   # Calls Ollama, publishes fraud result
│
└── controller/
    └── OrderController.java         # REST endpoint — accepts order requests
```

---

## How It Works

### 1. Order Submitted
A POST request arrives at `/api/orders` with order details — customer ID, amount, location, and item description.

### 2. Event Published
The order is serialized to JSON and published to the `orders` Kafka topic. The HTTP response is returned immediately.

### 3. AI Analysis
The Kafka consumer picks up the event and passes it to `FraudDetectionService`. The service builds a structured prompt and sends it to a locally running Ollama model (llama3.2).

The AI evaluates:
- Is the order amount unusually high?
- Is the location suspicious?
- Is the item description indicative of bulk/resale fraud?

### 4. Result Published
The AI returns a JSON verdict which is published to the `fraud-results` Kafka topic for downstream consumers (notifications, dashboards, blocking systems).

---

## Running Locally

### Prerequisites
- Java 25
- Docker
- Ollama installed and running

### Steps

**1. Pull the AI model:**
```bash
ollama pull llama3.2
```

**2. Start Kafka infrastructure:**
```bash
docker-compose up -d
```

**3. Run the Spring Boot app:**
```bash
./mvnw spring-boot:run
```

---

## Testing

Send a POST request to `http://localhost:8080/api/orders`:

```json
{
  "orderId": "ORD-001",
  "customerId": "CUST-42",
  "amount": 9999.99,
  "location": "Unknown Location",
  "itemDescription": "100x iPhone 15 Pro"
}
```

**Immediate response:**
```
Order submitted for fraud analysis: ORD-001
```

**Console logs (background processing):**
```
Order sent to Kafka: ORD-001
Order received from Kafka: ORD-001
Fraud result published for order ORD-001: fraudulent=true
```

---

## Key Design Decisions

**Why Kafka?**
Kafka decouples the order service from the fraud service. Each can scale independently. If the AI service is slow or down, orders are not lost — they wait in the topic.

**Why a local LLM?**
Ollama runs llama3.2 locally — no API keys, no per-request costs, no data leaving your machine. Ideal for learning and for sensitive financial data.

**Why String serialization?**
Using `StringSerializer` with manual `ObjectMapper` conversion keeps the setup simple and debuggable. You can read messages directly in Kafka without special tooling.

---

