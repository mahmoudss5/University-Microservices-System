# API and Technical Reference

[← Back to the project README](../README.md) · [← Architecture and system design](ARCHITECTURE.md)

This document describes the HTTP, WebSocket, and event interfaces currently implemented in the repository. The recommended base URL for clients is:

```text
http://localhost:8080
```

The Gateway routes requests to the appropriate service through Eureka. Direct service URLs are useful for local debugging, but they are not the intended public interface.

## Quick reference

| Service | Direct base URL | Gateway route prefix |
|---|---|---|
| API Gateway | `http://localhost:8080` | local Gateway controllers and all routed APIs |
| IAM | `http://localhost:8081` | `/api/auth`, `/api/users`, `/api/students`, `/api/teachers` |
| Academic Core | `http://localhost:8082` | `/api/courses`, `/api/departments`, `/api/enrolled-courses`, `/api/announcements`, `/api/feedbacks` |
| Communication | `http://localhost:8083` | `/api/notifications`, `/api/messages`, `/ws` |
| Audit Log | `http://localhost:8085` | `/api/audit-logs` |

The route configuration also contains `/api/enrollments/**` as an Academic Core route, but the current controllers expose enrollment operations under `/api/enrolled-courses`; there is no separate `/api/enrollments` controller at present.

## Authentication and authorization

Authenticated HTTP requests use:

```http
Authorization: Bearer <jwt>
Content-Type: application/json
```

The login and registration response has this shape:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "student1"
}
```

JWTs are signed with the shared HS256 secret and currently expire after 24 hours. Roles are `STUDENT`, `TEACHER`, and `ADMIN`. Through the Gateway, the JWT filter validates the token and forwards trusted `X-User-Id`, `X-Username`, and `X-Roles` headers to downstream services.

### Public Gateway routes

These paths do not require a JWT at the Gateway:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/courses/popular`
- `GET /api/departments/all`
- `GET /api/feedbacks/recent`
- the `/ws` WebSocket handshake

The WebSocket handshake is permitted by the Gateway, but the STOMP `CONNECT` frame must include a valid `Authorization: Bearer <jwt>` header.

All other Gateway paths require a valid JWT. Direct service calls can have different framework-level behavior, so use the Gateway when testing the real client security path.

### Access legend

The tables use these labels:

| Label | Meaning |
|---|---|
| Public | No JWT required through the Gateway |
| JWT | A valid JWT is required through the Gateway |
| Teacher/Admin | Academic Core's course-ownership aspect allows a Teacher who owns the course or an Admin |
| Admin | Explicit Admin method authorization or administrative operation |
| Internal | Used by another service for a read-model/bootstrap call; not a frontend contract |

Some older service code permits more operations than the role labels imply when called directly. The Gateway is the security boundary for normal clients, while domain-specific checks are enforced in the owning service.

## IAM Service

### Authentication

| Method | Endpoint | Access | Request body | Description |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | Public | `username`, `email`, `password`, optional `teacherCode` | Creates a Student by default or a Teacher when the current development code `teacher123` is supplied; returns a JWT |
| `POST` | `/api/auth/login` | Public | `email`, `password` | Authenticates a user and returns a JWT |

Example registration:

```json
{
  "username": "student1",
  "email": "student1@uni.edu",
  "password": "password123"
}
```

Validation rules include a username of 3–50 characters, a valid email, and a password of at least 6 characters. Authentication routes have a 20 requests/minute Gateway limiter; the IAM method-level limits are currently 200 requests/minute for registration and 100 requests/minute for login.

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users/me` | JWT | Return the current user's profile |
| `GET` | `/api/users/{id}` | JWT | Return a user by ID |
| `GET` | `/api/users` | JWT | Return all users |
| `GET` | `/api/users/role/{role}` | Admin | Filter users by `STUDENT`, `TEACHER`, or `ADMIN` |
| `PUT` | `/api/users/{id}` | JWT | Update username, email, and/or password |
| `POST` | `/api/users/{id}/activate` | JWT | Activate a user and publish the user-updated event |
| `POST` | `/api/users/{id}/deactivate` | JWT | Deactivate a user and publish the user-deactivated event |
| `DELETE` | `/api/users/{id}` | Admin | Delete a user and publish the user-deleted event |

Update body fields are optional:

```json
{
  "username": "updated-name",
  "email": "updated@uni.edu",
  "password": "new-password"
}
```

### Students

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/students` | JWT | List student profiles |
| `GET` | `/api/students/details/{id}` | JWT | Return the student profile/dashboard aggregation |
| `GET` | `/api/students/basic/{id}` | Internal | Return ID, username, role, and active status |
| `GET` | `/api/students/basic/all` | Internal | Return basic student records for Academic Core snapshot repair |

The basic endpoints are permitted by IAM for service-to-service bootstrap. Through the Gateway they still require a JWT because they are not on the Gateway's public list.

### Teachers

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/teachers` | JWT | List teacher profiles |
| `GET` | `/api/teachers/details/{id}` | JWT | Return the teacher profile/dashboard aggregation |
| `GET` | `/api/teachers/basic/{id}` | Internal | Return a basic teacher record |
| `GET` | `/api/teachers/basic/all` | Internal | Return basic teacher records for Academic Core snapshot repair |

## Academic Core Service

### Courses

| Method | Endpoint | Access | Request body/query | Description |
|---|---|---|---|---|
| `POST` | `/api/courses` | Teacher/Admin | `CreateCourseRequest` | Create a course; Admins and Teachers are allowed, without ownership because the course is new |
| `GET` | `/api/courses/{id}` | JWT | — | Get course details |
| `GET` | `/api/courses/all` | JWT | — | List course cards |
| `POST` | `/api/courses/by-ids` | JWT | `{ "ids": [1, 2, 3] }` | Load a list of courses by IDs |
| `GET` | `/api/courses/popular` | Public | optional `limit`, default `8` | List popular courses |
| `GET` | `/api/courses/teacher/name/{teacherName}` | JWT | — | Find courses by teacher name |
| `GET` | `/api/courses/teacher/{teacherId}` | JWT | — | Find courses by teacher ID |
| `GET` | `/api/courses/Department/{departmentName}` | JWT | — | Find courses by department name; the current path uses a capital `D` |
| `PUT` | `/api/courses/{id}` | Teacher/Admin | `UpdateCourseRequest` | Update a course; only the assigned Teacher or an Admin may do so |
| `DELETE` | `/api/courses/{id}` | Teacher/Admin | — | Delete a course; only the assigned Teacher or an Admin may do so |

Create and update course fields are:

```json
{
  "name": "Distributed Systems",
  "description": "Service-oriented architecture and distributed computing",
  "courseCode": "CS401",
  "startDate": "2026-10-01",
  "endDate": "2027-01-15",
  "departmentName": "COMPUTER_SCIENCE",
  "userId": 2,
  "creditHours": 3,
  "maxStudents": 40
}
```

### Course prerequisites

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/courses/{courseId}/prerequisites/{prerequisiteCourseId}` | JWT | Add a prerequisite course |
| `GET` | `/api/courses/{courseId}/prerequisites` | JWT | List prerequisites for a course |
| `DELETE` | `/api/courses/{courseId}/prerequisites/{prerequisiteCourseId}` | JWT | Remove a prerequisite |

These paths are covered by the Gateway's `/api/courses/**` route. The current controller does not add a separate Teacher/Admin annotation to them.

### Course materials and S3

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/courses/{courseId}/materials/upload-url` | Teacher/Admin | Create a pending material and a presigned S3 upload URL |
| `POST` | `/api/courses/{courseId}/materials/{materialId}/complete` | Teacher/Admin | Verify the object and mark the material as uploaded |
| `GET` | `/api/courses/{courseId}/materials` | JWT | List material metadata for a course |
| `GET` | `/api/courses/{courseId}/materials/{materialId}/download-url` | JWT | Return a presigned S3 download URL |
| `DELETE` | `/api/courses/{courseId}/materials/{materialId}` | Teacher/Admin | Delete metadata and the S3 object |

Upload initialization body:

```json
{
  "title": "Week 1 slides",
  "originalFilename": "week-1.pdf",
  "contentType": "application/pdf",
  "fileSize": 1048576
}
```

The upload response contains `materialId`, `objectKey`, `uploadUrl`, and `expiresAt`. The client uploads the bytes directly to `uploadUrl`, then calls the `complete` endpoint.

### Departments

| Method | Endpoint | Access | Request body/query | Description |
|---|---|---|---|---|
| `POST` | `/api/departments/create` | JWT | `{ "id": 1, "name": "COMPUTER_SCIENCE" }` | Create a department; names are normalized against the `DepartmentsType` enum |
| `GET` | `/api/departments/all` | Public | — | List all departments |
| `GET` | `/api/departments/{id}` | JWT | — | Get a department by ID |
| `GET` | `/api/departments/name/{name}` | JWT | — | Find departments by case-insensitive name |

### Enrollment

| Method | Endpoint | Access | Request body/query | Description |
|---|---|---|---|---|
| `POST` | `/api/enrolled-courses` | JWT | `{ "studentId": 1, "courseId": 10 }` | Enroll a student in a course |
| `DELETE` | `/api/enrolled-courses/{id}` | JWT | — | Delete an enrollment by ID |
| `DELETE` | `/api/enrolled-courses/drop` | JWT | `studentId`, `courseId` query parameters | Drop a student from a course |
| `GET` | `/api/enrolled-courses/student/{studentId}` | JWT | — | List a student's enrollments |
| `GET` | `/api/enrolled-courses/course/{courseId}` | JWT | — | List a course's enrollments/roster |
| `GET` | `/api/enrolled-courses/student/{studentId}/course/{courseId}` | JWT | — | Find one student-course enrollment |
| `PATCH` | `/api/enrolled-courses/student/{studentId}/course/{courseId}/result` | JWT | `{ "grade": 85.5, "passed": true }` | Record a course result |

Academic Core uses pessimistic course locking during enrollment to protect capacity under concurrent requests. Enrollment mutations publish `student-enrolled` or `student-unenrolled` events through the transactional outbox.

### Announcements

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/announcements/create` | Teacher/Admin | Create an announcement for a course; the assigned Teacher or an Admin may create it |
| `GET` | `/api/announcements/course/{courseId}` | JWT | List announcements for a course |
| `GET` | `/api/announcements/student/{studentId}` | JWT | List announcements relevant to a student |
| `GET` | `/api/announcements/teacher/{teacherId}` | JWT | List announcements created by a teacher |

Create body:

```json
{
  "title": "Midterm information",
  "content": "The midterm will be held next week.",
  "courseId": 10,
  "createdAt": "2026-10-04T12:00:00"
}
```

### Feedback

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/feedbacks` | JWT | Submit feedback |
| `GET` | `/api/feedbacks` | JWT | List all feedback |
| `GET` | `/api/feedbacks/recent` | Public | List recent feedback |
| `GET` | `/api/feedbacks/{id}` | JWT | Get one feedback record |
| `GET` | `/api/feedbacks/course/{courseId}` | JWT | List feedback for a course |
| `GET` | `/api/feedbacks/user/{userId}` | JWT | List feedback submitted by a user |

Feedback submission fields are `id` (optional), `userId`, `courseId`, `comment`, and `createdAt`.

## Communication Service

The Gateway requires a JWT for Communication REST paths. The direct Communication Spring Security configuration currently permits `/api/**`; use the Gateway for the normal authenticated behavior.

### Notifications

`NotificationType` values are `ENROLLMENT`, `ANNOUNCEMENT`, `COURSE_UPDATE`, `GRADE`, and `SYSTEM`.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/notifications` | JWT | Create and persist a notification |
| `POST` | `/api/notifications/user/send` | JWT | Persist and push a notification to one user |
| `POST` | `/api/notifications/course` | JWT | Persist and push a notification to enrolled course students |
| `GET` | `/api/notifications/{id}` | JWT | Get a notification by ID |
| `GET` | `/api/notifications/user/{userId}` | JWT | List a user's notifications, newest first |
| `GET` | `/api/notifications/user/{userId}/unread` | JWT | List unread notifications |
| `GET` | `/api/notifications/user/{userId}/unread/count` | JWT | Count unread notifications |
| `GET` | `/api/notifications/user/{userId}/type/{type}` | JWT | Filter notifications by type |
| `PATCH` | `/api/notifications/{id}/read` | JWT | Mark one notification as read |
| `PATCH` | `/api/notifications/user/{userId}/read-all` | JWT | Mark all notifications as read; returns the updated count |
| `DELETE` | `/api/notifications/{id}` | JWT | Delete one notification |
| `DELETE` | `/api/notifications/user/{userId}` | JWT | Delete all notifications for a user |

User notification body:

```json
{
  "recipientId": 1,
  "title": "Enrollment confirmed",
  "message": "You are enrolled in Distributed Systems.",
  "type": "ENROLLMENT"
}
```

Course notification body:

```json
{
  "courseId": 10,
  "title": "New announcement",
  "message": "New course information is available.",
  "type": "ANNOUNCEMENT"
}
```

### Messages

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/messages` | JWT | Persist a message and broadcast it to the course WebSocket topic |
| `GET` | `/api/messages/course/{courseId}` | JWT | List course messages in ascending creation order |
| `GET` | `/api/messages/sender/{senderId}` | JWT | List messages sent by a user |
| `GET` | `/api/messages/course/{courseId}/count` | JWT | Count messages in a course |
| `DELETE` | `/api/messages/{id}` | JWT | Delete a message |

Message body:

```json
{
  "courseId": 10,
  "senderId": 1,
  "content": "Does anyone want to review the assignment together?"
}
```

### WebSocket/STOMP

The SockJS/STOMP endpoint is `/ws` and is routed through the Gateway's `/ws/**` route. On `CONNECT`, send the JWT as a native STOMP header:

```text
Authorization: Bearer <jwt>
```

| Destination | Direction | Payload | Purpose |
|---|---|---|---|
| `/app/course/{courseId}` | Client → server | `MessageRequest` | Save a message and broadcast it to the course |
| `/topic/course/{courseId}` | Server → subscribers | `MessageResponse` | Course chat broadcast |
| `/user/{username}/queue/notifications` | Server → user | `NotificationResponse` | Personal notification push; the server resolves the authenticated user principal |

The Communication service enables `/app` application destinations, `/topic` and `/queue` broker destinations, and `/user` user destinations.

Swagger/OpenAPI is available for the Communication service at:

- UI: `http://localhost:8083/swagger-ui.html`
- JSON: `http://localhost:8083/api-docs`

## Audit Log Service

All endpoints are read-only HTTP queries. The Gateway requires a JWT. Responses are backed by `auditLogDb` and populated asynchronously from Kafka.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/audit-logs` | JWT | Paginated audit events; supports `eventType`, `source`, `userId`, `page`, and `size` |
| `GET` | `/api/audit-logs/{id}` | JWT | Find an event by database ID |
| `GET` | `/api/audit-logs/event/{eventId}` | JWT | Find an event by event ID |
| `GET` | `/api/audit-logs/last-week-students-logs` | JWT | Recent student activity |
| `GET` | `/api/audit-logs/last-week-teachers-logs` | JWT | Recent teacher activity |
| `GET` | `/api/audit-logs/last-week-admins-logs` | JWT | Recent administrator activity |

Example filtered query:

```text
GET /api/audit-logs?eventType=COURSE_CREATED&source=academic-core&userId=2&page=0&size=20
```

The default page size is 20 and the default sort is `occurredAt` descending.

## API Gateway controllers and diagnostics

### Dashboard BFF

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/gateway/dashboard/student/{id}` | JWT | Aggregate the student dashboard |
| `GET` | `/api/gateway/dashboard/teacher/{id}` | JWT | Aggregate the teacher dashboard |
| `GET` | `/api/gateway/dashboard/user` | JWT | Aggregate the current user's dashboard |

These endpoints make downstream calls with Feign/WebClient-style callers and apply the Gateway's resilience configuration.

### Gateway test endpoint

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/test` | JWT through the Gateway | Returns `API Gateway is working 🚀`; intended as a simple diagnostic |

`/test` is served by the Gateway itself and is not a routed business API.

## Rate-limit behavior

The Gateway sends these headers on rate-limited requests:

```http
X-RateLimit-Limit: <limit>
X-RateLimit-Remaining: <remaining>
X-RateLimit-Window: 60
```

When a limit is exceeded, the response is HTTP `429` and includes `Retry-After: 60`. Redis errors are logged and the Gateway limiters fail open. The current limits are:

| Limiter | Scope | Limit |
|---|---|---:|
| `GlobalRateLimiterFilter` | Every Gateway route per client IP | 150/minute |
| `LuaRateLimiter` | `/api/auth/**` per client IP | 20/minute |
| IAM `@RateLimit` | `register` method per client IP | 200/minute |
| IAM `@RateLimit` | `login` method per client IP | 100/minute |

## Kafka topics and event interfaces

| Topic | Main publisher | Consumers / effect |
|---|---|---|
| `user-registered-v1` | IAM | Academic user snapshot, Audit Log |
| `user-updated-v1` | IAM | Academic user snapshot, Audit Log |
| `user-deactivated-v1` | IAM | Academic user snapshot, Audit Log |
| `user-deleted-v1` | IAM | Academic user snapshot, Audit Log |
| `student-registered` | IAM | Academic snapshot, Communication welcome flow, Audit Log |
| `student-enrolled` | Academic Core | Communication enrollment flow, Audit Log |
| `student-unenrolled` | Academic Core | Communication flow, Audit Log |
| `course-created` | Academic Core | Communication handling, Audit Log |
| `course-deleted` | Academic Core | Communication cleanup, Audit Log |
| `announcement-created` | Academic Core | Communication fan-out, Audit Log |
| `feedback-created` | Academic Core | Audit Log |
| `notification-push` | Communication/other producers | Notification push, Audit Log |
| `security-audit-events.v1` | Security-event producers | Audit Log |

Academic Core uses an outbox table and relay for its domain events. Kafka failures are redirected to `.DLT` topics provisioned by Compose for investigation.

## Health, configuration, and local infrastructure

### Health endpoints

Each service exposes `/actuator/health` and `/actuator/info` on its direct port for Docker healthchecks and orchestration. The Gateway denies `/actuator/**` so those endpoints are not exposed through the public entry point.

### Local infrastructure ports

| Component | Host port | Local database / notes |
|---|---:|---|
| IAM MySQL | `3310` | `iamDb` |
| Academic MySQL | `3309` | `academicDb` |
| Communication MySQL | `3308` | `communicationServiceDb` |
| Audit Log MySQL | `3311` | `auditLogDb` |
| Redis | `6379` | Rate limits and caches |
| Kafka | `9092` | Host listener; containers use `kafka:29092` |
| Zookeeper | `2181` | Kafka coordination |
| Kafka UI | `8090` | Topic and consumer inspection |

The Compose development database credentials are documented in the Compose file and are not suitable for production. Environment variables used by the services include `JWT_SECRET`, `SPRING_DATA_REDIS_HOST`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`, datasource overrides, and the Academic Core `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY` values.

## Errors and troubleshooting

Common statuses:

| Status | Meaning |
|---:|---|
| `400` | Invalid request body, validation failure, or malformed path/query value |
| `401` | Missing, malformed, expired, or invalid JWT at the Gateway |
| `403` | Authenticated user lacks a required role or course ownership |
| `404` | Entity, course, material, or audit record not found |
| `409` | Business conflict such as duplicate user or enrollment/capacity conflict |
| `429` | Gateway or IAM rate limit exceeded |
| `5xx` | Service, dependency, database, Kafka, or object-storage failure |

If the frontend cannot connect, verify the Gateway is healthy and that `VITE_API_BASE_URL` points to `http://localhost:8080`. If a service cannot register, open Eureka and inspect `docker compose logs <service-name>`. If course materials fail, verify AWS credentials, region, bucket name, and S3 permissions.
