# 🧾 Bulk Transaction Service

## 📘 Overview
The **Bulk Transaction Service** is a Spring Boot–based microservice that enables users to process multiple financial transactions in a single request.  
It integrates securely with an external **Transaction Service** and provides full observability, fault tolerance, and role-based access control.

---

## 🚀 Key Features
✅ **JWT Authentication & Authorization** — Secure access using JSON Web Tokens.  
✅ **Role-Based Access Control (RBAC)** — Separate privileges for `ROLE_USER` and `ROLE_ADMIN`.  
✅ **Parallel Transaction Processing** — Executes multiple transactions concurrently.  
✅ **Resilience4j Integration** — Circuit Breaker and Retry patterns for fault tolerance.  
✅ **Metrics & Health Checks** — `/actuator/**` endpoints for monitoring.  
✅ **Comprehensive Test Suite** — Unit & Integration tests using H2 in-memory database.

---

## 🧱 Project Structure
### bulk-transactions/
#### ├── controller/ REST controllers (Auth & Transaction endpoints)
#### ├── service/ Core business logic
#### ├── client/ WebClient client to Transaction Service
#### ├── dto/ Request & Response DTOs
#### ├── security/ JWT filters, service, and config
#### ├── exception/ Global exception handling
#### └── repository/ User data persistence


---

## ⚙️ Configuration

### `application.yml`
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:bulkdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

transaction-service:
  base-url: http://localhost:8082

management:
  endpoints:
    web:
      exposure:
        include: health, metrics

application:
  jwt:
    secret: your-strong-secret-key
```
___

## 🧩 API Endpoints
### 🔐 Authentication


| Endpoint                | Method | Description                        |
| ----------------------- | ------ | ---------------------------------- |
| `/api/v1/auth/register` | `POST` | Register a new user                |
| `/api/v1/auth/login`    | `POST` | Authenticate and receive JWT token |


### 💳 Transactions
| Endpoint                    | Method | Roles       | Description                   |
| --------------------------- | ------ | ----------- | ----------------------------- |
| `/api/v1/bulk-transactions` | `POST` | USER, ADMIN | Process multiple transactions |


### ⚙️ System Monitoring
| Endpoint            | Method | Roles | Description          |
| ------------------- | ------ | ----- | -------------------- |
| `/actuator/health`  | `GET`  | ADMIN | Check service health |
| `/actuator/metrics` | `GET`  | ADMIN | View system metrics  |

___

## 🧪 Example Usage
### 1️⃣ Register a User
curl -X POST /api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "Password...",
    "role": "ROLE_USER"
  }'

### 2️⃣ Login to Get Token
curl -X POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "Password..."
  }'


### Response:

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5..."
}

### 3️⃣ Submit Bulk Transactions
curl -X POST /api/v1/bulk-transactions \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "batchId": "batch-101",
    "transactions": [
      {"transactionId": "tx-001", "fromAccount": "SRC1", "toAccount": "DEST1", "amount": 1500},
      {"transactionId": "tx-002", "fromAccount": "SRC2", "toAccount": "DEST2", "amount": 2300}
    ]
  }'

## 🧰 Testing
### Run All Tests
mvn test


The test suite covers:

- ✅ User registration & login

- ✅ Token validation & role-based access

- ✅ Bulk transaction success, partial, and failure scenarios

- ✅ Fallback logic when Transaction Service is down

- ✅ Health and metrics endpoint access control

### Example Integration Test

- ROLE_USER: Can access /api/v1/bulk-transactions

- ROLE_ADMIN: Can access /actuator/**

- Unauthorized users receive 401 Unauthorized

___

## 🧑‍💻 Local Development
### Run the Service
mvn spring-boot:run

### Build Executable JAR
mvn clean package

___

## 🧩 Integration with Transaction Service

### Each transaction is forwarded to the Transaction Service:

POST /api/v1/transactions

### Example Transaction Service Response:
{
  "transactionId": "tx-001",
  "status": "SUCCESS",
  "message": "Transaction processed successfully"
}


If the Transaction Service is unavailable, Resilience4j Circuit Breaker triggers a fallback:

{
  "error": "TransactionService unavailable"
}

___

## 🩺 Health & Monitoring
### Health Endpoint
curl -H "Authorization: Bearer <ADMIN_TOKEN>" /actuator/health

### Metrics Endpoint
curl -H "Authorization: Bearer <ADMIN_TOKEN>" /actuator/metrics

___

## 🔒 Role & Access Matrix
### Role	Accessible Endpoints	Description
- ROLE_USER	/api/v1/bulk-transactions	Submit bulk transactions
- ROLE_ADMIN	/actuator/**	View system health and metrics
- Public	/api/v1/auth/**	Register & login only

___

## 🧱 Tech Stack

- ☕ Java 17

- ⚡ Spring Boot 3.x

- 🔐 Spring Security + JWT

- 💪 Resilience4j (CircuitBreaker, Retry)

- 📊 Micrometer + Actuator

- 🧠 H2 Database (for testing)

- 🧪 JUnit 5 + Mockito

___

## ▶️ Run Locally with Docker
### Step 1: Build the JAR
mvn clean package

### Step 2: Build and Run Containers
docker compose up -d --build

### Step 3: Verify Running Containers
docker ps

## 🧹 Reset / Clear Docker Database

### If you want to clear all MySQL data:

docker compose down -v
