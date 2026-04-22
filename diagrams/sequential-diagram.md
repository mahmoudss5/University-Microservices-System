# UniSystem - Sequential Diagrams

This document contains sequence diagrams illustrating the main workflows of the UniSystem application.

## 1. User Registration & Authentication Flow

```mermaid
sequenceDiagram
    participant Client as React Client
    participant AuthController as Auth Controller
    participant AuthService as Auth Service
    participant UserRepo as User Repository
    participant JWT as JWT Service
    participant DB as MySQL Database

    Note over Client,DB: User Registration Flow
    Client->>AuthController: POST /api/auth/register
    AuthController->>AuthService: register(UserRequest)
    AuthService->>AuthService: BCrypt.encode(password)
    AuthService->>UserRepo: save(User)
    UserRepo->>DB: INSERT INTO users
    DB-->>UserRepo: User saved
    UserRepo-->>AuthService: User entity
    AuthService->>JWT: generateToken(User)
    JWT-->>AuthService: JWT Token
    AuthService-->>AuthController: AuthResponse (token, username)
    AuthController-->>Client: 201 Created + AuthResponse

    Note over Client,DB: User Login Flow
    Client->>AuthController: POST /api/auth/login
    AuthController->>AuthService: login(AuthRequest)
    AuthService->>UserRepo: findByEmail(email)
    UserRepo->>DB: SELECT * FROM users WHERE email=?
    DB-->>UserRepo: User data
    UserRepo-->>AuthService: User entity
    AuthService->>AuthService: BCrypt.matches(password, hash)
    alt Valid Credentials
        AuthService->>JWT: generateToken(User)
        JWT-->>AuthService: JWT Token
        AuthService-->>AuthController: AuthResponse (token)
        AuthController-->>Client: 200 OK + AuthResponse
    else Invalid Credentials
        AuthService-->>AuthController: Authentication Failed
        AuthController-->>Client: 401 Unauthorized
    end
```

## 2. AOP-guarded Course Creation (Teacher Only)

```mermaid
sequenceDiagram
    participant Client as React Client
    participant CourseController as Course Controller
    participant SecurityAspect as SecurityAspect @Before
    participant UserService as User Service
    participant CourseService as Course Service
    participant CourseRepo as Course Repository
    participant AuditAspect as AuditLogAspect @After
    participant AuditRepo as Audit Log Repository
    participant DB as MySQL Database

    Client->>CourseController: POST /api/courses (JWT)
    CourseController->>SecurityAspect: @TeachersOnly intercept
    SecurityAspect->>UserService: findUserById(currentUserId)
    UserService->>DB: SELECT user + roles
    DB-->>UserService: User entity
    UserService-->>SecurityAspect: User
    SecurityAspect->>SecurityAspect: check roles contains TEACHER
    alt Not a Teacher
        SecurityAspect-->>Client: 403 Forbidden
    else Is Teacher
        SecurityAspect->>CourseController: proceed()
        CourseController->>CourseService: createCourse(CourseRequest)
        CourseService->>CourseRepo: save(Course)
        CourseRepo->>DB: INSERT INTO courses
        DB-->>CourseRepo: Course saved
        CourseRepo-->>CourseService: Course entity
        CourseService-->>CourseController: CourseResponse
        CourseController-->>Client: 201 Created + CourseResponse
        AuditAspect->>AuditRepo: save(AuditLog — CREATE COURSE)
        AuditRepo->>DB: INSERT INTO audit_logs
    end
```

## 3. Student Enrollment Flow

```mermaid
sequenceDiagram
    participant Client as React Client
    participant EnrollController as Enrolled Course Controller
    participant EnrollService as Enrolled Course Service
    participant StudentRepo as Student Repository
    participant CourseRepo as Course Repository
    participant EnrollRepo as Enrolled Course Repository
    participant DB as MySQL Database

    Client->>EnrollController: POST /api/enrolled-courses (JWT)
    EnrollController->>EnrollService: enrollStudent(EnrolledCourseRequest)

    EnrollService->>StudentRepo: findById(studentId)
    StudentRepo->>DB: SELECT FROM students
    DB-->>StudentRepo: Student data
    StudentRepo-->>EnrollService: Student entity

    EnrollService->>CourseRepo: findById(courseId)
    CourseRepo->>DB: SELECT FROM courses
    DB-->>CourseRepo: Course data
    CourseRepo-->>EnrollService: Course entity

    EnrollService->>EnrollRepo: existsByStudentAndCourse()
    alt Already Enrolled
        EnrollService-->>EnrollController: Error: Already enrolled
        EnrollController-->>Client: 400 Bad Request
    else Course Full
        EnrollService-->>EnrollController: Error: Course full
        EnrollController-->>Client: 400 Bad Request
    else Can Enroll
        EnrollService->>EnrollRepo: save(EnrolledCourse)
        EnrollRepo->>DB: INSERT INTO enrolled_courses
        DB-->>EnrollRepo: Enrollment saved
        EnrollRepo-->>EnrollService: EnrolledCourse entity
        EnrollService-->>EnrollController: EnrolledCourseResponse
        EnrollController-->>Client: 201 Created + EnrollmentResponse
    end
```

## 4. Course Chat — Send & Receive Message

```mermaid
sequenceDiagram
    participant Client as React Client A (Sender)
    participant MsgController as Message Controller
    participant MsgService as Message Service
    participant MsgRepo as Message Repository
    participant Broker as STOMP Message Broker
    participant OtherClient as React Client B (Receiver)
    participant DB as MySQL Database

    Note over Client,OtherClient: Both clients connected via WebSocket
    OtherClient->>Broker: SUBSCRIBE /topic/course/{courseId}
    Client->>Broker: SUBSCRIBE /topic/course/{courseId}

    Note over Client,DB: Send a message via REST
    Client->>MsgController: POST /api/messages {courseId, senderId, content}
    MsgController->>MsgService: CreateMessage(MessageRequest)
    MsgService->>MsgRepo: save(Message)
    MsgRepo->>DB: INSERT INTO messages
    DB-->>MsgRepo: Message saved
    MsgRepo-->>MsgService: Message entity
    MsgService->>Broker: convertAndSend(/topic/course/{courseId}, MessageResponse)
    Broker-->>OtherClient: MESSAGE (live chat delivery)
    Broker-->>Client: MESSAGE (echo to sender)
    MsgService-->>MsgController: void
    MsgController-->>Client: 201 Created

    Note over Client,DB: Load chat history
    Client->>MsgController: GET /api/messages/course/{courseId}
    MsgController->>MsgService: GetMessagesByCourseId(courseId)
    MsgService->>MsgRepo: findByCourseIdOrderByCreatedAtAsc(courseId)
    MsgRepo->>DB: SELECT FROM messages WHERE course_id=?
    DB-->>MsgRepo: Messages list
    MsgRepo-->>MsgService: List<Message>
    MsgService-->>MsgController: List<MessageResponse>
    MsgController-->>Client: 200 OK + messages list
```

## 5. Notification Flow — Send & Mark as Read

```mermaid
sequenceDiagram
    participant Sender as System / Admin
    participant NotifController as Notification Controller
    participant NotifService as Notification Service
    participant NotifRepo as Notification Repository
    participant Broker as STOMP Message Broker
    participant Client as React Client (Recipient)
    participant DB as MySQL Database

    Note over Sender,DB: Push a notification to a user
    Sender->>NotifController: POST /api/notifications/user/send {userId, title, message, type}
    NotifController->>NotifService: sendNotificationToUser(NotificationRequest)
    NotifService->>NotifRepo: save(Notification)
    NotifRepo->>DB: INSERT INTO notifications
    DB-->>NotifRepo: Notification saved
    NotifRepo-->>NotifService: Notification entity
    NotifService->>Broker: convertAndSendToUser(userId, /queue/notifications, NotificationResponse)
    Broker-->>Client: MESSAGE (live push)
    NotifService-->>NotifController: void
    NotifController-->>Sender: 200 OK

    Note over Client,DB: Fetch unread count (badge)
    Client->>NotifController: GET /api/notifications/user/{userId}/unread/count
    NotifController->>NotifService: countUnreadForUser(userId)
    NotifService->>NotifRepo: countByUserIdAndIsRead(userId, false)
    NotifRepo->>DB: SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0
    DB-->>NotifRepo: count
    NotifRepo-->>NotifService: Long
    NotifService-->>NotifController: Long
    NotifController-->>Client: 200 OK + count

    Note over Client,DB: Mark all as read
    Client->>NotifController: PATCH /api/notifications/user/{userId}/read-all
    NotifController->>NotifService: markAllAsReadForUser(userId)
    NotifService->>NotifRepo: markAllAsRead(userId)
    NotifRepo->>DB: UPDATE notifications SET is_read=1 WHERE user_id=?
    DB-->>NotifRepo: rows updated
    NotifRepo-->>NotifService: int count
    NotifService-->>NotifController: int count
    NotifController-->>Client: 200 OK + updatedCount
```

## 6. Feedback Submission Flow

```mermaid
sequenceDiagram
    participant Client as React Client
    participant FeedbackController as Feedback Controller
    participant FeedbackService as Feedback Service
    participant UserRepo as User Repository
    participant FeedbackRepo as Feedback Repository
    participant DB as MySQL Database

    Note over Client,DB: Submit Feedback
    Client->>FeedbackController: POST /api/feedbacks (JWT)
    FeedbackController->>FeedbackService: createFeedback(FeedbackRequest)
    FeedbackService->>UserRepo: findById(userId)
    UserRepo->>DB: SELECT * FROM users WHERE id=?
    DB-->>UserRepo: User data
    UserRepo-->>FeedbackService: User entity
    FeedbackService->>FeedbackRepo: save(Feedback)
    FeedbackRepo->>DB: INSERT INTO feedbacks
    DB-->>FeedbackRepo: Feedback saved
    FeedbackRepo-->>FeedbackService: Feedback entity
    FeedbackService-->>FeedbackController: FeedbackResponse
    FeedbackController-->>Client: 201 Created + FeedbackResponse

    Note over Client,DB: Get All Feedbacks (Home page display)
    Client->>FeedbackController: GET /api/feedbacks
    FeedbackController->>FeedbackService: getAllFeedbacks()
    FeedbackService->>FeedbackRepo: findAll()
    FeedbackRepo->>DB: SELECT * FROM feedbacks
    DB-->>FeedbackRepo: Feedbacks list
    FeedbackRepo-->>FeedbackService: List<Feedback>
    FeedbackService-->>FeedbackController: List<FeedbackResponse>
    FeedbackController-->>Client: 200 OK + Feedbacks List
```

## 7. Dashboard Data Loading Flow

```mermaid
sequenceDiagram
    participant Client as React Client
    participant AuthCtx as AuthContext
    participant EnrollHook as useGetAllEnrolledCourses
    participant EnrollSvc as EnrolledCourse Service
    participant CourseHook as useGetAllTeacherCourses
    participant CourseSvc as Course Service
    participant DB as MySQL Database

    Note over Client,DB: Student Dashboard load
    Client->>AuthCtx: getUserId() + getRole()
    AuthCtx-->>Client: userId, role = STUDENT

    Client->>EnrollHook: mount
    EnrollHook->>EnrollSvc: GET /api/enrolled-courses/student/{id}
    EnrollSvc->>DB: SELECT enrolled_courses JOIN courses
    DB-->>EnrollSvc: enrolled course rows
    EnrollSvc-->>EnrollHook: List<EnrolledCourseResponse>
    EnrollHook-->>Client: { enrolledCourses, isLoading, error }
    Client->>Client: filter by isCompleted(endDate) → inProgress / completed tabs

    Note over Client,DB: Teacher Dashboard load
    Client->>AuthCtx: getUserId() + getRole()
    AuthCtx-->>Client: userId, role = TEACHER

    Client->>CourseHook: mount
    CourseHook->>CourseSvc: GET /api/courses?teacher={username}
    CourseSvc->>DB: SELECT courses WHERE teacher_id=?
    DB-->>CourseSvc: course rows
    CourseSvc-->>CourseHook: List<CourseResponse>
    CourseHook-->>Client: { teacherCourses, isLoading, error }
    Client->>Client: filter by isCompleted(endDate) → active / completed tabs
```

## Technology Stack

### Backend

- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21
- **Security**: Spring Security + JWT (jjwt 0.11.5) + OAuth2 GitHub
- **AOP**: Spring Boot Starter AOP (security + audit aspects)
- **WebSocket**: Spring Boot Starter WebSocket (STOMP broker)
- **Database**: MySQL with Flyway V1–V7
- **Cache**: Spring Data Redis
- **API Documentation**: SpringDoc OpenAPI 2.7.0

### Frontend

- **Framework**: React 19.2.0
- **Build Tool**: Vite 7.3.1
- **Routing**: React Router DOM 7.13.0
- **Data Fetching**: TanStack Query (custom hooks)
- **Styling**: TailwindCSS 4.2.0
- **Animations**: Framer Motion 12.34.3
- **Icons**: Lucide React 0.575.0

### Infrastructure

- **Containerization**: Docker Compose (MySQL)
- **Monitoring**: Spring Boot Actuator
- **Validation**: Jakarta Validation
- **ORM**: Spring Data JPA + Hibernate
