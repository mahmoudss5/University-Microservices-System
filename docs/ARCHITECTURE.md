# Architecture and System Design

[← Back to the project README](../README.md) · [API and technical reference →](API.md)

## Architectural goals

The system is organized around independent business capabilities. Each service owns its data and can be built, tested, deployed, and scaled separately. REST is used when a caller needs an immediate response; Kafka is used for domain events and side effects that can happen asynchronously.

The main goals are:

- keep identity, academic data, communication data, and audit data in separate ownership boundaries;
- prevent one service failure from cascading through every request;
- make enrollment and event publication consistent under concurrency;
- support both request/response workflows and real-time event delivery; and
- keep large course files outside application containers through presigned S3 URLs.

## System topology

![High-level architecture](<../Diagrams/High Level Architecture Design.jpg>)

At runtime, the normal path is:

```text
React frontend / external client
              │
              ▼
       API Gateway :8080
       │ JWT + rate limits + routing + BFF
       │
       ├── Eureka :8761 (service discovery)
       ├── IAM Service :8081 ───────── iamDb
       ├── Academic Core :8082 ─────── academicDb ─── S3
       ├── Communication :8083 ─────── communicationServiceDb
       └── Audit Log :8085 ─────────── auditLogDb
              │
              ├── Redis :6379 (rate limits and caches)
              └── Kafka :9092 (domain events and audit events)
```

The Docker network uses service names and Eureka registration for internal discovery. The host ports are exposed for local development and inspection; production deployments should normally expose only the Gateway and the required operational interfaces.

## Service boundaries

| Service | Port | Owns | Main responsibilities |
|---|---:|---|---|
| API Gateway | `8080` | No business database | Gateway routing, JWT validation, identity-header injection, global/per-route rate limits, CORS, dashboard BFF, and downstream resilience |
| Eureka Server | `8761` | Registry state | Service registration and discovery |
| IAM Service | `8081` | `iamDb` | Users, roles, authentication, profiles, account activation, and IAM lifecycle events |
| Academic Core | `8082` | `academicDb` | Courses, departments, prerequisites, enrollments, results, announcements, feedback, material metadata, user snapshots, and the transactional outbox |
| Communication Service | `8083` | `communicationServiceDb` | Notifications, messages, course chat, Kafka consumers, and WebSocket push |
| Audit Log Service | `8085` | `auditLogDb` | Normalized audit-event ingestion, deduplication, persistence, filtering, and administrator queries |
| Frontend | `5173` | Browser state | React/Vite UI, route protection, API clients, TanStack Query hooks, and WebSocket client behavior |

### Database-per-service

No service should read or write another service's database directly:

| Database | Owner | Data examples |
|---|---|---|
| `iamDb` | IAM | Users, roles, student and teacher subtype data |
| `academicDb` | Academic Core | Courses, departments, enrollments, prerequisites, announcements, feedback, outbox events, and user snapshots |
| `communicationServiceDb` | Communication | Notifications, messages, and communication-side user/course snapshots |
| `auditLogDb` | Audit Log | Normalized audit records and source/event metadata |

Cross-service reads use REST clients or local read models. Cross-service side effects use Kafka events.

## Request flow

### Gateway request processing

1. The client sends a request to `http://localhost:8080`.
2. The global Redis/Lua sliding-window limiter allows or rejects the request.
3. The JWT filter allows a small public endpoint list or validates `Authorization: Bearer <token>`.
4. For authenticated requests, client-supplied identity headers are removed. Validated claims are injected as `X-User-Id`, `X-Username`, and `X-Roles`.
5. Spring Cloud Gateway routes the request to a service discovered through Eureka.
6. The downstream service uses the trusted internal headers to build its security context and applies its own business authorization checks.

The Gateway's configured route groups are:

| Route group | Destination |
|---|---|
| `/api/auth/**` | IAM; includes the per-route auth limiter |
| `/api/users/**`, `/api/students/**`, `/api/teachers/**` | IAM |
| `/api/courses/**`, `/api/departments/**`, `/api/enrollments/**`, `/api/enrolled-courses/**`, `/api/announcements/**`, `/api/feedbacks/**` | Academic Core |
| `/api/notifications/**`, `/api/messages/**` | Communication |
| `/ws/**` | Communication WebSocket endpoint |
| `/api/audit-logs/**` | Audit Log |

Dashboard routes are handled directly by the Gateway's `DashboardController`; they aggregate calls to IAM, Academic Core, and Communication-facing data rather than forwarding to one downstream route.

### Synchronous versus asynchronous collaboration

```text
Synchronous REST/Feign:
Gateway ──► IAM / Academic Core / Communication / Audit Log
Academic Core ──► IAM for selected user lookups
Gateway BFF ──► IAM + Academic Core for dashboard aggregation

Asynchronous Kafka:
IAM ──► user lifecycle events ──► Academic user snapshots + Audit Log
Academic Core ──► enrollment/course/announcement/feedback events ──► Communication + Audit Log
Communication ──► notification-push ──► Audit Log and downstream consumers
```

## Service design

### API Gateway

The Gateway uses Spring Cloud Gateway/WebFlux. It is responsible for edge concerns rather than business rules:

- service discovery and load-balanced routing through Eureka;
- JWT signature and expiry validation;
- removal of spoofable client identity headers before trusted headers are added;
- CORS for the local frontend at `http://localhost:5173`;
- three rate-limit layers described below;
- BFF dashboard aggregation; and
- Resilience4j circuit breakers, retries, bulkheads, and local fallback wrappers for selected Feign callers.

### IAM Service

IAM is a Spring MVC service with stateless Spring Security and JJWT. It uses JOINED inheritance for the `User`, `Student`, `Teacher`, and `Admin` model. Flyway owns the schema migrations and Hibernate runs with validation rather than generating schema changes.

The service also demonstrates:

- a Facade for student dashboard aggregation;
- a Strategy-based academic-standing implementation;
- Spring AOP for logging, execution timing, and service-side rate limiting; and
- Kafka publication after user lifecycle changes.

New registrations become Students by default. The current development implementation accepts `teacher123` as the teacher code to create a Teacher. The seeded development Admin is configured in IAM's application configuration.

### Academic Core

Academic Core follows a hexagonal/ports-and-adapters structure:

```text
Inbound adapters: REST controllers, Kafka consumers
            │
            ▼
Application ports and use cases
            │
            ▼
Domain entities and business rules
            │
            ├── outbound persistence ports ──► JPA/MySQL adapters
            ├── outbound event ports ─────────► Kafka publisher
            ├── IAM client adapter ───────────► IAM Service
            └── object-storage port ──────────► Amazon S3 adapter
```

Course ownership is enforced by `CourseTeacherOnlyAspect`. Teachers can manage their own courses; Admins bypass the ownership check. Enrollment uses pessimistic locking on the course row so concurrent requests cannot oversubscribe the available capacity.

Academic Core also maintains a local `UserSnapshot` read model. IAM lifecycle events update it, while a startup/scheduled bootstrap can reconcile snapshots through IAM's basic-user endpoints after missed events or deployments.

### Communication Service

Communication uses a layered design:

```text
Controllers / WebSocket handlers
              │
              ▼
Service interfaces and implementations
              │
              ▼
Repositories and mappers
              │
              ▼
communicationServiceDb
```

Kafka listeners turn academic and IAM events into notifications or local communication updates. STOMP over WebSocket provides course chat broadcasts and per-user notification delivery. The `/ws` handshake and STOMP `CONNECT` frame are authenticated by `WebSocketAuthInterceptor`.

### Audit Log Service

Audit Log consumes security and domain topics in the `audit-log-v1` consumer group. It maps topic records into normalized `AuditLog` rows, uses the Kafka topic/partition/offset as a processing identity for deduplication, and exposes paginated and role-oriented query endpoints.

Failed Kafka records use provisioned dead-letter topics with a `.DLT` suffix. The DLT setup is intended to preserve failed records for investigation and replay rather than silently dropping them.

## Event-driven design

The following topics are created by Compose or service configuration:

| Topic | Publisher | Consumers / purpose |
|---|---|---|
| `user-registered-v1` | IAM | Academic user snapshot, Audit Log |
| `user-updated-v1` | IAM | Academic user snapshot, Audit Log |
| `user-deactivated-v1` | IAM | Academic user snapshot, Audit Log |
| `user-deleted-v1` | IAM | Academic user snapshot, Audit Log |
| `student-registered` | IAM | Academic user snapshot, Communication welcome flow, Audit Log |
| `student-enrolled` | Academic Core | Communication enrollment notification, Audit Log |
| `student-unenrolled` | Academic Core | Communication cleanup/notification, Audit Log |
| `course-created` | Academic Core | Communication handling, Audit Log |
| `course-deleted` | Academic Core | Communication cleanup, Audit Log |
| `announcement-created` | Academic Core | Communication fan-out, Audit Log |
| `feedback-created` | Academic Core | Audit Log |
| `notification-push` | Communication or another producer | Notification delivery and Audit Log |
| `security-audit-events.v1` | Security-event producers | Audit Log |

Compose also provisions dead-letter variants such as `student-enrolled.DLT`, `course-created.DLT`, and `security-audit-events.v1.DLT`.

### Transactional outbox

Academic mutations and their domain-event records are written to the same `outbox_event` transaction. A background relay claims pending rows, publishes them to Kafka, and marks them processed. This avoids the failure window in which a database transaction commits but its event is lost because Kafka is unavailable.

## Reliability and consistency patterns

### Rate limiting

All rate limiters use Redis sorted sets and an atomic Lua sliding-window script:

| Layer | Scope | Current limit |
|---|---|---:|
| Gateway global filter | Every Gateway route, per IP | 150 requests/minute |
| Gateway `LuaRateLimiter` route filter | `/api/auth/**`, per IP | 20 requests/minute |
| IAM `@RateLimit` aspect | Login method | 100 requests/minute |
| IAM `@RateLimit` aspect | Register method | 200 requests/minute |

The Gateway layers fail open if Redis is unavailable; rejected requests return HTTP `429` and rate-limit headers. The IAM method limits are independent of the Gateway limits.

### Caching

Academic Core uses Redis-backed Spring caches for course, enrollment, announcement, and feedback reads. Mutating use cases evict affected keys to avoid stale reads. Communication also uses Redis-backed service-level caching where configured.

### Downstream resilience

Gateway and Academic Core configure Resilience4j for selected IAM/Academic callers:

- circuit breakers open after the configured failure threshold and permit half-open probes;
- retries use exponential backoff for transient failures;
- bulkheads cap concurrent downstream calls; and
- caller wrappers return controlled fallback responses where a remote failure can be tolerated.

### Amazon S3 materials

Academic Core stores material metadata in `academicDb`, not file bytes. The flow is:

1. A teacher requests an upload URL.
2. Academic Core creates a pending material record and returns a short-lived presigned S3 URL.
3. The client uploads directly to S3.
4. The teacher completes the upload; Academic Core verifies the object and updates metadata.
5. Authorized clients request presigned download URLs when they need the file.

## Security model

```text
Client
  │ Authorization: Bearer <JWT>
  ▼
Gateway
  ├─ validates HS256 signature and expiry
  ├─ strips client-supplied X-User-Id/X-Username/X-Roles
  └─ injects claims as trusted internal headers
       │
       ▼
Downstream service
  ├─ builds its security context from trusted headers
  └─ applies endpoint/domain authorization
```

The current JWT expiry is 24 hours. Public Gateway routes are login, registration, popular courses, all departments, recent feedback, and the WebSocket handshake; the WebSocket STOMP connection still requires a valid JWT in its `CONNECT` frame. See [API.md](API.md) for the exact public list and endpoint access notes.

The Gateway's actuator paths are denied externally. Services expose health and info for Docker/Kubernetes probes. Do not use the development secrets from Compose in a deployed environment.

## Technology stack

| Layer | Technology |
|---|---|
| Backend | Java 17/21, Spring Boot, Spring MVC/WebFlux, Spring Cloud |
| Discovery and routing | Netflix Eureka, Spring Cloud Gateway |
| Security | Spring Security, JJWT, BCrypt |
| Persistence | MySQL 8, Spring Data JPA/Hibernate, Flyway |
| Cache and limits | Redis 7.2, Spring Data Redis, Lua |
| Messaging | Apache Kafka 7.5, Spring Kafka, Zookeeper |
| File storage | Amazon S3, AWS SDK v2, presigned URLs |
| Real time | STOMP over WebSocket with SockJS |
| Frontend | React, TypeScript, Vite, TanStack Query, Axios, TailwindCSS |
| Delivery | Docker Compose, GitHub Actions |
| Performance testing | Grafana k6 with JSON summaries and HTML dashboards |

## Design references

The repository contains additional diagrams and requirements material in [`Diagrams/`](../Diagrams/):

- [Entity Relationship Diagram](../Diagrams/ERD.md)
- [Sequence diagrams](../Diagrams/Sequence.md)
- [High-level architecture image](<../Diagrams/High Level Architecture Design.jpg>)
- Use-case, activity, class-diagram, and SRS files in the same directory
