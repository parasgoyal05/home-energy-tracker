# ⚡ Home Energy Tracking System (Microservices)

A scalable, event-driven microservices system that monitors real-time IoT energy usage, processes data, and triggers alerts when user-defined thresholds are exceeded.

---

## 🚀 Tech Stack

- **Backend:** Java, Spring Boot
- **Architecture:** Microservices
- **Messaging:** Apache Kafka
- **Database:**
    - MySQL (relational data)
    - InfluxDB (time-series data)
- **Caching:** Spring Cache (performance optimization)
- **Security:** Keycloak (JWT-based authentication)
- **API Gateway:** Spring Cloud Gateway
- **Resilience:** Resilience4j (circuit breaker)
- **AI Integration:** Insight Service (LLM-based insights)

---

## 🏗️ Architecture Overview

![Microservices Flow](./microservices-flow.png)

---

## 🔗 Service URLs (Local)

| Service | Base URL |
|--------|---------|
| API Gateway | http://localhost:9000 |
| User Service | http://localhost:8080 |
| Device Service | http://localhost:8081 |
| Ingestion Service | http://localhost:8082 |
| Usage Service | http://localhost:8083 |
| Alert Service | http://localhost:8084 |
| Insight Service | http://localhost:8085 |

---

## 🔄 End-to-End Flow

1. **Client Authentication**
    - User authenticates via Keycloak
    - JWT token is issued

2. **API Gateway**
    - Central entry point for all requests
    - Handles routing, authentication, and security

3. **Ingestion Service**
    - Simulates IoT devices
    - Publishes energy usage events to Kafka

4. **Kafka (Event Streaming)**
    - Decouples ingestion and processing
    - Enables scalable asynchronous communication

5. **Usage Service**
    - Consumes energy events from Kafka
    - Stores time-series data in InfluxDB
    - Aggregates energy usage per device and user
    - Applies caching to reduce repeated API calls

6. **Device & User Services**
    - Provide device-to-user mapping
    - Store user data and alert thresholds (MySQL)

7. **Alert Service**
    - Triggered when energy consumption exceeds threshold
    - Sends notifications via Kafka events

8. **Insight Service (AI)**
    - Provides AI-driven energy insights
    - Uses aggregated usage data for recommendations

---

## ⚡ Key Features

- Real-time event-driven architecture using Kafka
- Time-series data processing using InfluxDB
- Microservices communication via REST APIs
- JWT-based authentication using Keycloak
- Circuit breaker implementation for fault tolerance
- AI-powered insights using LLM integration
