# UniSystem - University Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.2.0-blue.svg)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-blue.svg)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

A comprehensive, full-stack university management system built with modern technologies for managing students, teachers, courses, departments, enrollments, announcements, feedback, upcoming events, real-time messaging, and notifications.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

UniSystem is a modern, full-stack university management system designed to streamline academic operations. It provides a comprehensive platform for managing students, teachers, courses, departments, enrollments, announcements, feedback, audit logs, upcoming events, real-time course chat messages, and per-user notifications — with a focus on security, clean architecture, and a responsive user interface.

### Key Highlights

- **Full-Stack Solution**: React + TypeScript frontend with Spring Boot backend
- **RESTful API**: Well-structured REST APIs following best practices
- **WebSocket / Real-time**: STOMP-based WebSocket for live course chat and push notifications
- **AOP-driven Security**: Custom annotations (`@TeachersOnly`, `@CourseTeacherOnly`) enforced by Spring AOP aspects
- **AOP-driven Audit Logging**: `@AuditLog` annotation automatically records every sensitive action
- **Security**: JWT-based authentication, role-based access control, and GitHub OAuth2 login
- **Redis Caching**: Configurable caching layer backed by Redis
- **Detail Views**: Rich detail endpoints for students (with GPA-based academic standing) and teachers (with full course list)
- **Notifications**: Per-user notification inbox with read/unread tracking and type filtering
- **Course Chat**: Per-course group messaging (students + teacher in the same course)
- **Documentation**: Full API documentation via Swagger/OpenAPI
- **Modern UI**: Role-specific dashboards with TailwindCSS, Framer Motion animations, and protected routes

## ✨ Features

### Authentication & Authorization

- User registration and JWT login
- GitHub OAuth2 login (auto-creates account on first login)
- Role-based access control: `Admin`, `Teacher`, `Student`
- Account activation / deactivation
- Stateless security with `OncePerRequestFilter` JWT validation

### AOP — Security & Audit Aspects

- `@TeachersOnly` — method-level annotation that allows access only to users with the `TEACHER` role
- `@CourseTeacherOnly` — allows access only to the teacher who owns the targeted course
- `@AuditLog` — automatically captures and persists action details after successful method execution
- Aspects are applied transparently via Spring AOP, keeping controllers clean

### Student Management

- Full CRUD for student profiles
- GPA, enrollment year, and total credits tracking
- **Student Details endpoint**: returns enrolled courses + academic standing (Excellent / Very Good / Good / Satisfactory / Probation) computed from GPA

### Teacher Management

- Full CRUD for teacher profiles (office location, salary)
- **Teacher Details endpoint**: returns full course list with department and enrollment counts

### Course Management

- Full CRUD for courses (name, code, description, start/end dates, credits, max students, department)
- Department and teacher assignment
- Capacity management (enrolled count vs max students)
- Popular courses ranking (ordered by enrollment count)
- Teacher can create, edit, and manage only their own courses (enforced via AOP)

### Department Management

- Full CRUD for departments
- Department-to-course relationships

### Enrollment System

- Student course enrollment and drop
- Duplicate enrollment and capacity checks
- Enrollment history per student and per course

### Announcement System

- Course-linked announcements (title, description, timestamp)
- CRUD per course

### Feedback System

- User feedback with role label and comment
- Filter by role or user

### Upcoming Events

- Per-user event cards (owned by a `user_id` FK)
- Event types: `HIGH_PRIORITY`, `EXAM`, `EVENT`
- Filter by type or by user
- `GET /api/events/upcoming` returns events from now onwards, ordered by date

### Notifications

- Per-user notification inbox with `is_read` tracking
- Notification types: `SYSTEM`, and any custom type defined in `NotificationType`
- CRUD + mark-as-read (single and bulk)
- Unread count endpoint for badge display
- Push delivery over WebSocket

### Course Chat (Messaging)

- Per-course group chat: any enrolled student or the course teacher can send messages
- Messages ordered by creation time (ascending)
- REST endpoints for history retrieval + real-time delivery via WebSocket (STOMP)
- Message count endpoint per course

### Audit Logging

- Tracks user actions (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)
- Implemented as an AOP `@After` advice — zero impact on normal code flow
- Filter by username, action, or combination

## 🛠 Technology Stack

### Frontend

| Technology       | Version |
| ---------------- | ------- |
| React            | 19.2.0  |
| TypeScript       | 5.9.3   |
| Vite             | 7.3.1   |
| React Router DOM | 7.13.0  |
| TailwindCSS      | 4.2.0   |
| Framer Motion    | 12.34.3 |
| Lucide React     | 0.575.0 |
| TanStack Query   | latest  |

### Backend

| Technology          | Details                           |
| ------------------- | --------------------------------- |
| Spring Boot         | 3.4.2                             |
| Java                | 21                                |
| Spring Security     | JWT + OAuth2 GitHub               |
| Spring Data JPA     | Hibernate ORM                     |
| Spring AOP          | Aspect-oriented audit & security  |
| Spring WebSocket    | STOMP real-time messaging         |
| Spring Data Redis   | Caching layer                     |
| MySQL               | 8.0                               |
| Flyway              | Database migrations (V1–V7)       |
| SpringDoc OpenAPI   | 2.7.0 — Swagger UI                |
| Lombok              | Boilerplate reduction             |
| Jakarta Validation  | Request validation                |
| Spring Boot Actuator| Runtime health & metrics          |

### Infrastructure

- **Docker + Docker Compose**: containerised MySQL
- **Maven**: backend build tool
- **npm**: frontend build tool

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Client Layer (Browser)                    │
├─────────────────────────────────────────────────────────────┤
│                React Application (Frontend)                 │
│  Pages · Dashboard · Courses · Auth                         │
│  Custom Hooks · Services · Contexts                         │
│  Utils · Constants · Interfaces                             │
│  Protected Routes · Role-aware Layouts                      │
├─────────────────────────────────────────────────────────────┤
│           Spring Boot Backend (Java)                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  REST Controllers + WebSocket Controller             │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Security Layer (JWT + OAuth2 + CORS)                │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  AOP Layer (@TeachersOnly · @CourseTeacherOnly       │   │
│  │             @AuditLog)                               │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Business Logic (Services)                           │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Data Access Layer (Repositories)                    │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  DTOs · Entities · Enums                             │   │
│  └──────────────────────────────────────────────────────┘   │
├──────────────────────┬──────────────────────────────────────┤
│   MySQL Database     │           Redis Cache                │
│  (Flyway V1–V7)      │                                      │
└──────────────────────┴──────────────────────────────────────┘
```

For detailed diagrams, see:

- [System Design](./diagrams/system-design.md)
- [Sequential Diagrams](./diagrams/sequential-diagram.md)
- [Activity Diagrams](./diagrams/activity-diagram.md)

## 🚀 Getting Started

### Prerequisites

- Java JDK 21+
- Node.js v18+
- MySQL 8.0+
- Maven 3.8+
- npm 9.0+
- Redis (optional — required for caching features)

### 1. Clone the Repository

```bash
git clone https://github.com/mahmoudss5/UniSystem.git
cd UniSystem
```

### 2. Backend Setup

```bash
cd Backend/Uni
```

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/helwanuni
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your-256-bit-secret
jwt.expiration=864000000
teacherCode=teacher123
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Run the application:

```bash
mvn clean install
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`. Flyway runs all migrations (V1–V7) automatically on startup.

### 3. Frontend Setup

```bash
cd FrontEnd/my-app
npm install
npm run dev
```

Frontend starts on `http://localhost:5173`.

### 4. Docker (MySQL only)

```bash
cd Backend/Uni
docker-compose up -d
```

This spins up a MySQL container matching the `application.properties` configuration.

## 📁 Project Structure

```
UniSystem/
├── Backend/
│   └── Uni/
│       ├── src/main/java/UnitSystem/demo/
│       │   ├── Aspect/
│       │   │   ├── Logs/
│       │   │   │   ├── AuditLog.java              # @AuditLog annotation
│       │   │   │   └── AuditLogAspect.java         # After-advice records audit entries
│       │   │   └── Security/
│       │   │       ├── TeachersOnly.java           # @TeachersOnly annotation
│       │   │       ├── CourseTeacherOnly.java      # @CourseTeacherOnly annotation
│       │   │       └── SecurityAspect.java         # Before-advice enforces role checks
│       │   ├── Controllers/
│       │   │   ├── AuthController.java
│       │   │   ├── StudentController.java
│       │   │   ├── TeacherController.java
│       │   │   ├── CourseController.java
│       │   │   ├── DepartmentController.java
│       │   │   ├── EnrolledCourseController.java
│       │   │   ├── AnnouncementController.java
│       │   │   ├── FeedbackController.java
│       │   │   ├── AuditLogController.java
│       │   │   ├── UserController.java
│       │   │   ├── UpcomingEventController.java
│       │   │   ├── MessageController.java          # Course group chat REST
│       │   │   ├── NotificationController.java     # Per-user notifications REST
│       │   │   └── WebSocketController.java        # STOMP WebSocket endpoint
│       │   ├── BusinessLogic/
│       │   │   ├── InterfaceServiceLayer/          # Service interfaces
│       │   │   └── ImpServiceLayer/               # Service implementations
│       │   ├── DataAccessLayer/
│       │   │   ├── Entities/
│       │   │   │   ├── User.java, Student.java, Teacher.java
│       │   │   │   ├── Course.java, Department.java
│       │   │   │   ├── EnrolledCourse.java
│       │   │   │   ├── Announcement.java, Feedback.java
│       │   │   │   ├── AuditLog.java, UpcomingEvent.java
│       │   │   │   ├── Message.java               # Course chat message entity
│       │   │   │   ├── Notification.java          # Per-user notification entity
│       │   │   │   └── NotificationType.java      # Enum: SYSTEM, ...
│       │   │   ├── Repositories/                  # Spring Data repositories
│       │   │   └── Dto/
│       │   │       ├── Auth/, Student/, Teacher/, Course/
│       │   │       ├── Department/, EnrolledCourse/
│       │   │       ├── Announcement/, Feedback/, AuditLog/
│       │   │       ├── User/, UpcomingEvent/, UserDetails/
│       │   │       ├── Message/                   # MessageRequest / MessageResponse
│       │   │       └── Notification/              # NotificationRequest / NotificationResponse
│       │   ├── Security/
│       │   │   ├── config/                        # AppConfig, SecurityConfiguration
│       │   │   ├── Jwt/                           # JwtService, JwtAuthenticationFilter
│       │   │   ├── Oauth2/                        # OAuth2LoginSuccessHandler
│       │   │   ├── User/                          # SecurityUser, CustomUserDetailsService
│       │   │   ├── WebSocket/                     # WebSocketAuthInterceptor
│       │   │   └── Util/                          # SecurityUtils
│       │   └── Config/
│       │       ├── RedisConfig/                   # RedisConfig.java
│       │       ├── WebSocketConfig/               # WebSocketConfig.java (STOMP broker)
│       │       ├── DataSeeder.java                # Seeds roles on startup
│       │       └── SwaggerConfig/
│       └── src/main/resources/
│           ├── application.properties
│           └── db/migration/
│               ├── V1__init_schema.sql            # Core tables
│               ├── V2__AddCourseDescriptionCoulmn.schema.sql
│               ├── V3__AddFeedBackTable.schema.sql
│               ├── V4__AddAnouncmentTable.schema.sql
│               ├── V5__AddUpcomingEventsTable.schema.sql
│               ├── V6__SampleData.sql             # Seed data
│               └── V7__addNotficationAndMessagesTable.schema.sql
├── FrontEnd/
│   └── my-app/
│       └── src/
│           ├── components/
│           │   ├── Auth/                          # LoginForm, RegisterForm
│           │   ├── common/                        # Nav, AsideNav, Footer,
│           │   │                                  # ProtectedRoute, LoadingSpinner
│           │   ├── Courses/                       # AllCourses, CourseCard,
│           │   │                                  # EnrolledCourseCard, TeacherCourseCard
│           │   │                                  # CourseFormModal, StudentsModal
│           │   ├── Dashboard/                     # StatsCard, EnrolledCourses,
│           │   │                                  # TeachingCourses, RecentAnnouncements
│           │   │                                  # UpcomingEvents
│           │   └── Home/                          # Welcome, Departments, DepartmentCard
│           │                                      # PopularCourses, CourseCard,
│           │                                      # FeedBacks, FeedBackCard, FinalSection
│           ├── pages/
│           │   ├── Home.tsx, Auth.tsx, Dashboard.tsx
│           │   ├── DashboardLayout.tsx            # Shared sidebar + header layout
│           │   ├── TeacherDashboard.tsx
│           │   └── Courses/
│           │       ├── CoursesDashboard.tsx       # Admin course view
│           │       ├── Registration.tsx           # Student course browse & enroll
│           │       ├── StudentCoursesDashboard.tsx# In-progress / completed tabs
│           │       └── TeacherCouresesDashboard.tsx# Active / completed + create/edit
│           ├── CustomeHooks/
│           │   ├── CoursesHooks/                  # UseGetAllCourses, UseCreateCourse,
│           │   │                                  # UseDeleteCourse, UseGetAllTeacherCourses
│           │   │                                  # UseGetAllEnrolledCourses, UseGetPopularCourses
│           │   ├── Departments/                   # UseGetDepartments
│           │   ├── EnrollmentsHooks/              # UseEnrollCourse,
│           │   │                                  # UseUnEnrollStudentFromCourse
│           │   │                                  # UseGetAllEnrollmentsByCourseId
│           │   └── FeedBacks/                     # UseGetRecentFeedBacks
│           ├── Services/                          # authService, courseService,
│           │                                      # enrolledCourseService, departmentService
│           │                                      # dashboardService, feedBacks, etc.
│           ├── ContextsProviders/                 # AuthContext, DashboardContext
│           ├── utils/
│           │   ├── courseUtils.tsx                # Palette, icons, status/capacity colors
│           │   ├── dateUtils.ts                   # getSemesterLabel, toInputDate
│           │   ├── announcementUtils.ts           # Icon + color mapping for announcement types
│           │   ├── eventUtils.ts                  # Color helpers for event types
│           │   └── avatarUtils.ts                 # Random color, initials generation
│           ├── constants/
│           │   └── departments.ts                 # DEPARTMENT_VALUES, DEPARTMENT_OPTIONS
│           └── Interfaces/                        # TypeScript interfaces
│               ├── Auth.ts, user.ts, student.ts, teacher.ts
│               ├── course.ts, enrolledCourse.ts
│               ├── Department.ts, announcement.ts
│               ├── feedBack.ts, upComingEvent.ts
│               └── dashboard.ts                   # Dashboard widget interfaces
├── diagrams/
│   ├── system-design.md
│   ├── sequential-diagram.md
│   └── activity-diagram.md
└── README.md
```

## 📚 API Documentation

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

### Authentication

| Method | Endpoint             | Description                   |
| ------ | -------------------- | ----------------------------- |
| POST   | `/api/auth/register` | Register a new user           |
| POST   | `/api/auth/login`    | Login and receive a JWT token |

### Users

| Method | Endpoint                           | Description             |
| ------ | ---------------------------------- | ----------------------- |
| GET    | `/api/users`                       | Get all users           |
| POST   | `/api/users`                       | Create user             |
| PUT    | `/api/users`                       | Update user             |
| DELETE | `/api/users/{userId}`              | Delete user             |
| PATCH  | `/api/users/{userId}/deactivate`   | Deactivate user         |
| PATCH  | `/api/users/deactivate-current`    | Deactivate current user |
| POST   | `/api/users/{userId}/roles/{role}` | Assign role to user     |

### Students

| Method | Endpoint                            | Description                                                     |
| ------ | ----------------------------------- | --------------------------------------------------------------- |
| GET    | `/api/students`                     | Get all students                                                |
| GET    | `/api/students/{id}`                | Get student by ID                                               |
| GET    | `/api/students/username/{userName}` | Get student by username                                         |
| GET    | `/api/students/details/{id}`        | Get full student details (enrolled courses + academic standing) |
| POST   | `/api/students`                     | Create student                                                  |
| PUT    | `/api/students/{id}`                | Update student                                                  |
| DELETE | `/api/students/{id}`                | Delete student                                                  |

### Teachers

| Method | Endpoint                            | Description                                          |
| ------ | ----------------------------------- | ---------------------------------------------------- |
| GET    | `/api/teachers`                     | Get all teachers                                     |
| GET    | `/api/teachers/{id}`                | Get teacher by ID                                    |
| GET    | `/api/teachers/username/{userName}` | Get teacher by username                              |
| GET    | `/api/teachers/details/{id}`        | Get full teacher details (courses + salary + office) |
| POST   | `/api/teachers`                     | Create teacher                                       |
| PUT    | `/api/teachers/{id}`                | Update teacher                                       |
| DELETE | `/api/teachers/{id}`                | Delete teacher                                       |

### Courses

| Method | Endpoint                      | Description               |
| ------ | ----------------------------- | ------------------------- |
| GET    | `/api/courses`                | Get all courses           |
| GET    | `/api/courses/{id}`           | Get course by ID          |
| GET    | `/api/courses/popular`        | Get top 4 popular courses |
| GET    | `/api/courses/popular/{topN}` | Get top N popular courses |
| POST   | `/api/courses`                | Create course `@TeachersOnly` |
| PUT    | `/api/courses/{id}`           | Update course             |
| DELETE | `/api/courses/{id}`           | Delete course             |

### Departments

| Method | Endpoint                       | Description         |
| ------ | ------------------------------ | ------------------- |
| GET    | `/api/departments/all`         | Get all departments |
| GET    | `/api/departments/{id}`        | Get by ID           |
| GET    | `/api/departments/name/{name}` | Get by name         |
| POST   | `/api/departments`             | Create department   |
| PUT    | `/api/departments/{id}`        | Update department   |
| DELETE | `/api/departments/{id}`        | Delete department   |

### Enrollments

| Method | Endpoint                                    | Description                                      |
| ------ | ------------------------------------------- | ------------------------------------------------ |
| GET    | `/api/enrolled-courses`                     | Get all enrollments                              |
| GET    | `/api/enrolled-courses/{id}`                | Get by ID                                        |
| GET    | `/api/enrolled-courses/student/{studentId}` | Get by student                                   |
| GET    | `/api/enrolled-courses/course/{courseId}`   | Get by course                                    |
| POST   | `/api/enrolled-courses`                     | Enroll student `@CourseTeacherOnly` check bypass |
| DELETE | `/api/enrolled-courses/{id}`                | Drop enrollment                                  |

### Announcements

| Method | Endpoint                                      | Description         |
| ------ | --------------------------------------------- | ------------------- |
| POST   | `/api/announcements/create`                   | Create announcement |
| POST   | `/api/announcements/delete/{id}`              | Delete announcement |
| POST   | `/api/announcements/get/{id}`                 | Get by ID           |
| POST   | `/api/announcements/getByCourseId/{courseId}` | Get by course       |
| POST   | `/api/announcements/getAll`                   | Get all             |

### Feedbacks

| Method | Endpoint                       | Description       |
| ------ | ------------------------------ | ----------------- |
| GET    | `/api/feedbacks`               | Get all feedbacks |
| GET    | `/api/feedbacks/{id}`          | Get by ID         |
| GET    | `/api/feedbacks/role/{role}`   | Get by role       |
| GET    | `/api/feedbacks/user/{userId}` | Get by user       |
| POST   | `/api/feedbacks`               | Create feedback   |
| PUT    | `/api/feedbacks/{id}`          | Update feedback   |
| DELETE | `/api/feedbacks/{id}`          | Delete feedback   |

### Audit Logs

| Method | Endpoint                                              | Description      |
| ------ | ----------------------------------------------------- | ---------------- |
| GET    | `/api/audit-logs`                                     | Get all logs     |
| GET    | `/api/audit-logs/{id}`                                | Get by ID        |
| GET    | `/api/audit-logs/username/{userName}`                 | Get by username  |
| GET    | `/api/audit-logs/action/{action}`                     | Get by action    |
| GET    | `/api/audit-logs/action/{action}/username/{userName}` | Filter by both   |
| POST   | `/api/audit-logs`                                     | Create log entry |
| DELETE | `/api/audit-logs/{id}`                                | Delete log       |

### Upcoming Events

| Method | Endpoint                    | Description                                       |
| ------ | --------------------------- | ------------------------------------------------- |
| GET    | `/api/events`               | Get all events                                    |
| GET    | `/api/events/upcoming`      | Get events from now onwards (ordered by date)     |
| GET    | `/api/events/type/{type}`   | Filter by type (`HIGH_PRIORITY`, `EXAM`, `EVENT`) |
| GET    | `/api/events/user/{userId}` | Get events belonging to a user                    |
| GET    | `/api/events/{id}`          | Get event by ID                                   |
| POST   | `/api/events`               | Create event                                      |
| PUT    | `/api/events/{id}`          | Update event                                      |
| DELETE | `/api/events/{id}`          | Delete event                                      |

### Messages (Course Chat)

| Method | Endpoint                              | Description                                   |
| ------ | ------------------------------------- | --------------------------------------------- |
| POST   | `/api/messages`                       | Send a message to a course chat               |
| GET    | `/api/messages/course/{courseId}`     | Get all messages for a course (time asc)      |
| GET    | `/api/messages/sender/{senderId}`     | Get all messages sent by a user               |
| GET    | `/api/messages/course/{courseId}/count` | Count total messages in a course            |
| DELETE | `/api/messages/{id}`                  | Delete a message by ID                        |

**WebSocket** (STOMP): connect to `ws://localhost:8080/ws`, subscribe to `/topic/course/{courseId}` to receive live messages.

### Notifications

| Method | Endpoint                                        | Description                                   |
| ------ | ----------------------------------------------- | --------------------------------------------- |
| POST   | `/api/notifications`                            | Create a notification                         |
| POST   | `/api/notifications/user/send`                  | Send a notification to a specific user        |
| GET    | `/api/notifications/{id}`                       | Get notification by ID                        |
| GET    | `/api/notifications/user/{userId}`              | Get all notifications for a user (newest first) |
| GET    | `/api/notifications/user/{userId}/unread`       | Get unread notifications for a user           |
| GET    | `/api/notifications/user/{userId}/type/{type}`  | Get notifications filtered by type            |
| GET    | `/api/notifications/user/{userId}/unread/count` | Count unread notifications                    |
| PATCH  | `/api/notifications/{id}/read`                  | Mark a notification as read                   |
| PATCH  | `/api/notifications/user/{userId}/read-all`     | Mark all notifications as read                |
| DELETE | `/api/notifications/{id}`                       | Delete a notification                         |
| DELETE | `/api/notifications/user/{userId}`              | Delete all notifications for a user           |

## 🗄 Database Schema

### Flyway Migrations

| Version | File                                                   | Description                                                                        |
| ------- | ------------------------------------------------------ | ---------------------------------------------------------------------------------- |
| V1      | `V1__init_schema.sql`                                  | Core tables: users, students, teachers, roles, courses, departments, enrolled_courses, audit_logs |
| V2      | `V2__AddCourseDescriptionCoulmn.schema.sql`            | Adds `course_description` to courses                                               |
| V3      | `V3__AddFeedBackTable.schema.sql`                      | Creates `feedbacks` table                                                          |
| V4      | `V4__AddAnouncmentTable.schema.sql`                    | Creates `announcements` table (linked to courses)                                  |
| V5      | `V5__AddUpcomingEventsTable.schema.sql`                | Creates `upcoming_events` table (linked to users)                                  |
| V6      | `V6__SampleData.sql`                                   | Inserts seed / sample data for development                                         |
| V7      | `V7__addNotficationAndMessagesTable.schema.sql`        | Creates `notifications` table (per-user) and `messages` table (per-course chat)    |

### Entity Relationships

```
users  (JOINED inheritance parent)
  ├── students          (user_id PK/FK)
  ├── teachers          (user_id PK/FK)
  ├── user_roles        (M:N with roles)
  ├── feedbacks         (1:N)
  ├── audit_logs        (1:N)
  ├── upcoming_events   (1:N)
  └── notifications     (1:N)  ← per-user notification inbox

departments
  └── courses (1:N)

teachers
  └── courses (1:N)

courses
  ├── enrolled_courses  (1:N)
  ├── announcements     (1:N)
  └── messages          (1:N)  ← per-course group chat

students
  └── enrolled_courses  (1:N)
```

### Key Tables

| Table              | Description                                                         |
| ------------------ | ------------------------------------------------------------------- |
| `users`            | Base user table (JOINED inheritance)                                |
| `students`         | Student-specific fields (GPA, enrollment year, credits)             |
| `teachers`         | Teacher-specific fields (office location, salary)                   |
| `roles`            | Role definitions (Student, Teacher, Admin)                          |
| `user_roles`       | User↔Role join table                                                |
| `courses`          | Course catalogue (name, code, description, dates, capacity, credits) |
| `departments`      | Academic departments                                                |
| `enrolled_courses` | Student↔Course enrollment records                                   |
| `announcements`    | Course-linked announcements                                         |
| `feedbacks`        | User feedback with role label                                       |
| `audit_logs`       | User action audit trail                                             |
| `upcoming_events`  | Per-user scheduled events (exam, deadline, event)                   |
| `notifications`    | Per-user notifications with read/unread status                      |
| `messages`         | Per-course group chat messages                                      |

## 🔒 Security

### Authentication Flow

1. User submits credentials → `POST /api/auth/login`
2. Spring Security validates via `DaoAuthenticationProvider`
3. `JwtService` generates a signed HMAC-SHA token (10-day expiry) embedding `userId`, `userName`, and `roles`
4. Subsequent requests carry `Authorization: Bearer <token>`
5. `JwtAuthenticationFilter` validates the token and populates `SecurityContext`

### OAuth2 (GitHub)

- On first GitHub login, a new `User` is created automatically and a JWT is returned via redirect to `localhost:5173`

### AOP Security

- `@TeachersOnly` — applied to endpoints that only teachers may call (e.g., create course). Enforced by `SecurityAspect.checkTeacherRole()` which reads the current user's roles from the `SecurityContext`.
- `@CourseTeacherOnly` — applied to enrollment management endpoints. Enforced by `SecurityAspect.checkCourseTeacher()` which validates that the acting user is the teacher of the targeted course.

### WebSocket Security

- `WebSocketAuthInterceptor` validates the JWT token present in the STOMP `CONNECT` frame before allowing a session to be established.

### Public Endpoints (no token required)

```
POST /api/auth/**
GET  /api/courses/popular
GET  /api/courses/popular/**
GET  /api/departments/all
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

### Password Storage

- BCrypt hashing via `PasswordEncoder` bean

### CORS

- Configured to allow `http://localhost:5173`

## 🧪 Testing

### Backend

```bash
cd Backend/Uni
mvn test
```

### Frontend

```bash
cd FrontEnd/my-app
npm test
```

## 🚢 Deployment

### Production Builds

```bash
# Backend
cd Backend/Uni
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Frontend
cd FrontEnd/my-app
npm run build
# Serve dist/ with a static web server or NGINX
```

### Key Environment Variables

#### Backend (`application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/helwanuni
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your-256-bit-hex-secret
jwt.expiration=864000000
teacherCode=teacher123
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

#### Frontend

```bash
# vite.config.ts proxy or .env
VITE_API_URL=http://localhost:8080
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m 'feat: add your feature'`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

### Code Conventions

- Java: standard naming conventions, service interface + implementation pattern, manual DTO mapping (no MapStruct), AOP annotations for cross-cutting concerns
- TypeScript/React: functional components, typed props, TanStack Query hooks, pure utility functions in `utils/`, shared constants in `constants/`, all interfaces in `Interfaces/`
- Write descriptive commit messages

## 📝 License

This project is licensed under the MIT License.

## 📞 Contact

- GitHub: [@mahmoudss5](https://github.com/mahmoudss5)

---

> This is a learning/portfolio project. Additional security hardening, input validation, and testing are recommended before any production use.
