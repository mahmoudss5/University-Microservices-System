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

The **University Management System** is a robust, scalable, and modular platform designed to manage university operations — including student enrollment, course management, teacher assignments, course-material storage, audit logging, and inter-service communication. Built with a **Microservices Architecture**, the system ensures high availability, independent deployability, and fault tolerance.

![High Level Architecture Design](https://raw.githubusercontent.com/maariamashraf/University-Management-System-Microservices/main/Diagrams/High%20Level%20Architecture%20Design.jpg)

---

## Architecture

The project follows a modern microservices architecture where each service owns a specific business domain. Services communicate via **REST** (synchronous) and **Apache Kafka** (asynchronous events). The **Academic Core** service also integrates with **Amazon S3** for storing course materials, uploaded documents, and presigned file access.

### Core Services

| Service | Port | Responsibility |
|---|---|---|
| **API Gateway** | `8080` | Single entry point — routing, JWT auth, rate limiting, BFF dashboard aggregation |
| **Eureka Server** | `8761` | Service discovery registry |
| **IAM Service** | `8081` | Identity & access — registration, login, JWT issuance, user profiles |
| **Academic Core** | `8082` | Courses, departments, enrollments, announcements, and feedback |
| **Communication** | `8083` | Notifications, direct messages, WebSocket real-time messaging |
| **Audit Log Service** | `8085` | Kafka audit-event ingestion, durable audit storage, filtering, and admin queries |

### Infrastructure

| Component | Port | Purpose |
|---|---|---|
| **IAM MySQL** | `3310` (host) / `3306` (container) | IAM users and roles |
| **Academic MySQL** | `3309` (host) / `3306` (container) | Courses, enrollments, prerequisites, and outbox |
| **Communication MySQL** | `3308` (host) / `3306` (container) | Notifications, messages, and local snapshots |
| **Audit Log MySQL** | `3311` (host) / `3306` (container) | Durable audit events consumed from Kafka |
| **Redis** | `6379` | Rate limiting (sorted sets), response caching |
| **Apache Kafka** | `9092` | Async event bus between services |
| **Amazon S3** | N/A | Object storage for Academic Core course materials and uploaded files |
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
│   │           ├── aop/         (CourseTeacherOnly aspect)
│   │           └── config/      (Security, Cache, Seeding, Snapshot bootstrap)
│   │
│   ├── communication-service/         # Spring MVC + WebSocket
│   │   └── src/main/java/UnitSystem/demo/
│   │       ├── Controllers/     (MessageController, NotificationController)
│   │       ├── BusinessLogic/   (services, mappers)
│   │       ├── Kafka/           (KafkaConsumer — processes events from academic-core)
│   │       └── Security/        (JWT + WebSocket auth interceptor)
│   │
│   ├── AuditLogService/                # Kafka-backed audit log service (port 8085)
│   │   └── src/main/java/com/AuditLog/AuditLogService/
│   │       ├── Controllers/             (paginated and role-based audit queries)
│   │       ├── BusinessLogic/           (audit validation and persistence)
│   │       ├── DataAccessLayer/         (AuditLog entity and repository)
│   │       └── kafka/                   (domain/security event consumer)
│   │
│   └── eureka-server/                 # Netflix Eureka service registry
│
├── FrontEnd/my-app/                   # React + Vite (port 5173)
├── Diagrams/                          # ERD, Sequence, Use Case, Activity, Class diagrams
├── performance/                        # k6 stress tests and HTML dashboards
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
| `security-audit-events.v1` | Gateway/security producers | Audit Log Service |
| `user-registered-v1` | IAM Service | Academic Core, Communication, Audit Log Service |
| `user-updated-v1` | IAM Service | Academic Core, Communication, Audit Log Service |
| `user-deactivated-v1`| IAM Service | Academic Core, Communication, Audit Log Service |
| `user-deleted-v1` | IAM Service | Academic Core, Communication, Audit Log Service |
| `student-registered` | IAM Service | Academic Core, Communication, Audit Log Service |
| `student-enrolled` | Academic Core | Communication Service, Audit Log Service |
| `student-unenrolled` | Academic Core | Communication Service, Audit Log Service |
| `course-created` | Academic Core | Communication Service, Audit Log Service |
| `course-deleted` | Academic Core | Communication Service, Audit Log Service |
| `announcement-created` | Academic Core | Communication Service, Audit Log Service |
| `feedback-created` | Academic Core | Communication Service, Audit Log Service |
| `notification-push` | Communication Service | Downstream, Audit Log Service |

The Audit Log Service consumes the security and domain-event topics in its own consumer group (`audit-log-v1`) and persists normalized events in `auditLogDb`. Kafka dead-letter topics (`*.DLT`) are provisioned for failed event processing.

### 5. Aspect-Oriented Programming (AOP)
- **Academic Core** — `@CourseTeacherOnly` enforces access control
- **IAM Service** — `@RateLimit` enforces per-endpoint sliding-window rate limits (see Rate Limiting section)
- **Communication Service** — `LoggingAspect` provides method-level telemetry
- **Audit Log Service** — Kafka consumers normalize and persist security and domain events for later investigation

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

### 9. Database-per-Service Pattern
To ensure true loose coupling and independent scaling, the system eschews a monolithic shared database in favor of dedicated datastores for each service boundary:
- **`iamDb`**: Owned exclusively by the IAM Service (Users and Roles).
- **`academicDb`**: Owned exclusively by the Academic Core Service (Courses, Enrollments, Feedback, Outbox).
- **`communicationServiceDb`**: Owned exclusively by the Communication Service (Messages, Notifications).
- **`auditLogDb`**: Owned exclusively by the Audit Log Service (normalized audit events).

### 10. Transactional Outbox Pattern
Implemented in the **Academic Core Service** to guarantee at-least-once delivery of domain events (e.g., `student-enrolled`, `course-deleted`) to Kafka, even in the event of message broker downtime.
- Events are persisted to an `outbox_event` table in the exact same database transaction that updates the business entities.
- A background relayer then safely publishes these pending events to Kafka and marks them as processed, preventing data inconsistencies between the database and the event stream.

### 11. Course Materials with Amazon S3

Academic Core stores course-material metadata in `academicDb` and uses Amazon S3 for the file contents. Teachers request a short-lived presigned upload URL, upload directly to S3, and then mark the material as complete. Students and teachers receive presigned download URLs through the API. This keeps large files out of the service containers and avoids proxying file data through the gateway.

Material endpoints are exposed through the gateway under `/api/courses/{courseId}/materials`:

- `POST /upload-url` — teacher-only upload initialization
- `POST /{materialId}/complete` — teacher-only upload completion and metadata verification
- `GET /` — list materials for a course
- `GET /{materialId}/download-url` — create a presigned download URL
- `DELETE /{materialId}` — teacher-only deletion

### 12. Recoverable User Snapshots

Academic Core keeps a local `UserSnapshot` read model for operations that need user names and roles without synchronous IAM calls. IAM domain events remain the primary synchronization path. A startup bootstrapper and scheduled retry reconcile student and teacher snapshots from IAM (`/api/students/basic/all` and `/api/teachers/basic/all`) so deployments or missed Kafka events can repair the local read model without blocking Academic Core startup.

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
| `GET` | `/api/students/basic/all` | ✓ Internal service access | Basic student IDs, usernames, roles, and active status for Academic Core snapshot repair |
| `GET` | `/api/teachers/basic/all` | ✓ Internal service access | Basic teacher IDs, names, roles, and active status for Academic Core snapshot repair |
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
| `GET` | `/api/courses/{courseId}/materials` | ✓ JWT | List course materials |
| `POST` | `/api/courses/{courseId}/materials/upload-url` | ✓ Teacher | Create a presigned S3 upload URL |
| `POST` | `/api/courses/{courseId}/materials/{materialId}/complete` | ✓ Teacher | Verify and complete an S3 upload |
| `GET` | `/api/courses/{courseId}/materials/{materialId}/download-url` | ✓ JWT | Create a presigned S3 download URL |
| `DELETE` | `/api/courses/{courseId}/materials/{materialId}` | ✓ Teacher | Delete course material and its S3 object |

### Communication Service

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/notifications/user/{userId}` | ✓ JWT | User notifications |
| `POST` | `/api/messages` | ✓ JWT | Send a direct message |
| `GET` | `/api/messages/course/{courseId}` | ✓ JWT | Course group messages |
| `WS` | `/ws/**` | ✓ JWT (interceptor) | Real-time WebSocket channel |

### Audit Log Service (`/api/audit-logs`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/audit-logs` | ✓ JWT | Paginated audit events; supports `eventType`, `source`, `userId`, `page`, and `size` filters |
| `GET` | `/api/audit-logs/{id}` | ✓ JWT | Find an audit event by database ID |
| `GET` | `/api/audit-logs/event/{eventId}` | ✓ JWT | Find an audit event by event ID |
| `GET` | `/api/audit-logs/last-week-students-logs` | ✓ JWT | Recent student activity |
| `GET` | `/api/audit-logs/last-week-teachers-logs` | ✓ JWT | Recent teacher activity |
| `GET` | `/api/audit-logs/last-week-admins-logs` | ✓ JWT | Recent administrator activity |

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

### Stress Testing

Service-level stress tests are implemented with [k6](https://k6.io/) under [`performance/`](performance/):

- **General University API** — ramping authenticated and public journeys covering profiles, courses, departments, and recent feedback (`performance/genreal/`). The directory name is kept for compatibility with the existing scripts.
- **Academic Core** — course and department reads plus a 200-VU enrollment race for a course with one available seat. The report detects oversubscription.
- **IAM** — 200-VU login, current-profile, and admin user-list traffic. Login `429` responses are reported separately as expected gateway rate limiting.
- **Audit Log** — 200-VU HTTP queries for paginated, filtered, and weekly audit logs. Kafka ingestion is intentionally excluded.

The suites target the API Gateway at `http://localhost:8080` by default. Each suite produces a JSON summary and a standalone HTML/CSS/JavaScript dashboard. The service-specific suites default to 200 VUs; the general suite can be tuned with `MAX_VUS`. See [`performance/README.md`](performance/README.md) for prerequisites, commands, environment variables, and dashboard URLs.

## Observability & Kubernetes Readiness

Every microservice exposes **Spring Boot Actuator** health endpoints configured specifically for Kubernetes (Liveness and Readiness probes):
- **Secured via API Gateway**: Actuator endpoints (`/actuator/**`) are strictly blocked at the Gateway level preventing external internet access, while still allowing the internal Docker network or Kubernetes orchestrator to ping them safely.

---

## How to Run

The entire stack is orchestrated with **Docker Compose**.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) (only required for stress tests)
- AWS credentials with access to the configured S3 bucket (required for course-material uploads)

### Start the System

```bash
# 1. Clone the repository
git clone https://github.com/maariamashraf/University-Management-System-Microservices
cd University-Management-System-Microservices

# 2. Create a local .env file for Academic Core's S3 integration
# Add these entries to .env (do not commit the file):
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key

# 3. Build and start all services
docker compose up --build
```

> First build downloads all Maven dependencies into a named volume (`maven-cache`) — subsequent restarts are much faster.

The Academic Core S3 configuration defaults to the `us-east-1` region and bucket `academic-core-service`. Configure the bucket and AWS credentials for the environment before using course-material uploads. Keep `.env` local; it is ignored by Git.

On startup, Academic Core retries its IAM teacher lookup before seeding courses and feedback. If IAM is temporarily unavailable, the service remains up and the seed step is skipped safely; the scheduled user-snapshot bootstrap continues retrying synchronization after IAM is ready.

### Service URLs

| Service | URL |
|---|---|
| **Frontend App** | http://localhost:5173 |
| **API Gateway** | http://localhost:8080 |
| **Eureka Dashboard** | http://localhost:8761 |
| **Audit Log Service** | http://localhost:8085 |
| **Kafka UI** | http://localhost:8090 |

### Infrastructure Details

| Component | Host Port | Credentials |
|---|---|---|
| IAM MySQL | `3310` | root / `iamUniSys@Db#2026`, DB: `iamDb` |
| Academic MySQL | `3309` | root / `academicUniSys@Db#2026`, DB: `academicDb` |
| Communication MySQL | `3308` | root / `communicationUniSys@Db#2026`, DB: `communicationServiceDb` |
| Audit Log MySQL | `3311` | root / `auditLogUniSys@Db#2026`, DB: `auditLogDb` |
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
| Language | Java 17 (IAM) / Java 21 (Gateway, Eureka, Academic Core, Communication, Audit Log) |
| Framework | Spring Boot 3.3.4 (main services), Spring Boot 3.4.2 (Audit Log), Spring Cloud 2023.0.3 (main services) / 2024.0.0 (Audit Log) |
| API Gateway | Spring Cloud Gateway (WebFlux/Reactor) |
| Service Discovery | Netflix Eureka |
| Security | Spring Security, JJWT 0.11.5 |
| Database | MySQL 8.0, Spring Data JPA, Flyway |
| Cache / Rate Limit | Redis 7.2, Spring Data Redis, Lua scripting |
| Messaging | Apache Kafka 7.5, Spring Kafka |
| Object Storage | Amazon S3, AWS SDK v2, presigned upload/download URLs |
| Real-time | STOMP over WebSocket |
| AOP | Spring AOP (AspectJ) |
| Containerisation | Docker, Docker Compose |
| Frontend | React 18 + Vite |
| Performance Testing | Grafana k6 with generated JSON and browser dashboards |
