# Payment Service

The **Payment Service** is a core component of the **Sentinel Orchestrator**, a distributed state machine designed to manage transaction consistency across microservices. This service handles financial transactions, user balance management, and provides compensation logic (refunds) essential for the Saga design pattern.

It also integrates with Inventory Service to fetch product details and with Notification Service to send email alerts for payment success, insufficient balance, and refund events.

---
## Tech Stack
- Java
- Spring Boot
- Spring Web
- Spring security
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j
- Redis Cache
- MySQL
- Lombok
- Jakarta Validation
---
## Features

- **Saga Transaction Management:** Implements "Deduct" and "Compensate" (Refund) logic to maintain data integrity across distributed steps.
- **Fault Tolerance:** Integrated with **Resilience4j** for Circuit Breaker and Retry patterns to handle downstream failures (Inventory/Notification services).
- **High Performance Caching:** Utilizes **Redis** to cache user profile data, reducing database hits for frequently accessed information.
- **Asynchronous Notifications:** Leverages a dedicated `ExecutorService` thread pool to send payment success/failure emails without blocking the main transaction.
- **State Tracking:** Maintains a detailed `payment_table` to audit transaction IDs, workflow steps, and statuses (SUCCESS, COMPENSATED, INSUFFICIENT_BALANCE).
---
## Service Information

- **Service Name:** payment-service
- **Port:** `8002`
- **Context Path:** `/payment`

Base URL:

http://localhost:8002/payment

---
## API Endpoints

**User APIs**

1. Create User
POST /payment/user/create

2. Update User
PUT /payment/user/update?userId={userId}

3. Get User By Id
GET /payment/user/{userId}

4. Delete User
DELETE /payment/user/detele/{userId}

**Payment APIs**

5. Process Payment
POST /payment/process

6. Get Payment By Workflow Id
GET /payment/workflow/{workflowId}

7. Get All Payments By User Id
GET /payment/get/paymentsby/user/{userId}

8. Compensate Payment
GET /payment/compensate/{workflowId}
---
**Create User Request**

{
  "name": "Manikantan",
  "email": "manikantan1501@gmail.com",
  "phoneNumber": "1234567890",
  "address": "Narayanavanam",
  "balance": 5000
}

Create User Response

{
  "userId": 1,
  "name": "Manikantan",
  "email": "manikantan1501@gmail.com",
  "phoneNumber": "1234567890",
  "address": "Narayanavanam",
  "balance": 5000.0
}

---
**Update User Request**

{
  "name": "Thiru",
  "email": "manikantan1501@gmail.com",
  "phoneNumber": "1234567890",
  "address": "Puttur",
  "balance": 100000
}

Update User Response

{
  "userId": 2,
  "name": "Thiru",
  "email": "manikantan1501@gmail.com",
  "phoneNumber": "1234567890",
  "address": "Puttur",
  "balance": 100000.0
}

---
**Get User By Id Response Example**

{
  "userId": 2,
  "name": "Thiru",
  "email": "manikantan1501@gmail.com",
  "phoneNumber": "1234567890",
  "address": "Puttur",
  "balance": 100000.0
}

---
**Process Payment Request**

{
  "workflowId": 10023,
  "userId": 2,
  "productId": 3,
  "quantity": 40,
  "calledTimer": "2026-03-29T07:23:21.291Z",
  "workFlowStepId": "9407f8be-1916-4401-bc8e-27cd6094khjb"
}

Process Payment Success Response

{
  "transactionId": "1759ba25-8d94-4e8c-8dc7-e4a608af686d",
  "amount": 15998.0,
  "status": "SUCCESS: payment received of rupees:  15998.0",
  "errorMessage": null,
  "respondedTime": "2026-04-01T17:07:05.3728852"
}

Process Payment Insufficient Balance Response

{
  "transactionId": "aad56680-9bc6-47fe-9db8-2fbcb7878928",
  "amount": 244598.0,
  "status": "INSUFFICIENT_BALANCE",
  "errorMessage": "Insufficient balance in user account",
  "respondedTime": "2026-04-01T17:14:30.4030616"
}

---
**Payment By Workflow Id Response**

{
  "transactionId": "1759ba25-8d94-4e8c-8dc7-e4a608af686d",
  "amount": 15998.0,
  "status": "SUCCESS",
  "errorMessage": null,
  "respondedTime": "2026-04-01T17:11:47.2906579"
}

---
**Payments By User Id Response**

[
  {
    "transactionId": "1759ba25-8d94-4e8c-8dc7-e4a608af686d",
    "amount": 15998.0,
    "status": "SUCCESS",
    "errorMessage": null,
    "respondedTime": "2026-04-01T17:15:05.5410953"
  },
  {
    "transactionId": "98147eed-1f6a-4080-a4a6-ca1f44b78acb",
    "amount": 2410.0,
    "status": "SUCCESS",
    "errorMessage": null,
    "respondedTime": "2026-04-01T17:15:05.5410953"
  },
  {
    "transactionId": "aad56680-9bc6-47fe-9db8-2fbcb7878928",
    "amount": 244598.0,
    "status": "INSUFFICIENT_BALANCE",
    "errorMessage": "Insufficient balance in user account",
    "respondedTime": "2026-04-01T17:15:05.5410953"
  }
]

---
**Compensate Payment Response**

{
  "transactionId": "98147eed-1f6a-4080-a4a6-ca1f44b78acb",
  "amount": 2410.0,
  "status": "COMPENSATED",
  "errorMessage": null,
  "respondedTime": "2026-04-01T17:17:07.1568206"
}

---
## Database Tables

**User Table**
Stores user profile and wallet balance.

Main fields:

user_id

name

email

phone_number

address

balance


**Payment Table**
Stores payment workflow history.

Main fields:

payment_id

workflow_id

user_id

product_id

workflow_step_id

quantity

error_message

amount

payment_status

created_at

updated_at

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
**Payment Status Values**

PENDING

SUCCESS

COMPENSATED

COMPENSATED_ALREADY

INSUFFICIENT_BALANCE

TECHNICAL_FAILURE

---
### Workflow Summary
1. Request reaches Payment Service.
2. Payment Service fetches product details from Inventory Service.
3. It calculates the total amount using product price × quantity.
4. It checks user balance from the User table.
5. If balance is sufficient, the amount is deducted and payment is marked as success.
6. If balance is insufficient, payment is saved as failed.
7. Notification Service is called asynchronously for success, refund, or low balance cases.
8. Compensation can later restore the amount back to the user balance.
---
### Downstream Service Calls

Inventory Service
Used to fetch product details.

GET /inventory/product/{productId}

Notification Service
Used to send payment-related emails.

POST /notification/send/payment/success
POST /notification/send/payment/refunded
POST /notification/send/failed/balance
---
### Redis Cache

User details are cached using Redis to reduce repeated database reads.
Cache TTL: 10 minutes
---
### Configuration

Environment variables used:

DB_HOST

DB_USERNAME

DB_PASSWORD

REDIS_HOST
---
### application.properties

spring.application.name=payment-service

server.port=8002
server.servlet.context-path=/payment

spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/payment_db
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
Create the payment_db database.

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
Call the user and payment endpoints using the sample payloads above.

---
### Notes
The payment amount is calculated as product price × quantity.
Payment processing is linked to the orchestration workflow using workflowId.
Notification calls are triggered asynchronously after payment success, failure, or compensation.
This service is designed to work with the orchestrator-based saga flow.