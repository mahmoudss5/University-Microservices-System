# University Management System — Microservices

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.3-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-cloud)
[![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-7.5-231F20?style=for-the-badge&logo=apache-kafka)](https://kafka.apache.org)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)

</div>

---

## Project Overview

The **University Management System** is a robust, scalable, and modular platform designed to manage university operations — including student enrollment, course management, teacher assignments, and inter-service communication. Built with a **Microservices Architecture**, the system ensures high availability, independent deployability, and fault tolerance.

![High Level Architecture Design](https://raw.githubusercontent.com/maariamashraf/University-Management-System-Microservices/main/Diagrams/High%20Level%20Architecture%20Design.jpg)

---

## Architecture

The project follows a modern microservices architecture where each service owns a specific business domain. Services communicate via **REST** (synchronous) and **Apache Kafka** (asynchronous events).

### Core Services

| Service | Port | Responsibility |
|---|---|---|
| **API Gateway** | `8080` | Single entry point — routing, JWT auth, rate limiting, BFF dashboard aggregation |
| **Eureka Server** | `8761` | Service discovery registry |
| **IAM Service** | `8081` | Identity & access — registration, login, JWT issuance, user profiles |
| **Academic Core** | `8082` | Courses, departments, enrollments, announcements, feedback, audit logs |
| **Communication** | `8083` | Notifications, direct messages, WebSocket real-time messaging |

### Infrastructure

| Component | Port | Purpose |
|---|---|---|
| **IAM MySQL** | `3310` (host) / `3306` (container) | IAM users and security audit records |
| **Academic MySQL** | `3309` (host) / `3306` (container) | Courses, enrollments, prerequisites, and outbox |
| **Communication MySQL** | `3308` (host) / `3306` (container) | Notifications, messages, and local snapshots |
| **Redis** | `6379` | Rate limiting (sorted sets), response caching |
| **Apache Kafka** | `9092` | Async event bus between services |
| **Zookeeper** | `2181` | Kafka coordination |
| **Kafka UI** | `8090` | Web console for Kafka topic monitoring |

---

## Project Structure

```
University-Management-System-Microservices/
├── Backend/
│   ├── api-gateway/                   # Spring Cloud Gateway + WebFlux
│   │   └── src/main/
│   │       ├── java/com/unisystem/api_gateway/
│   │       │   ├── filter/
│   │       │   │   ├── GlobalRateLimiterFilter.java      # 150 req/min per IP (all routes)
│   │       │   │   └── LuaRateLimiterGatewayFilterFactory.java  # Per-route configurable limiter
│   │       │   ├── controller/DashboardController.java   # BFF aggregation endpoints
│   │       │   ├── service/DashboardAggregationService.java
│   │       │   ├── JwtAuthFilter.java                    # JWT validation + header injection
│   │       │   ├── RateLimiterConfig.java                # Redis script + key resolver beans
│   │       │   └── SecurityConfig.java                   # WebFlux security + CORS
│   │       └── resources/
│   │           ├── scripts/rate_limiter.lua              # Lua sliding-window algorithm
│   │           └── application.yml
│   │
│   ├── iam-service/                   # Spring MVC (Servlet)
│   │   └── src/main/java/com/uni/iam/
│   │       ├── controller/AuthController.java            # @RateLimit(requestsPerMinute=20)
│   │       ├── ratelimit/
│   │       │   ├── RateLimit.java                       # Custom annotation
│   │       │   └── RateLimitAspect.java                 # AOP sliding-window enforcement
│   │       ├── entity/          (User, Student, Teacher, Admin, Role)
│   │       ├── security/        (JwtAuthFilter, JwtUtils, CustomUserDetails)
│   │       └── service/impl/
│   │           ├── AcademicStandingImp/ (Strategy pattern)
│   │           └── StudentSerivces/    (Facade pattern)
│   │
│   ├── academic-core-Service/         # Hexagonal Architecture
│   │   └── src/main/java/…/academic_core_service/
│   │       ├── domain/          (entities, ports, use cases)
│   │       └── infrastructure/
│   │           ├── adapters/in/web/     (REST controllers)
│   │           ├── adapters/out/kafka/  (Kafka producers)
│   │           ├── adapters/out/persistence/ (JPA adapters)
│   │           ├── aop/         (AuditLog, CourseTeacherOnly aspects)
│   │           └── config/      (Security, Cache, Bean configs)
│   │
│   ├── communication-service/         # Spring MVC + WebSocket
│   │   └── src/main/java/UnitSystem/demo/
│   │       ├── Controllers/     (MessageController, NotificationController)
│   │       ├── BusinessLogic/   (services, mappers)
│   │       ├── Kafka/           (KafkaConsumer — processes events from academic-core)
│   │       └── Security/        (JWT + WebSocket auth interceptor)
│   │
│   └── eureka-server/                 # Netflix Eureka service registry
│
├── FrontEnd/my-app/                   # React + Vite (port 5173)
├── Diagrams/                          # ERD, Sequence, Use Case, Activity, Class diagrams
├── docker-compose.yml                 # Full stack orchestration
└── Checklist.md
```

---

## Design Patterns Implemented

### 1. Hexagonal Architecture (Ports and Adapters)
Applied in **Academic Core Service** to decouple business logic from frameworks and databases.
- **Domain** — pure business entities and logic
- **Ports** — interfaces (`CoursePort`, `EnrollmentPort`, …)
- **Adapters** — REST controllers (inbound) and JPA/Kafka (outbound)

### 2. Facade Pattern
`StudentDashboardFacade` in the **IAM Service** aggregates data from multiple sources into a single, simplified interface for the dashboard view.

### 3. Strategy Pattern
`AcademicStandingStrategy` in the **IAM Service** allows different GPA/standing calculation algorithms (`StandardStandingStrategy`, `OldStandingStrategy`) to be swapped at runtime.

### 4. Observer Pattern (Event-Driven via Kafka)
Services publish domain events consumed by downstream services:

| Topic | Publisher | Consumer |
|---|---|---|
| `user-registered-v1` | IAM Service | Academic Core, Communication |
| `user-updated-v1` | IAM Service | Academic Core, Communication |
| `user-deactivated-v1`| IAM Service | Academic Core, Communication |
| `user-deleted-v1` | IAM Service | Academic Core, Communication |
| `security-audit-events`| API Gateway | IAM Service |
| `student-enrolled` | Academic Core | Communication Service |
| `student-unenrolled` | Academic Core | Communication Service |
| `course-created` | Academic Core | Communication Service |
| `course-deleted` | Academic Core | Communication Service |
| `announcement-created` | Academic Core | Communication Service |
| `feedback-created` | Academic Core | Communication Service |
| `notification-push` | Communication Service | Downstream |

### 5. Aspect-Oriented Programming (AOP)
- **Academic Core** — `@AuditLog` records every write operation; `@CourseTeacherOnly` enforces access control
- **IAM Service** — `@RateLimit` enforces per-endpoint sliding-window rate limits (see Rate Limiting section)
- **Communication Service** — `LoggingAspect` provides method-level telemetry

### 6. Backend for Frontend (BFF)
`DashboardController` in the **API Gateway** aggregates data from IAM, Academic Core, and Communication services into single-call responses optimised for the React frontend.

### 7. Database Concurrency Control
- **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`): Implemented in the **Academic Core Service** (`CourseJpaRepository`) to prevent race conditions when multiple users attempt to enroll in the same course simultaneously. This guarantees that only one transaction can check and update course capacity at a time, ensuring strict data consistency without application-level retry logic.

### 8. Caller Resilience Pattern (Fault Tolerance)
Implemented using **Resilience4j** in the **API Gateway** and **Academic Core Service** to prevent cascading failures when making synchronous inter-service calls via Feign (e.g., calling the IAM Service).
- **Circuit Breaker**: Fast-fails requests when the error rate exceeds a threshold, giving the failing remote service time to recover.
- **Retry**: Automatically retries transient network failures.
- **Bulkhead**: Limits concurrent calls to a specific remote service, ensuring a slow downstream service does not exhaust the caller's thread pool.
- **Caller Wrapper**: Feign clients are encapsulated inside a dedicated "Caller" layer (e.g., `IamServiceCaller`, `IamUserClient`). This layer gracefully handles exceptions and executes local fallback methods (returning default responses) without cluttering the Feign interface.

---

## 🚦 Rate Limiting (Sliding Window — Lua + Redis)

A **three-layer, defence-in-depth** rate-limiting strategy protects the system against abuse, brute-force attacks, and traffic spikes.

### Algorithm: Sliding Window Log

All rate limiters use the same algorithm implemented as an atomic **Lua script** (`scripts/rate_limiter.lua`) executed inside Redis:

```
┌─ Sliding Window (60 seconds) ──────────────────────────────────┐
│  ZREMRANGEBYSCORE key -inf (now - 60000)  ← prune old entries  │
│  ZCARD key                                ← count remaining     │
│  if count < limit:                                              │
│      ZADD key now request_id              ← log the request    │
│      PEXPIRE key 60000                    ← reset TTL          │
│      return [1, remaining]                ← ALLOWED            │
│  else:                                                          │
│      return [0, 0]                        ← DENIED (429)       │
└────────────────────────────────────────────────────────────────┘
```

**Why Lua?** Redis executes the entire script atomically — no race conditions possible even under high concurrency from multiple gateway replicas.

---

### Layer 1 — Global Filter (API Gateway)

**File:** [`GlobalRateLimiterFilter.java`](Backend/api-gateway/src/main/java/com/unisystem/api_gateway/filter/GlobalRateLimiterFilter.java)

| Property | Value |
|---|---|
| Scope | **All routes** |
| Limit | **150 requests / minute / IP** |
| Algorithm | Sliding Window (Lua + Redis Sorted Set) |
| Gateway filter order | `-2` (outermost — before JWT validation) |
| Redis key pattern | `rl:global:{clientIp}` |
| Fail behaviour | **Fail open** — Redis outage never blocks traffic |

**Response headers (on every request):**
```
X-RateLimit-Limit: 150
X-RateLimit-Remaining: <n>
X-RateLimit-Window: 60
```

**When denied (HTTP 429):**
```
Retry-After: 60
X-RateLimit-Limit: 150
X-RateLimit-Remaining: 0
```

---

### Layer 2 — Per-Route Filter (API Gateway)

**File:** [`LuaRateLimiterGatewayFilterFactory.java`](Backend/api-gateway/src/main/java/com/unisystem/api_gateway/filter/LuaRateLimiterGatewayFilterFactory.java)

A **configurable** Spring Cloud Gateway filter factory — attach it to any route and set the limit in `application.yml`.

| Property | Value |
|---|---|
| Scope | **Per-route** (currently `/api/auth/**`) |
| Limit | **20 requests / minute / IP** on auth routes |
| Algorithm | Sliding Window (same Lua script as Layer 1) |
| Redis key pattern | `rl:route:{routeId}:{clientIp}` |

**Configuration syntax (`application.yml`):**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: some-route
          filters:
            - name: LuaRateLimiter
              args:
                requestsPerMinute: 20
```

---

### Layer 3 — AOP Annotation (IAM Service)

**Files:**
- [`RateLimit.java`](Backend/iam-service/src/main/java/com/uni/iam/ratelimit/RateLimit.java) — annotation
- [`RateLimitAspect.java`](Backend/iam-service/src/main/java/com/uni/iam/ratelimit/RateLimitAspect.java) — aspect

A **service-layer** annotation that enforces the sliding window directly inside the IAM service, independently of the gateway. Apply it to any Spring bean method:

```java
@PostMapping("/login")
@RateLimit(requestsPerMinute = 20)   // ← change this number per endpoint
public ResponseEntity<AuthResponse> login(...) { ... }

@PostMapping("/register")
@RateLimit(requestsPerMinute = 20)
public ResponseEntity<AuthResponse> register(...) { ... }
```

| Property | Value |
|---|---|
| Scope | **Per-method / per-class** |
| Default limit | `20` req/min (configurable via annotation attribute) |
| Algorithm | Sliding Window (Redis pipeline — `ZREMRANGEBYSCORE` → `ZCARD` → `ZADD`) |
| Redis key pattern | `rl:service:{ClassName}.{methodName}:{clientIp}` |
| Annotation target | `ElementType.METHOD` or `ElementType.TYPE` |

**Error response body (HTTP 429):**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Max 20 requests per minute. Retry after 60 seconds."
}
```

---

### Rate Limit Summary

```
Client Request
     │
     ▼
┌────────────────────────────────────────────────────┐
│  API GATEWAY  (port 8080)                          │
│                                                    │
│  ① GlobalRateLimiterFilter  (order -2)             │
│     └─ 150 req/min per IP  ─── ALL routes          │
│                                                    │
│  ② LuaRateLimiterFactory   (route filter)          │
│     └─ 20 req/min per IP   ─── /api/auth/**        │
│                                                    │
│  ③ JwtAuthFilter            (order -1)             │
│     └─ JWT validation + header injection           │
└──────────────────┬─────────────────────────────────┘
                   │  (forwarded to IAM Service)
                   ▼
┌────────────────────────────────────────────────────┐
│  IAM SERVICE  (port 8081)                          │
│                                                    │
│  ④ @RateLimit AOP Aspect                           │
│     └─ 20 req/min per IP  ─── login / register     │
└────────────────────────────────────────────────────┘
```

---

## API Endpoints

### IAM Service (`/api/auth`, `/api/users`, `/api/students`, `/api/teachers`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | ✗ Public | Register a new user, returns JWT |
| `POST` | `/api/auth/login` | ✗ Public | Authenticate user, returns JWT |
| `GET` | `/api/users/me` | ✓ JWT | Get current user profile |
| `GET` | `/api/students/details/{id}` | ✓ JWT | Detailed student profile |
| `GET` | `/api/teachers/details/{id}` | ✓ JWT | Detailed teacher profile |
| `PUT` | `/api/users/{id}` | ✓ JWT | Update user information |

### Academic Core Service

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/courses` | ✓ JWT | Create a new course |
| `GET` | `/api/courses` | ✓ JWT | List all courses |
| `GET` | `/api/courses/popular` | ✗ Public | List popular courses |
| `POST` | `/api/enrollments` | ✓ JWT | Enroll a student in a course |
| `GET` | `/api/departments/all` | ✗ Public | List all departments |
| `GET` | `/api/announcements/course/{courseId}` | ✓ JWT | Course announcements |
| `POST` | `/api/feedbacks` | ✓ JWT | Submit course feedback |
| `GET` | `/api/feedbacks/recent` | ✗ Public | Recent feedback |
| `GET` | `/api/semesters` | ✓ JWT | Academic semesters |
| `GET` | `/api/audit-logs` | ✓ JWT | Audit trail (admin only) |

### Communication Service

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/notifications/user/{userId}` | ✓ JWT | User notifications |
| `POST` | `/api/messages` | ✓ JWT | Send a direct message |
| `GET` | `/api/messages/course/{courseId}` | ✓ JWT | Course group messages |
| `WS` | `/ws/**` | ✓ JWT (interceptor) | Real-time WebSocket channel |

### API Gateway — BFF Dashboard

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/gateway/dashboard/student/{id}` | ✓ JWT | Aggregated student dashboard |
| `GET` | `/api/gateway/dashboard/teacher/{id}` | ✓ JWT | Aggregated teacher dashboard |
| `GET` | `/api/gateway/dashboard/user` | ✓ JWT | Current user dashboard |

---

## Diagrams Reference

All project diagrams are in the [`Diagrams/`](https://github.com/maariamashraf/University-Management-System-Microservices/tree/main/Diagrams) directory:

- **ERD** — Entity Relationship Diagram
- **Sequence Diagrams** — login, enrollment, notification flows
- **Use Case Diagrams**
- **Activity Diagrams**
- **Class Diagram + OCL constraints**
- **SRS Document**

---

## CI/CD & Testing

The project uses **GitHub Actions** for Continuous Integration (CI).
- **Unit Testing**: Services are unit-tested using **JUnit 5** and **Mockito**.
- **Test Reporting**: Automated XML test reports are generated and published visually directly on the GitHub PR using the `EnricoMi/publish-unit-test-result-action`.
- **Multi-module builds**: The pipeline automatically detects and tests every Spring Boot microservice in the `Backend/` directory.

## Observability & Kubernetes Readiness

Every microservice exposes **Spring Boot Actuator** health endpoints configured specifically for Kubernetes (Liveness and Readiness probes):
- **Secured via API Gateway**: Actuator endpoints (`/actuator/**`) are strictly blocked at the Gateway level preventing external internet access, while still allowing the internal Docker network or Kubernetes orchestrator to ping them safely.

---

## How to Run

The entire stack is orchestrated with **Docker Compose**.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)

### Start the System

```bash
# 1. Clone the repository
git clone https://github.com/maariamashraf/University-Management-System-Microservices
cd University-Management-System-Microservices

# 2. Build and start all services
docker-compose up --build
```

> First build downloads all Maven dependencies into a named volume (`maven-cache`) — subsequent restarts are much faster.

### Service URLs

| Service | URL |
|---|---|
| **Frontend App** | http://localhost:5173 |
| **API Gateway** | http://localhost:8080 |
| **Eureka Dashboard** | http://localhost:8761 |
| **Kafka UI** | http://localhost:8090 |

### Infrastructure Details

| Component | Host Port | Credentials |
|---|---|---|
| IAM MySQL | `3310` | root / `iamUniSys@Db#2026`, DB: `iamDb` |
| Academic MySQL | `3309` | root / `academicUniSys@Db#2026`, DB: `academicDb` |
| Communication MySQL | `3308` | root / `communicationUniSys@Db#2026`, DB: `communicationServiceDb` |
| Redis | `6379` | no auth |
| Kafka | `9092` | no auth |

### Verify Rate Limiting

```bash
# Test the global limit (150 req/min per IP) — should get 429 after 150 rapid requests
for i in $(seq 1 155); do
  echo -n "Request $i: "
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/courses
done

# Test the auth route limit (20 req/min per IP — Layers 2 + 3)
for i in $(seq 1 25); do
  echo -n "Auth request $i: "
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@uni.edu","password":"wrong"}'
done
```

---

## Security Model

```
Client
  │
  ├─ JWT issued by IAM Service (HS256, 24 h expiry)
  │
  └─ API Gateway JwtAuthFilter (order -1):
       • Validates signature + expiry
       • Strips client-supplied X-User-Id / X-Username / X-Roles headers
       • Injects validated claims as trusted internal headers
       • Downstream services trust X-User-Id, X-Username, X-Roles
         without re-validating the JWT
```

Public endpoints (no JWT required): `/api/auth/login`, `/api/auth/register`, `/api/courses/popular`, `/api/departments/all`, `/api/feedbacks/recent`, `/ws/**`

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 (IAM, Academic Core, Communication) / Java 21 (API Gateway) |
| Framework | Spring Boot 3.3.4, Spring Cloud 2023.0.3 |
| API Gateway | Spring Cloud Gateway (WebFlux/Reactor) |
| Service Discovery | Netflix Eureka |
| Security | Spring Security, JJWT 0.11.5 |
| Database | MySQL 8.0, Spring Data JPA, Flyway |
| Cache / Rate Limit | Redis 7.2, Spring Data Redis, Lua scripting |
| Messaging | Apache Kafka 7.5, Spring Kafka |
| Real-time | STOMP over WebSocket |
| AOP | Spring AOP (AspectJ) |
| Containerisation | Docker, Docker Compose |
| Frontend | React 18 + Vite |
