# University Management System — Microservices

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.3-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-cloud)
[![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-7.5-231F20?style=for-the-badge&logo=apache-kafka)](https://kafka.apache.org)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)

</div>

University Management System is a full-stack platform for managing university users, courses, departments, enrollment, announcements, course materials, messaging, notifications, and audit events. The backend is split into independently deployable Spring Boot services and the frontend is a React/Vite application.

## Documentation

Use the document that matches what you are trying to do:

| Document | Contents |
|---|---|
| **This README** | Project overview, prerequisites, installation, first run, development workflow, and verification |
| [Architecture and system design](docs/ARCHITECTURE.md) | Service boundaries, request and event flows, data ownership, patterns, reliability, and security design |
| [API and technical reference](docs/API.md) | Complete endpoint inventory, request bodies, authentication, rate limits, WebSocket topics, Kafka topics, and operational details |

## What the system provides

- JWT authentication with Student, Teacher, and Admin roles.
- Course creation, discovery, prerequisites, enrollment, results, announcements, and feedback.
- Presigned Amazon S3 upload and download URLs for course materials.
- Direct messages, course chat, notifications, and real-time WebSocket updates.
- API Gateway routing, JWT validation, Redis-backed rate limiting, BFF dashboard aggregation, and Resilience4j fallbacks.
- Kafka-based domain events and a durable audit-log service.
- Separate MySQL databases for IAM, Academic Core, Communication, and Audit Log.
- React dashboards for students, teachers, and administrators.

## Service map

| Component | Port | Responsibility |
|---|---:|---|
| API Gateway | `8080` | Public entry point, routing, JWT validation, rate limiting, and dashboard BFF |
| Eureka Server | `8761` | Service discovery registry and dashboard |
| IAM Service | `8081` | Registration, login, JWT issuance, users, students, teachers, and roles |
| Academic Core | `8082` | Courses, departments, prerequisites, enrollments, materials, announcements, and feedback |
| Communication Service | `8083` | Notifications, direct messages, course chat, and WebSocket delivery |
| Audit Log Service | `8085` | Kafka audit-event ingestion, persistence, filtering, and queries |
| React frontend | `5173` | Student, teacher, and admin user interface |

The normal client base URL is `http://localhost:8080`. The frontend and external clients should use the Gateway rather than calling backend services directly.

## Prerequisites

- Docker Desktop with Docker Compose.
- Git.
- Node.js and npm only when running the frontend outside Docker.
- Java 21 and Maven for most backend modules (IAM is configured for Java 17) when running services outside Docker.
- k6 only for the optional stress-test suites.
- AWS credentials with access to the configured S3 bucket when using course-material uploads.

## Installation and first run

### 1. Clone the repository

```bash
git clone https://github.com/maariamashraf/University-Management-System-Microservices.git
cd University-Management-System-Microservices
```

### 2. Configure local environment values

Create a local `.env` file in the repository root for S3 credentials. Do not commit it:

```dotenv
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
```

Academic Core defaults to the `us-east-1` region and the `academic-core-service` bucket. Change the application configuration or Compose environment if your bucket uses another name or region. The stack can start without S3 credentials, but material upload/download operations need valid AWS access.

For a shared or production environment, replace the development JWT secret and database credentials with secret-managed values. The Compose file currently contains local-development defaults so that the stack can be started immediately.

### 3. Build and start the complete stack

```bash
docker compose up --build
```

The first build downloads Maven and npm dependencies. Later builds reuse the named `maven-cache` volume and Docker build layers. To run in the background:

```bash
docker compose up -d --build
```

### 4. Open the applications

| Application | URL |
|---|---|
| Frontend | [http://localhost:5173](http://localhost:5173) |
| API Gateway | [http://localhost:8080](http://localhost:8080) |
| Eureka dashboard | [http://localhost:8761](http://localhost:8761) |
| Kafka UI | [http://localhost:8090](http://localhost:8090) |
| Audit Log Service | [http://localhost:8085](http://localhost:8085) |

Container health endpoints are available internally at `/actuator/health`; the Gateway deliberately blocks `/actuator/**` from external Gateway traffic.

## Basic usage

Register a student account:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"student1","email":"student1@uni.edu","password":"password123"}'
```

Registering without `teacherCode` creates a Student. The current development implementation accepts `teacher123` as the teacher code:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"teacher1","email":"teacher1@uni.edu","password":"password123","teacherCode":"teacher123"}'
```

Log in and save the returned `token` as `TOKEN` for authenticated calls:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student1@uni.edu","password":"password123"}'

export TOKEN='paste-the-token-from-the-response-here'
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

The frontend performs the same flow through the Gateway. See the [complete API reference](docs/API.md) for all routes, payloads, access rules, WebSocket destinations, and event topics.

## Development workflow

### Run the frontend outside Docker

```bash
cd FrontEnd/my-app
npm install
npm run dev
```

The frontend defaults to `http://localhost:8080` as its API base URL. Override it with either `VITE_API_BASE_URL` or the legacy `VITE_API_URL` variable:

```dotenv
VITE_API_BASE_URL=http://localhost:8080
```

Useful frontend commands are `npm run build`, `npm run preview`, and `npm run lint`.

### Run one backend service outside Docker

Start infrastructure first, then run the service from its module directory:

```bash
cd Backend/academic-core-Service
./mvnw spring-boot:run
```

The same pattern applies to the other Maven modules. When running outside Compose, update datasource, Eureka, Kafka, Redis, JWT, and AWS values for the local host environment. Module-level notes are available in the service READMEs under `Backend/` and `FrontEnd/`.

### Stop the stack

```bash
docker compose down
```

This stops containers while preserving named volumes. Removing volumes resets local databases and Kafka/Redis data, so do that only when a clean local reset is intentional.

## Testing and performance checks

Run the backend test suites locally:

```bash
for dir in Backend/*/; do
  if [ -f "${dir}pom.xml" ]; then
    mvn -f "${dir}pom.xml" clean test
  fi
done
```

The GitHub Actions workflow performs the same multi-module build with Java 21 and publishes JUnit reports. Frontend checks are run from `FrontEnd/my-app` with `npm run lint` and `npm run build`.

Optional k6 stress tests and generated dashboards are documented in [performance/README.md](performance/README.md). The suites target the Gateway at `http://localhost:8080` by default.

## Repository layout

```text
University-Management-System-Microservices/
├── Backend/
│   ├── api-gateway/
│   ├── eureka-server/
│   ├── iam-service/
│   ├── academic-core-Service/
│   ├── communication-service/
│   └── AuditLogService/
├── FrontEnd/my-app/
├── Diagrams/
├── performance/
├── docs/
│   ├── ARCHITECTURE.md
│   └── API.md
└── docker-compose.yml
```

Start with [Architecture and system design](docs/ARCHITECTURE.md) to understand how the services collaborate, or go directly to the [API and technical reference](docs/API.md) when integrating with the system.
