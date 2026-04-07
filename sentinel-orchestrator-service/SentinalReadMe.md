# Sentinel Orchestrator Service

The **Sentinel Orchestrator Service** is the central coordinator of the Sentinel microservices ecosystem. It functions as a distributed state machine, managing the execution flow of multi-step transactions (Saga Pattern) across the Payment, Inventory, and Notification services.

The service supports workflow start, retry, resume of pending workflows, status-based listing, and manual compensation handling.
---
## Tech Stack
- Java
- Spring Boot
- Spring Web
- spring security
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j
- MySQL
- Lombok
- Jakarta Validation
- Scheduled Jobs
---
## Features
* **State Machine:** Uses `WorkFlowStatus` (CREATED, IN_PROGRESS, COMPLETED, FAILED, etc.) to track the lifecycle of every request.
* **Saga Orchestration:** Implements a command-based Saga. If a step fails (e.g., Inventory), the Orchestrator automatically triggers compensating transactions (e.g., Payment Refund).
* **Self-Healing Scheduler:** Includes a background job (`WorkFlowSecular`) that automatically resumes `IN_PROGRESS` workflows and retries `RETRY_QUERY` states every 60 seconds.
* **Comprehensive Resilience:** Implements the full Resilience4j stack:
    * **Circuit Breaker:** Prevents cascading failures.
    * **Retry:** Automatically retries Feign exceptions.
    * **Bulkhead:** Limits concurrent calls to protect service resources.
    * **Rate Limiter:** Controls the throughput to the Notification service.
---
## Service Information
- **Service Name:** sentinel-orchestrator-service
- **Port:** `8001`
- **Context Path:** `/workflow`

Base URL:

http://localhost:8001/workflow
---
## API Endpoints

1. Start Workflow
POST /workflow/start

2. Get Workflow By Id
GET /workflow/id/{workFlowId}

3. Retry Workflow
GET /workflow/id/{workFlowId}/retry

4. Resume Pending Workflows
GET /workflow/resume

5. Get Workflows By Status
GET /workflow/status/{status}

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
**Start Workflow Request**

{
  "workflowType": "Electronic's WorkFlow",
  "userId": 3,
  "productId": 3,
  "quantity": 10,
  "message": "booking for electronics items"
}
---
**Workflow Response Format**

The workflow response contains:

workFlowId

workFlowType

status

currentStep

payload

lastError

createdAt

updatedAt

steps


Each workflow step contains:

stepId

workFlowId

stepOrder

stepName

status

requestPayload

responsePayload

errorMessage

startedAt

finishedAt

---
## Sample Workflow Outcomes
**1. Payment Failure**
When payment fails, the workflow stops at PAYMENT and the workflow status becomes FAILED.

Example status:
FAILED

Example current step:
PAYMENT

**2. Inventory Failure After Payment Success**
When payment succeeds but inventory fails, the workflow moves into compensation flow and the status becomes COMPENSATED_PAYMENT.

Example status:
COMPENSATED_PAYMENT

Example current step:
INVENTORY

**3. Notification Failure**
When payment and inventory succeed but notification fails, the workflow status becomes RETRY_QUERY.

Example status:
RETRY_QUERY

Example current step:
NOTIFICATION

**4. Full Success**
When all steps succeed, the workflow status becomes COMPLETED.

Example status:
COMPLETED


Example current step:
NOTIFICATION

---
### Workflow States

**Workflow Status**

CREATED
IN_PROGRESS
COMPLETED
FAILED
RETRY_QUERY
COMPENSATING_PAYMENT
COMPENSATING_INVENTORY
COMPENSATED_INVENTORY
COMPENSATED_PAYMENT

**Step Status**

PENDING
SUCCESS
FAILED
TECHNICAL_FAILURE

**Step Execution Order**

1. PAYMENT
2. INVENTORY
3. NOTIFICATION

---
## Database Tables

**1. workflows**
Stores the main workflow state.

Main columns:

workflow_id

user_id

product_id

workflow_type

quantity

workflow_status

current_step

payload

last_error

retries

created_at

updated_at

retried_at


**2. workflow_steps**
Stores each step execution history.

Main columns:

step_id

workflow_id

step_order

step_name

step_status

input_payload

output_payload

error_details

started_at

finished_at

---
### Workflow Summary
1. The request enters the orchestrator.
2. The orchestrator stores the workflow and serializes the payload.
3. It executes PAYMENT first.
4. If payment succeeds, it executes INVENTORY.
5. If inventory succeeds, it executes NOTIFICATION.
6. If notification fails, the workflow is marked for retry.
7. Scheduled jobs resume pending workflows and retry-query workflows.
8. Full workflow and step history is saved in the database.

---
### Downstream Services:

Payment Service
Used for payment processing and compensation.

Inventory Service
Used for stock reservation and stock release.

Notification Service
Used for sending order-success email notifications.
---
### Configuration

Environment variables used:

DB_HOST

DB_USERNAME

DB_PASSWORD

---
### Local Run

1. Start MySQL
Create the database orchestrator_db.

2. Set environment variables

Example:
export DB_HOST=localhost
export DB_USERNAME=root
export DB_PASSWORD=your_password

3. Run the application
mvn spring-boot:run

4. Test using Postman
Use the sample payloads above to start workflows and inspect workflow history.

---
### Notes:
The orchestrator is the main brain of the system.
It stores every workflow and step execution.
Notification failures go into retry flow, and the scheduler can resume them later.
The project currently demonstrates saga orchestration with compensation and retry behavior.

