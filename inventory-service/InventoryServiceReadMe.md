# Inventory Service

Inventory Service is a Spring Boot microservice that manages products, stock reservation, and stock release for an order workflow. It supports product CRUD operations, reservation tracking, Redis caching, and out-of-stock notification integration.
The **Inventory Service** is a core component of the Microservices Ecosystem, responsible for product lifecycle management and stock orchestration. It utilizes the **Saga Pattern** to ensure data consistency across distributed transactions through dedicated reservation and compensation logic.
---
## Tech Stack
- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j
- Redis Cache
- MySQL
- Lombok
- Jakarta Validation
---
## Features
- **Distributed Transaction Management:** Implements `reserveStock` and `releaseStock` (compensation) logic to support Saga-based workflows.
- **High-Performance Caching:** Integrated **Redis** caching for product lookups to reduce database load and improve response times.
- **Asynchronous Alerts:** Offloads "Out of Stock" email notifications to a dedicated thread pool to maintain low latency in core business logic.
- **Resilience & Fault Tolerance:** Protected inter-service communication with **Resilience4j** Circuit Breaker and Retry mechanisms.
- **Audit Trail:** Persists all stock reservations in a `reservation_table` with unique UUIDs for end-to-end traceability.
---
## Service Information

- **Service Name:** inventory-service
- **Port:** `8003`
- **Context Path:** `/inventory`

Base URL:
http://localhost:8003/inventory
---
## API Endpoints
Product APIs

1. Create Product
POST /inventory/product/create

2. Get Product By Id
GET /inventory/product/{productId}

3. Update Product
PUT /inventory/product/update?productId={productId}

4. Delete Product
DELETE /inventory/product/delete/{productId}

Stock Reservation APIs

5. Reserve Stock
POST /inventory/process

6. Release Stock
POST /inventory/compensate?workflowId={workflowId}

7. Get Reservation Details
GET /inventory/reservation/{reservationId}

---
Create Product Request

{
  "productName": "Wireless Bluetooth Headphones",
  "price": 1059.99,
  "stockQuantity": 120,
  "status": "AVAILABLE",
  "description": "High-quality over-ear headphones with noise cancellation, 20-hour battery life, and built-in microphone."
}

Create Product Response

{
  "productId": 1,
  "productName": "Wireless Bluetooth Headphones",
  "price": 1059.99,
  "stockQuantity": 120,
  "status": "AVAILABLE",
  "description": "High-quality over-ear headphones with noise cancellation, 20-hour battery life, and built-in microphone.",
  "updatedAt": "2026-04-01T15:25:39.9511287"
}
---
Update Product Request

{
  "productName": "Gaming Laptop",
  "price": 122299.00,
  "stockQuantity": 25,
  "status": "AVAILABLE",
  "description": "Powerful gaming laptop with Intel i7 processor, 16GB RAM, RTX 3060 GPU, and 512GB SSD."
}

Update Product Response

{
  "productId": 2,
  "productName": "Gaming Laptop",
  "price": 122299.0,
  "stockQuantity": 25,
  "status": "AVAILABLE",
  "description": "Powerful gaming laptop with Intel i7 processor, 16GB RAM, RTX 3060 GPU, and 512GB SSD.",
  "updatedAt": "2026-04-01T15:32:05.8227977"
}
---
Get Product By Id Response Example

{
  "productId": 1,
  "productName": "Wireless Bluetooth Headphones",
  "price": 1059.99,
  "stockQuantity": 120,
  "status": "AVAILABLE",
  "description": "High-quality over-ear headphones with noise cancellation, 20-hour battery life, and built-in microphone.",
  "updatedAt": "2026-04-01T15:25:39.9511287"
}
---
Reserve Stock Request

{
  "workflowId": 10010,
  "userId": 2,
  "productId": 1,
  "workFlowStepId": "0fe6586d-554f-4ee9-8aa4-a2d0963a206e",
  "quantity": 10,
  "calledTimer": "2026-03-30T10:36:52.839Z"
}

Reserve Stock Success Response

{
  "transactionId": "fc3e40bd-14c4-4295-b350-1af3e8b756a8",
  "amount": 10599.9,
  "status": "SUCCESS",
  "errorMessage": null,
  "respondedTime": "2026-04-01T15:38:16.951623"
}
Reserve Stock Failure Response

{
  "transactionId": "672ebf01-67ee-4967-87f1-5218cb32b531",
  "amount": 1059990.0,
  "status": "FAILED",
  "errorMessage": "Insufficient stock",
  "respondedTime": "2026-04-01T15:40:00.7202295"
}
---
Release Stock Request

POST /inventory/compensate?workflowId=10010

Release Stock Response

{
  "transactionId": "fc3e40bd-14c4-4295-b350-1af3e8b756a8",
  "amount": 10599.9,
  "status": "COMPENSATED",
  "errorMessage": null,
  "respondedTime": "2026-04-01T15:41:45.0221183"
}
---

Get Reservation Details Response Example

Reservation(reservationId=fc3e40bd-14c4-4295-b350-1af3e8b756a8, workflowId=10010, userId=2, status=RELEASED, product=Product(productId=1, productName=Wireless Bluetooth Headphones, price=1059.99, stockQuantity=120, status=AVAILABLE, description=High-quality over-ear headphones with noise cancellation, 20-hour battery life, and built-in microphone., updatedAt=2026-04-01T15:41:46.239298), quantity=10, WorkFlowStepId=0fe6586d-554f-4ee9-8aa4-a2d0963a206e, reservedAt=2026-04-01T15:38:16.998279, updatedAt=2026-04-01T15:41:46.239298)

---
## Database Tables

**Product Table**
Stores product catalog details.

Main fields:

product_id

product_name

price

stock_quantity

status

description

updated_at


**Reservation Table**
Stores stock reservation and release history.

Main fields:

reservation_id

workflow_id

user_id

status

product_id

quantity

workflow_step_id

reserved_at

updated_at

---
### Enum Values

**StockStatus**

AVAILABLE

RESERVATION

OUT_OF_STOCK

**ReservationStatus**

RESERVED

RELEASED

FAILED
---
## Security

This service uses JWT-based authentication.

- Public endpoints: `/...`
- Protected endpoints require:
  `Authorization: Bearer <token>`
- The API Gateway validates JWT before routing.
- This service also validates JWT locally.
- Feign calls propagate the `Authorization` header to downstream services.
---
### Workflow Summary
1. Product is created and stored in the database.
2. Product details can be fetched using product id.
3. When an order arrives, stock is reserved.
4. If stock is available, quantity is reduced and reservation is stored.
5. If stock is insufficient, reservation is marked as failed and out-of-stock notification is triggered.
6. If order fails later in the saga, stock can be released using the compensation endpoint.
7. Reservation details can be fetched using reservation id.
---
### Redis Cache
Product details are cached using Redis to reduce database hits for frequent read requests.

Cache TTL: 10 minutes
---
### Downstream Service Call
Inventory Service calls Notification Service when stock is not available.

Notification endpoint used:
POST /notification/send/failed/outofstock

---
### Configuration
Environment variables used:

DB_HOST

DB_USERNAME

DB_PASSWORD

REDIS_HOST

---
### application.properties

spring.application.name=inventory-service

server.port=8003
server.servlet.context-path=/inventory

spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/inventory_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=6379
---
### Local Run

1. Start MySQL
Create the inventory_db database.

2. Start Redis
Make sure Redis is running on port 6379.

3. Set environment variables
Example:

export DB_HOST=localhost
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost

4. Run the application
mvn spring-boot:run

5. Test using Postman
Call product and stock reservation endpoints using the sample payloads above.
---
### Notes
Product data is cached in Redis for faster reads.
Reservation records are stored in MySQL for tracking and compensation.
This service is used by the orchestrator-based saga flow.
The service calculates amount as price × quantity.