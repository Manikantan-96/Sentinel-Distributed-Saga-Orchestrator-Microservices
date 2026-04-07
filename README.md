# 🚀 Sentinel: Distributed Saga Orchestrator Microservices

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Services Overview](#services-overview)
- [Getting Started](#getting-started)
- [Workflow Execution](#workflow-execution)
- [Resilience Patterns](#resilience-patterns)
- [Security](#security)
- [Docker Deployment](#docker-deployment)
- [Design Decisions](#design-decisions)
- [Author](#author)
---
## 🎯 Overview

**Sentinel** is an advanced **Orchestration-based Saga Microservices Platform** designed to coordinate distributed transactions across **Payment**, **Inventory**, and **Notification** services. It implements the Saga pattern with automatic compensation, ensuring workflow consistency, failure recovery, and operational resilience across a distributed system.

### Problem Solved
In distributed systems, coordinating multi-step transactions across independent services is challenging. Sentinel solves this by:
- ✅ Orchestrating complex workflows with multiple service dependencies
- ✅ Automatically handling failures with compensating transactions (saga rollback)
- ✅ Providing self-healing capabilities through scheduled job recovery
- ✅ Maintaining audit trails of all transactions for compliance and debugging
- ✅ Protecting services with comprehensive resilience patterns (Circuit Breaker, Retry, Bulkhead, Rate Limiting)

---
## ✨ Key Features

### 🔄 **Orchestration-Based Saga Pattern**
- Central orchestrator manages workflow state machine
- Automatic compensation on failure (e.g., refund payment when inventory fails)
- Deterministic workflow execution with predefined steps: PAYMENT → INVENTORY → NOTIFICATION

### 🛡️ **Comprehensive Resilience**
- **Circuit Breaker**: Prevents cascading failures across services
- **Retry Logic**: Automatic retries with exponential backoff
- **Bulkhead Pattern**: Limits concurrent calls to protect resources
- **Rate Limiting**: Controls throughput to prevent service overload
- **Time Limiter**: Enforces request timeouts

### 🔧 **Self-Healing Capabilities**
- Scheduled jobs (every 60 seconds) automatically resume IN_PROGRESS workflows
- Retry mechanism for failed notification steps
- Manual retry capability for specific workflows

### 💾 **Centralized Configuration**
- Spring Cloud Config Server for dynamic configuration management
- Environment-based property overrides
- Hot configuration refresh support

### 📊 **Service Registry & Discovery**
- Netflix Eureka for dynamic service registration
- Load-balanced inter-service communication
- Automatic service discovery

### 🔐 **JWT Authentication**
- Token-based security across all services
- API Gateway JWT validation
- Service-to-service JWT propagation
- Token-based Authorization - Per-endpoint access control

### 💰 **Business Domain Features**
- User wallet management with balance tracking
- Product inventory with stock reservation/release
- Payment processing with transaction auditing
- Email notifications for order and payment events
- Redis caching for high-performance data access

### 📝 **Complete Audit Trail**
- All workflows stored in database with full execution history
- Step-by-step transaction logging
- Error tracking and compensation history
- Payment, inventory, and notification transaction records

---
## 🏗️ Architecture

### System Architecture Diagram

┌─────────────────────────────────���───────────────────────────────────┐
│                         CLIENT APPLICATIONS                             │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY (Port 8080)                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ • JWT Authentication & Authorization                         │   │
│  │ • Rate Limiting (Redis-based)                                │   │
│  │ • Circuit Breaker Pattern (Resilience4j)                     │   │
│  │ • Request Retry Logic                                        │   │
│  │ • Service Routing & Load Balancing                           │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Routes:                                                      │   │
│  │ • /payment/** → PAYMENT-SERVICE                              │   │
│  │ • /inventory/** → INVENTORY-SERVICE                          │   │
│  │ • /workflow/** → SENTINEL-ORCHESTRATOR-SERVICE               │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   │ HTTP/REST
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│          SENTINEL ORCHESTRATOR SERVICE (Port 8001, /workflow)                       │
│                                                                                     │
│  ┌────���─────────────────────────────────────────────────────────────────┐       │
│  │ Orchestration Engine (State Machine)                                     │       │
│  │ ├─ Workflow Management (CREATED → IN_PROGRESS → COMPLETED/FAILED)        │       │
│  │ ├─ Saga Orchestration (Payment → Inventory → Notification)               │       │
│  │ ├─ Compensation Handling (Auto-rollback on failure)                      │       │
│  │ ├─ Retry & Resume Logic (Scheduled jobs every 60s)                       │       │
│  │ └─ Circuit Breaker (5 instances for resilience)                          │       │
│  └──────────────────────────────────────────────────────────────────────────┘       │
│                                                                                     │
└───────────┼────────────────────────────────────────────┼────────────────────────────┘
            │                                            │
			▼			            ┌────────────────────┼──────────────────┐
┌──────────────────────┐ 		    │                    │                  │
│   Orchestrator DB    │		    ▼                    ▼                  ▼
│   (MySQL:3310)       │	  ┌─────────────────┐  ┌─────��─────────┐  ┌──────────────────┐   
│                      │	  │  PAYMENT SVC    │  │ INVENTORY SVC   │  │ NOTIFICATION SVC │
│ • workflows table    │	  │  (Port 8002)    │  │ (Port 8003)     │  │ (Port 8004)      │
│ • workflow_steps tbl │	  │                 │  │                 │  │                  │
└──────────────────────┘	  │ • Deduct Fund   │  │ • Reserve Stock │  │ • Send Emails    │
							              │ • Refund        │  │ • Release Stock │  │ • SMTP Gateway   │
							              │ • Redis Cache   │  │ • Redis Cache   │  │ • Async Threads  │
							              │ • User Mgmt     │  │ • Product Mgmt  │  │                  │
							              └────────┬────────┘  └────────┬────────┘  └────────┬─────────┘
									                   │                    │                    │
								                	   ▼                    ▼                    ▼
							             ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐
							             │ Payment DB      │  │ Inventory DB    │  │ Notification DB  │
							             │ (MySQL:3307)    │  │ (MySQL:3308)    │  │ (MySQL:3309)     │
							             └─────────────────┘  └─────────────────┘  └──────────────────┘
                


┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                INFRASTRUCTURE LAYER                                 │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐   │
│  │ Service      │    │ Config       │    │ Redis Cache  │    │ Sentinel Network │   │
│  │ Registry     │    │ Server       │    │ (Port 6379)  │    │ (Bridge Network) │   │
│  │ Eureka       │    │ (Port 8888)  │    │              │    │                  │   │
│  │ (Port 8761)  │    │              │    │ • Payment    │    │ • Inter-service  │   │
│  │              │    │ • Centralized│    │ • Inventory  │    │   communication  │   │
│  │ • Service    │    │   configs    │    │ • Session    │    │ • Service disc.  │   │
│  │   discovery  │    │ • Property   │    │   cache      │    │                  │   │
│  │ • Health     │    │   mgmt       │    │              │    │                  │   │
│  │   checks     │    │              │    │              │    │                  │   │
│  └──────────────┘    └──────────────┘    └──────────────┘    └──────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

### Workflow Execution Flow

┌─────────────────────────────────────────────────────────────────────────────┐
│                      WORKFLOW LIFECYCLE                                     │
└─────────────────────────────────────────────────────────────────────────────┘

1. REQUEST INITIATION

┌─────────────────────────────────────────┐
│ API GATEWAY - Port 8080                 │
│ - Validates JWT                         │
│ - Applies Rate Limiting                 │
│ - Routes to Orchestrator                │
└───────────────┬─────────────────────────┘
                │
                ▼
2. WORKFLOW CREATION
┌─────────────────────────────────────────┐
│ ORCHESTRATOR - Port 8001                │
│ - Validates JWT                         │
│ - Creates Workflow (status: INITIATED)  │
│ - Publishes saga_event (WORKFLOW_START) │
│ - Initiates payment step                │
└────────────┬────────────────────────────┘
             ↓
                │
                ▼
3. PAYMENT STEP (Step 1)
   ┌─────────────────────────────────────┐
   │ POST /payment/process                │
   ├─────────────────────────────────────┤
   │ SUCCESS? ──→ Amount Deducted         │
   │             Status: IN_PROGRESS      │
   │                                      │
   │ FAIL? ──→ Balance Insufficient       │
   │           Status: FAILED             │
   │           (Stop execution)           │
   └────────────┬────────────────────────┘
                │
                ▼
4. INVENTORY STEP (Step 2)
   ┌─────────────────────────────────────┐
   │ POST /inventory/process              │
   ├─────────────────────────────────────┤
   │ SUCCESS? ──→ Stock Reserved          │
   │             Status: IN_PROGRESS      │
   │                                      │
   │ FAIL? ──→ Insufficient Stock         │
   │           Status: COMPENSATING      │
   │           (Trigger Refund)           │
   └────────────┬────────────────────────┘
                │
                ▼
5. COMPENSATION (if needed)
   ┌─────────────────────────────────────┐
   │ GET /payment/compensate              │
   ├─────────────────────────────────────┤
   │ Refund Amount                        │
   │ Release Stock                        │
   │ Status: COMPENSATED_*                │
   └────────────┬────────────────────────┘
                │
                ▼
6. NOTIFICATION STEP (Step 3)
   ┌─────────────────────────────────────┐
   │ POST /notification/send/*            │
   ├─────────────────────────────────────┤
   │ SUCCESS? ──→ Email Sent              │
   │             Status: COMPLETED        │
   │                                      │
   │ FAIL? ──→ Mark for Retry             │
   │           Status: RETRY_QUERY        │
   │           (Scheduler retries in 60s) │
   └────────────┬────────────────────────┘
                │
                ▼
7. SCHEDULED RECOVERY (every 60 seconds)
   ┌─────────────────────────────────────┐
   │ WorkflowScheduler Job                │
   ├─────────────────────────────────────┤
   │ • Resume IN_PROGRESS workflows       │
   │ • Retry RETRY_QUERY workflows        │
   │ • Auto-recovery capability           │
   └─────────────────────────────────────┘

---

## 🛠️ Technology Stack

### Core Framework
| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 4.0.4 | Application Framework |
| Spring Cloud | 2025.1.1 | Microservices Orchestration |

### Service Communication & Discovery
| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Cloud Netflix Eureka | Latest | Service Registry & Discovery |
| Spring Cloud OpenFeign | Latest | Declarative HTTP Client |
| Spring Cloud Gateway | Latest | API Gateway & Routing |

### Resilience & Fault Tolerance
| Component | Version | Purpose |
|-----------|---------|---------|
| Resilience4j | Latest | Circuit Breaker, Retry, Bulkhead, Rate Limiter |
| Spring Cloud Circuit Breaker | Latest | Circuit Breaker Abstraction |

### Data & Caching
| Technology | Version | Purpose |
|-----------|---------|---------|
| MySQL | 8.0 | Persistent Data Storage |
| Redis | 7-alpine | High-Speed Caching |
| Spring Data JPA | Latest | ORM Framework |
| Hibernate | Latest | JPA Implementation |

### Security & Authentication
| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Security | Latest | Security Framework |
| JJWT (JWT) | 0.13.0 | JWT Token Management |

### Additional Libraries
| Library | Version | Purpose |
|---------|---------|---------|
| Lombok | Latest | Code Generation |
| Jakarta Validation | Latest | Bean Validation |
| Jackson | Latest | JSON Processing |
| Mail Sender | Latest | Email Notifications |

### DevOps & Containerization
| Tool | Version | Purpose |
|------|---------|---------|
| Docker | Latest | Container Orchestration |
| Docker Compose | 3.8 | Multi-container Setup |
| Maven | 4.0.4 | Build Tool |
---

## 📁 Project Structure

sentinel-distributed-saga-orchestrator-microservices/
│
├── api-gateway/                             # API Gateway Service
│   ├── src/
│   │   ├── main/
│   │       ├── java/                         # Java source code
│   │       │   └── com/...                   # Package structure
│   │       └── resources/
│   │            └──application.properties    # Spring configuration
│   ├── pom.xml                               # Maven dependencies
│   └── Dockerfile
│
├── sentinel-orchestrator-service/         # Central Orchestrator
│   ├── src/
│   │   ├── main/
│   │       ├── java/
│   │       │   ├── Controller/            # API endpoints Exception Handling
│   │       │   ├── Service/               # hide Business logic
│   │       │   ├── ServiceImpl            # Business logic
│   │       │   ├── Repository/            # Data access
│   │       │   ├── Entity/                # JPA entities
│   │       │   ├── DTO/                   # Data transfer objects
│   │       │   ├── Enum/                  # Enumerations for states
│   │       │   ├── Scheduler/             # Scheduled jobs
│   │       │   ├── Feign/                 # Feign clients
│   │       │   ├── Security/              # spring security
│   │       └── resources/
│   ├── pom.xml
│   ├── Dockerfile
│   └── SentinalReadMe.md
│
├── payment-service/                       # Payment Processing
│   ├── src/
│   │   ├── main/
│   │       ├── java/
│   │       │   ├── Controller/            # API endpoints Exception Handling
│   │       │   ├── Service/               # hide Business logic
│   │       │   ├── ServiceImpl            # Business logic
│   │       │   ├── Repository/            # Data access
│   │       │   ├── Entity/                # JPA entities
│   │       │   ├── DTO/                   # Data transfer objects
│   │       │   ├── Enum/                  # Enumerations for states
│   │       │   ├── RedisConfig/           # Redis configuration
│   │       │   ├── Feign/                 # Feign clients
│   │       │   ├── Security/              # spring security
│   │       └── resources/
│   ├── pom.xml
│   ├── Dockerfile
│   └── PaymentServiceReadMe.md
│
├── inventory-service/                     # Inventory Management
│   ├── src/
│   │   ├── main/
│   │       ├── java/
│   │       │   ├── Controller/            # API endpoints Exception Handling
│   │       │   ├── Service/               # hide Business logic
│   │       │   ├── ServiceImpl            # Business logic
│   │       │   ├── Repository/            # Data access
│   │       │   ├── Entity/                # JPA entities
│   │       │   ├── DTO/                   # Data transfer objects
│   │       │   ├── Enum/                  # Enumerations for states
│   │       │   ├── RedisConfig/           # Redis configuration
│   │       │   ├── Feign/                 # Feign clients
│   │       │   ├── Security/              # spring security
│   │       └── resources/
│   ├── pom.xml
│   ├── Dockerfile
│   └── InventoryServiceReadMe.md
│
├── notification-service/                  # Email Notifications
│   ├── src/
│   │   ├── main/
│   │       ├── java/
│   │       │   ├── Controller/            # API endpoints Exception Handling
│   │       │   ├── Service/               # hide Business logic
│   │       │   ├── ServiceImpl            # Business logic
│   │       │   ├── Repository/            # Data access
│   │       │   ├── Entity/                # JPA entities
│   │       │   ├── DTO/                   # Data transfer objects
│   │       │   ├── Enum/                  # Enumerations for states
│   │       │   ├── Feign/                 # Feign clients
│   │       └── resources/
│   ├── pom.xml
│   ├── Dockerfile
│   └── NotificationServiceReadme.md
│
├── service-registry/                      # Eureka Service Registry
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── config-server/                         # Spring Cloud Config Server
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── docker-compose.yml                     # Multi-container orchestration
├── README.md                              # This file
└── LICENSE                                # MIT License
```
---
## 🎯 Services Overview

### 1. **API Gateway** (Port 8080)
**Purpose**: Single entry point for all client requests

**Key Responsibilities**:
- JWT token validation
- Request routing to appropriate microservices
- Load balancing
- Rate limiting
- Cross-cutting concerns (logging, metrics)

**Tech**: Spring Cloud Gateway (Reactive WebFlux)

---
### 2. **Sentinel Orchestrator Service** (Port 8001, `/workflow`)
**Purpose**: Central state machine orchestrating distributed saga workflows

**Key Responsibilities**:
- JWT token validation locally
- Workflow state management (CREATED → IN_PROGRESS → COMPLETED/FAILED)
- Saga orchestration (Payment → Inventory → Notification)
- Automatic compensation on failures
- Scheduled job execution for workflow recovery
- Circuit breaker management for downstream calls

**Database**: `orchestrator_db`

**Tables**:
- `workflows`: Workflow state and metadata
- `workflow_steps`: Step execution history

**API Endpoints**:
```
POST   /workflow/start                  # Start new workflow
GET    /workflow/id/{workflowId}        # Get workflow status
GET    /workflow/id/{workflowId}/retry  # Retry failed workflow
GET    /workflow/resume                 # Resume pending workflows
GET    /workflow/status/{status}        # List workflows by status
```

**Features**:
- ✅ State Machine: Tracks workflow lifecycle
- ✅ Saga Pattern: Command-based orchestration
- ✅ Self-Healing: Scheduled job recovery (every 60s)
- ✅ Comprehensive Resilience: 5 circuit breakers, bulkhead, rate limiting

---
### 3. **Payment Service** (Port 8002, `/payment`)
**Purpose**: Handle payment processing and user account management

**Key Responsibilities**:
- User creation and account management
- Payment processing (deduction)
- Payment compensation (refund)
- Redis caching for user profiles
- Asynchronous email notifications
- JWT token validation locally  

**Database**: `payment_db`

**Tables**:
- `users`: User profiles and wallet balance
- `payments`: Payment transaction history

**API Endpoints**:
```
POST   /payment/user/create              # Create user account
POST   /payment/user/login               # Create JWT Token
PUT    /payment/user/update              # Update user details
GET    /payment/user/{userId}            # Get user by ID
DELETE /payment/user/delete/{userId}     # Delete user
POST   /payment/process                  # Process payment
GET    /payment/workflow/{workflowId}    # Get payment by workflow
GET    /payment/user/{userId}            # Get user payments
GET    /payment/compensate/{workflowId}  # Refund payment
```

**Features**:
- ✅ Saga Transactions: Deduct & Compensate logic
- ✅ JWT Token Creation for the first time/login
- ✅ Redis Caching: User profile caching (TTL: 10 min)
- ✅ Async Notifications: Email via ExecutorService
- ✅ State Tracking: SUCCESS, COMPENSATED, INSUFFICIENT_BALANCE
- ✅ Fault Tolerance: Resilience4j integration

---
### 4. **Inventory Service** (Port 8003, `/inventory`)
**Purpose**: Manage products and stock inventory

**Key Responsibilities**:
- Product CRUD operations
- Stock reservation for orders
- Stock release (compensation)
- Redis caching for product lookups
- Out-of-stock notifications

**Database**: `inventory_db`

**Tables**:
- `products`: Product catalog
- `reservations`: Stock reservation history

**API Endpoints**:
```
POST   /inventory/product/create         # Create product
GET    /inventory/product/{productId}    # Get product
PUT    /inventory/product/update         # Update product
DELETE /inventory/product/delete         # Delete product
POST   /inventory/process                # Reserve stock
POST   /inventory/compensate             # Release stock
GET    /inventory/reservation/{id}       # Get reservation
```

**Features**:
- ✅ Saga Transactions: ReserveStock & ReleaseStock
- ✅ Redis Caching: Product caching (TTL: 10 min)
- ✅ Async Alerts: Out-of-stock via thread pool
- ✅ Audit Trail: Reservation tracking with UUIDs
- ✅ Status Enums: AVAILABLE, RESERVATION, OUT_OF_STOCK

---
### 5. **Notification Service** (Port 8004, `/notification`)
**Purpose**: Send email notifications for workflow events

**Key Responsibilities**:
- Order success email
- Payment success email
- Payment refund email
- Low balance alerts
- Out-of-stock notifications
- Notification history audit

**Database**: `notification_db`

**Tables**:
- `notification_details`: Notification audit log

**API Endpoints**:
```
POST   /notification/send/order/success      # Order success email
POST   /notification/send/payment/success    # Payment success email
POST   /notification/send/payment/refunded   # Refund confirmation email
POST   /notification/send/failed/balance     # Low balance alert
POST   /notification/send/failed/outofstock  # Out-of-stock alert
```

**Features**:
- ✅ Multi-Type Notifications: 5 different email types
- ✅ SMTP Integration: Gmail SMTP gateway
- ✅ Async Execution: Non-blocking email sending
- ✅ Fallback Handlers: Graceful degradation
- ✅ Circuit Breaker: Protected downstream calls

---
### 6. **Service Registry (Eureka)** (Port 8761)
**Purpose**: Dynamic service registration and discovery

**Features**:
- ✅ Service registration
- ✅ Health checks
- ✅ Load balancing
- ✅ Automatic deregistration

---
### 7. **Config Server** (Port 8888)
**Purpose**: Centralized configuration management

**Features**:
- ✅ Dynamic property management
- ✅ Environment-based configs
- ✅ Hot refresh capability

---
## 🚀 Getting Started

### Prerequisites

```bash
# Required
- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0
- Redis 7

# Optional (for local testing without Docker)
- Git
- Postman (for API testing)
```

### Installation & Setup

#### **Option 1: Docker Compose (Recommended - Single Command)**

```bash
# 1. Clone the repository
git clone https://github.com/Manikantan-96/Sentinel-Distributed-Saga-Orchestrator-Microservices.git
cd Sentinel-Distributed-Saga-Orchestrator-Microservices

# 2. Start all services
docker-compose up -d

# 3. Verify all containers are running
docker-compose ps

# 4. Check service health
curl http://localhost:8761/eureka/apps  # Service Registry
curl http://localhost:8080/actuator/health  # API Gateway
```

**Expected Output:**
```
CONTAINER ID   IMAGE                    STATUS         PORTS
abc123...      sentinel-orchestrator    Up 2 min       8001:8001
def456...      payment-service          Up 2 min       8002:8002
ghi789...      inventory-service        Up 2 min       8003:8003
jkl012...      notification-service     Up 2 min       8004:8004
mno345...      api-gateway              Up 2 min       8080:8080
pqr678...      service-registry         Up 2 min       8761:8761
stu901...      config-server            Up 2 min       8888:8888
vwx234...      redis                    Up 2 min       6379:6379
```
---
## 🔄 Workflow Execution

### Example: Successful Order Flow
```
1. Client initiates order via API Gateway
   ↓
2. Orchestrator receives: POST /workflow/start
   {
     "userId": 1,
     "productId": 5,
     "quantity": 2,
     "workflowType": "Electronics Order"
   }
   ↓
3. Orchestrator creates workflow (Status: CREATED)
   ↓
4. Step 1: PAYMENT
   → Calls Payment Service: POST /payment/process
   → User balance: ₹50,000, Product price: ₹1,200/unit
   → Amount needed: ₹2,400 (2 × 1,200)
   → Balance sufficient ✓
   → Payment processed, amount deducted
   → User new balance: ₹47,600
   → Step Status: SUCCESS
   ↓
5. Step 2: INVENTORY
   → Calls Inventory Service: POST /inventory/process
   → Available stock: 100 units
   → Reservation quantity: 2
   → Stock available ✓
   → Stock reserved, quantity reduced to 98
   → Step Status: SUCCESS
   ↓
6. Step 3: NOTIFICATION
   → Calls Notification Service: POST /notification/send/order/success
   → Email sent to user@example.com
   → Notification recorded
   → Step Status: SUCCESS
   ↓
7. Workflow Completed
   → Status: COMPLETED
   → Response sent to client
```
---
### Example: Failure with Compensation

```
1. Order initiated
   → User balance: ₹50,000
   → Product price: ₹30,000/unit
   → Quantity: 2 → Total: ₹60,000
   ↓
2. Step 1: PAYMENT
   → Amount needed: ₹60,000
   → User balance: ₹50,000 (INSUFFICIENT!)
   → Payment failed
   → Step Status: FAILED
   ↓
3. Workflow Status: FAILED
   → CurrentStep: PAYMENT
   → No compensation needed (payment didn't complete)
   → Response: FAILED with error message
```
---
### Example 1: Partial Failure with Compensation
```
1. Order initiated
   → User balance: ₹50,000
   → Product price: ₹1,200/unit
   → Quantity: 10 → Total: ₹12,000
   ↓
2. Step 1: PAYMENT ✓
   → Amount deducted: ₹12,000
   → New balance: ₹38,000
   → Step Status: SUCCESS
   ↓
3. Step 2: INVENTORY ✗
   → Stock needed: 10 units
   → Stock available: 5 units (OUT OF STOCK!)
   → Reservation failed
   → Step Status: FAILED
   ↓
4. COMPENSATION TRIGGERED
   → Status changes to: COMPENSATING_PAYMENT
   → Calls: GET /payment/compensate/{workflowId}
   → Amount refunded: ₹12,000
   → User balance restored: ₹50,000
   → Status: COMPENSATED_PAYMENT
```
### Example 2: Partial Failure with Compensation
```
1. Order initiated
   → User balance: ₹50,000
   → Product price: ₹1,200/unit
   → Quantity: 10 → Total: ₹12,000
   ↓
2. Step 1: PAYMENT ✓
   → Amount deducted: ₹12,000
   → New balance: ₹38,000
   → Step Status: SUCCESS
   ↓
3. Step 2: INVENTORY ✓
   → Stock available: 15 units
   → Stock needed: 10 units
   → New Stock available: 5 units
   → Step Status: SUCCESS
      ↓
5. Notification sent ✗
   → Email: failed due to some issue
   → Status: RETRY_QUERY (for notification)
   ↓
6. Scheduler Job (runs every 60s)
   → Retries RETRY_QUERY workflows until retries<3
   → Email resent if needed
   → If retries >=3 triger compensation
   ↓
4. COMPENSATION TRIGGERED
   → Status changes to: COMPENSATING_INVENTORY
   → Calls:  /inventory/compensate?workflowId
   → Stock Released: 10
   → Stock available: 15
   → Status changes to: COMPENSATED_INVENTORY
   → Status changes to: COMPENSATING_PAYMENT
   → Calls: GET /payment/compensate/{workflowId}
   → Amount refunded: ₹12,000
   → User balance restored: ₹50,000
   → Status: COMPENSATED_PAYMENT
```
---
## 🛡️ Resilience Patterns

### 1. **Circuit Breaker**

**Configuration:**
- Sliding window: 10 calls
- Failure rate threshold: 50%
- Wait duration in open state: 10 seconds
- Automatic transition to half-open: Enabled

**Behavior:**
```
CLOSED (Normal)
   ↓ (Failure rate > 50%)
OPEN (Rejecting calls)
   ↓ (After 10 seconds)
HALF_OPEN (Testing)
   ↓ (Success threshold met)
CLOSED (Recovered)
```

**Instances:**
- Orchestrator: 5 circuit breakers (Inventory1, Inventory2, Payment1, Payment2, Notification)
- Payment: 4 circuit breakers (Inventory, PaymentSuccess, PaymentRefunded, FailedBalance)
- Inventory: 1 circuit breaker
- Notification: 1 circuit breaker (protected by Feign)

---

### 2. **Retry Pattern**

**Configuration:**
- Max attempts: 3
- Wait duration: 100ms
- Retry on: Feign exceptions

**Workflow:**
```
Call 1 → FAIL
   ↓ Wait 100ms
Call 2 → FAIL
   ↓ Wait 100ms
Call 3 → FAIL
   ↓ Give Up (Circuit opens or fallback triggered)
```
---
### 3. **Bulkhead Pattern**

**Configuration:**
- Max concurrent calls: 10
- Max wait duration: 0 (fail immediately if queue full)

**Purpose:** Prevent thread pool exhaustion

---
### 4. **Rate Limiter**

**Configuration:**
- Limit for period: 15 requests
- Limit refresh period: 1 second
- Timeout duration: 500ms

**Purpose:** Control throughput to notification service
---
### 5. **Time Limiter**

**Configuration:**
- Timeout duration: 1 second
- Cancel running future: Enabled

**Purpose:** Prevent hanging requests
---
## 🔐 Security

### JWT Authentication

**Token Format:**
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

### Security Flow
```
1. Client requests token from Auth endpoint
   ↓
2. API Gateway validates credentials
   ↓
3. JWT token issued with expiry
   ↓
4. Client includes token in Authorization header
   ↓
5. API Gateway validates JWT signature
   ↓
6. Request routed to service
   ↓
7. Service validates JWT locally
   ↓
8. Service processes request
   ↓
9. Feign clients propagate Authorization header to downstream services
```
---
## 🐳 Docker Deployment
bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f orchestrator-service

# Check status
docker-compose ps

# Stop all services
docker-compose down

# Remove volumes (careful - deletes data!)
docker-compose down -v

# Rebuild images
docker-compose build --no-cache

# Run single service
docker-compose up -d payment-service

# Stop specific service
docker-compose stop payment-service

# Restart service
docker-compose restart payment-service

# View resource usage
docker stats


### Port Mapping
```
| Service | Internal Port | External Port | URL |
|---------|--------------|--------------|-----|
| API Gateway | 8080 | 8080 | http://localhost:8080 |
| Orchestrator | 8001 | 8001 | http://localhost:8001/workflow |
| Payment | 8002 | 8002 | http://localhost:8002/payment |
| Inventory | 8003 | 8003 | http://localhost:8003/inventory |
| Notification | 8004 | 8004 | http://localhost:8004/notification |
| Eureka | 8761 | 8761 | http://localhost:8761 |
| Config Server | 8888 | 8888 | http://localhost:8888 |
| Redis | 6379 | 6379 | localhost:6379 |
```
---
## 📝 Testing
### Manual Testing with Postman

#### 1. Create User (Payment Service)
```http
POST http://localhost:8002/payment/user/create
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "address": "123 Main Street",
  "balance": 100000.00
}
```
#### 2. Create JWT Token (Payment Service)
```http
POST http://localhost:8002/payment/user/login?userId=1&email=john@example.com

Response: Authorization: Bearer JWT_TOKEN
```

#### 3. Create Product (Inventory Service)
```http
POST http://localhost:8003/inventory/product/create
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "productName": "Gaming Laptop",
  "price": 120000.00,
  "stockQuantity": 50,
  "status": "AVAILABLE",
  "description": "High-performance gaming laptop"
}
```

#### 4. Start Workflow (Orchestrator Service)
```http
POST http://localhost:8001/workflow/start
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN

{
  "workflowType": "Electronics Purchase",
  "userId": 1,
  "productId": 1,
  "quantity": 1,
  "message": "Order for gaming laptop"
}
```

#### 5. Check Workflow Status
```http
GET http://localhost:8001/workflow/id/10001
Authorization: Bearer YOUR_JWT_TOKEN
```

---
## 🧠 Design Decisions

- Chose orchestration-based Saga over choreography for better control and observability
- Used Redis caching to reduce DB load and improve response time
- Implemented circuit breakers to prevent cascading failures in distributed calls
- Added scheduler-based recovery to ensure eventual consistency
---
## 📞 Support & Contact

- **Email**: manikantan1501@gmail.com
- **GitHub Issues**: [Create Issue](https://github.com/Manikantan-96/Sentinel-Distributed-Saga-Orchestrator-Microservices/issues)
- **Discussion**: [GitHub Discussions](https://github.com/Manikantan-96/Sentinel-Distributed-Saga-Orchestrator-Microservices/discussions)
---
## 👨‍💻 Author

**Manikantan**
- GitHub: [@Manikantan-96](https://github.com/Manikantan-96)
- Email: manikantan1501@gmail.com
- LinkedIn: [Profile](https://www.linkedin.com/in/manikantan-deevanooru)

---
## 📊 Project Statistics

- **Services**: 7 (Orchestrator, Payment, Inventory, Notification, Gateway, Eureka, Config)
- **Databases**: 4 (MySQL instances)
- **Cache Layer**: 1 (Redis)
- **Total Ports**: 8 active services
- **Code Language**: 97.9% Java, 2.1% Dockerfile
- **Spring Boot Version**: 4.0.4
- **Java Version**: 17+
---
## 🚀 Future Enhancements

- [ ] Distributed tracing (Zipkin integration)
- [ ] Metrics collection (Prometheus + Grafana)
- [ ] Event-driven saga pattern (Kafka/RabbitMQ)
- [ ] Performance testing suite
- [ ] Load testing framework
- [ ] Security hardening (OAuth2/OIDC)
- [ ] Multi-region deployment support
- [ ] Kubernetes deployment manifests
- [ ] CI/CD pipeline (GitHub Actions)
---

