# Notification Service

Notification Service is a Spring Boot microservice that sends email notifications for different workflow events such as order success, payment success, payment refund, low balance failure, and out-of-stock cancellation.

It receives a workflow payload, fetches user and product details from downstream services using Feign clients, builds the email body, sends the email through SMTP, and stores notification audit data in MySQL.
---
## Tech Stack
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j
- JavaMailSender
- MySQL
- Lombok
- Jakarta Validation
---
## Features
- Send email for order success
- Send email for payment success
- Send email for refund confirmation
- Send email for low balance failure
- Send email for out-of-stock cancellation
- Store notification history in database
- Retry and circuit breaker protection for downstream service calls
---
## Service Information

- **Service Name:** notification-service

- **Port:** `8004`

- **Context Path:** `/notification`

Base URL:
http://localhost:8004/notification
---
## API Endpoints

1. Send Order Success Mail

**POST /notification/send/order/success**

2. Send Failed Due to Low Balance Mail

**POST /notification/send/failed/balance**

3. Send Failed Due to Out of Stock Mail

**POST /notification/send/failed/outofstock**

4. Send Payment Success Mail

**POST /notification/send/payment/success**

5. Send Payment Refunded Mail

**POST /notification/send/payment/refunded**
---
## Request Payload

All endpoints accept the same request structure.

{

 "workflowId": 100,
 "userId": 1,
 "productId": 1,
 "quantity": 2,
 "calledTimer": "2026-03-29T19:02:13.261Z",
 "workFlowStepId": "dd7ab325-c715-401e-b846-e115a61ca392"

}
---
## Request Field Details

workflowId → unique workflow id
userId → user id
productId → product id
quantity → ordered quantity
calledTimer → timestamp when workflow was triggered
workFlowStepId → workflow step identifier
---
## Response Format

The service currently returns a plain string response.

**Example success response:**

SUCCESSFULLY EMAIL HAS BEEN SENT PaymentSuccess Mail Details Id: 1

Example failure response when product service is unavailable:
Product Server is unavailable currently please try again after some time

Example failure response when user service is unavailable:
User Server is unavailable currently please try again after some time
---
## Database

Table: notification_details
This table stores notification audit history.

Columns
notification_id → primary key
workflow_id
user_id
product_id
quantity
status
error_message
workflow_step_id
amount
called_timer
---
## Database Sample Records

notification_id	workflow_id   user_id	product_id   quantity	amount	 	status	 				        error_message		workflow_step_id



1		100		1	1		4	4239.96   EMAIL_SENT_SUCCESSFULLY_PAYMENT_SUCCESS   		    Nothing	dd7ab325-c715-401e-b846-e115a61ca392

2		100		1	1		2	2119.98   EMAIL_SENT_SUCCESSFULLY_PAYMENT_REFUNDED	  	    Nothing	dd7ab325-c715-401e-b846-e115a61ca392

3		100		1	1		2	2119.98	  EMAIL_SENT_SUCCESSFULLY_ORDER_SUCCESS	                    Nothing	dd7ab325-c715-401e-b846-e115a61ca392

4		100		1	1		2	2119.98	  EMAIL_SENT_SUCCESSFULLY_FAILED_DUE_PRODUCT_OUT_OF_STOCK   Nothing	dd7ab325-c715-401e-b846-e115a61ca392

5		100		1	1		2	2119.98	  EMAIL_SENT_SUCCESSFULLY_FAILED_DUE_LOW_BALANCE	    Nothing	dd7ab325-c715-401e-b846-e115a61ca392
---
## Workflow Summary

1. Request reaches Notification Service.
2. It fetches product details from Inventory Service.
3. It fetches user details from Payment Service.
4. It prepares email content based on notification type.
5. Email is sent using SMTP.
6. Notification details are saved in MySQL.
7. Service returns the mail status and DB id.
---
## Notification Types
ORDER_SUCCESS
PAYMENT_SUCCESS
PAYMENT_REFUNDED
FAILED_DUE_LOW_BALANCE
FAILED_DUE_PRODUCT_OUT_OF_STOCK
---
## Downstream Service Calls

Inventory Service
Used to fetch product details.
GET /inventory/product/{productId}

Payment Service
Used to fetch user details.
GET /payment/user/{userId}
---
## Configuration

The service uses the following environment variables:
EMAIL_ID
PASSWORD_FOR_EMAIL_ID
DB_HOST
DB_USERNAME
DB_PASSWORD
---
## application.properties

### Important configuration values:

spring.application.name=notification-service
server.port=8004
server.servlet.context-path=/notification

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_ID}
spring.mail.password=${PASSWORD_FOR_EMAIL_ID}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:3306/notification_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
---
## Resilience Configuration
The service is hardened against failures in the Inventory or Payment services using:
* Circuit Breaker: Opens after 50% failure rate; waits 10s before retrying.
* Retry: 3 attempts with a 100ms backoff interval.
* Fallback: Provides "Unknown User" or "Unavailable Product" placeholders to ensure the service remains operational.
---
## Local Run
1. Start MySQL
Make sure MySQL is running and database notification\_db exists.

2. Set environment variables
Example:
export EMAIL_ID=your_email@gmail.com
export PASSWORD_FOR_EMAIL_ID=your_app_password
export DB_HOST=localhost
export DB_USERNAME=root
export DB_PASSWORD=your_password

3. Run the application
Use your IDE or:

mvn spring-boot:run

4. Test with Postman
Call any of the notification endpoints with the request payload given in the Example Request and Response payload file.
---
### Notes

The service calculates amount using: product price × quantity
Email content is generated based on the notification type.
Notification history is stored for traceability and debugging.
This service is designed to work with the orchestrator-based saga flow.
---
### Example Success Response

SUCCESSFULLY EMAIL HAS BEEN SENT Order Mail Details Id: 3

Example Failure Response

FAILED TO SEND EMAIL: <reason>

