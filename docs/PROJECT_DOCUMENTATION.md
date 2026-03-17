# EQuipHub - Equipment Request Management System

**Project Version:** 3.8.0  
**Last Updated:** March 17, 2026  
**Documentation Version:** 1.0

---

## Table of Contents

1. [Architecture](#architecture)
2. [Installation Details](#installation-details)
3. [Development](#development)
4. [API Reference](#api-reference)
5. [Testing](#testing)
6. [Reference Details](#reference-details)

---

## Architecture

### System Overview

EQuipHub is a comprehensive equipment request management system designed for university departments (Computer Engineering and Electrical & Electronics Engineering). The system facilitates equipment borrowing, tracking, and management through a multi-tier approval workflow.

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Backend API** | Spring Boot | 3.2.2 |
| **Backend Language** | Java | 21 |
| **Frontend Web** | Next.js | 16.1.6 |
| **Frontend Framework** | React | 19.2.3 |
| **Mobile App** | React Native (Expo) | 54.0.0 |
| **Database** | PostgreSQL | 15 |
| **Cache/Session** | Redis | 7 |
| **Authentication** | JWT | 0.12.5 |
| **API Documentation** | SpringDoc OpenAPI | 2.3.0 |

### Project Structure

```
EQuipHub/
├── backend/
│   └── api/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/equiphub/api/
│       │   │   │   ├── config/          # Configuration classes
│       │   │   │   ├── controller/      # REST controllers
│       │   │   │   ├── dto/             # Data Transfer Objects
│       │   │   │   ├── exception/       # Exception handling
│       │   │   │   ├── model/           # Entity models
│       │   │   │   ├── repository/      # Data repositories
│       │   │   │   ├── security/        # Security components
│       │   │   │   └── service/         # Business logic
│       │   │   └── resources/
│       │   │       ├── db/migration/    # Flyway migrations
│       │   │       └── application.yml  # Configuration
│       │   └── test/                    # Unit tests
│       ├── pom.xml                      # Maven dependencies
│       ├── compose.yaml                 # Docker Compose
│       └── Dockerfile                   # Container definition
│
├── frontend/
│   └── src/
│       ├── app/                         # Next.js App Router
│       │   ├── (auth)/                  # Authentication pages
│       │   ├── admin/                   # Admin dashboard
│       │   ├── department-admin/        # Department admin
│       │   ├── lecturer/                # Lecturer interface
│       │   ├── student/                 # Student interface
│       │   └── technical-officer/       # TO interface
│       ├── components/                  # Reusable components
│       └── lib/                         # Utilities & API client
│
├── app/                                 # React Native Mobile App
│   ├── src/
│   │   ├── components/                  # UI components
│   │   ├── context/                     # React Context
│   │   ├── lib/                         # API & theme
│   │   └── screens/                     # App screens
│   └── package.json
│
└── docs/                                # Documentation
```

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTS                                  │
├─────────────────┬─────────────────┬────────────────────────────┤
│   Web Browser   │   Mobile App    │   External Systems         │
│   (Next.js)     │   (Expo)        │   (Student Info System)    │
└────────┬────────┴────────┬────────┴─────────────┬──────────────┘
         │                 │                      │
         ▼                 ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      LOAD BALANCER / REVERSE PROXY              │
│                    (Nginx - Production Only)                    │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT API (Port 8080)                  │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │ Controllers  │  │  Services    │  │   Security (JWT)     │  │
│  │              │  │              │  │   - Authentication   │  │
│  │ - Auth       │  │ - Request    │  │   - Authorization    │  │
│  │ - Request    │  │ - Equipment  │  │   - CORS             │  │
│  │ - Equipment  │  │ - Approval   │  │                      │  │
│  │ - Admin      │  │ - Email      │  └──────────────────────┘  │
│  │ - TO         │  │ - User       │                            │
│  └──────────────┘  └──────────────┘                            │
├─────────────────────────────────────────────────────────────────┤
│                    DATA LAYER                                   │
│  ┌──────────────────┐  ┌─────────────────────────────────────┐ │
│  │  JPA/Hibernate   │  │           Redis Cache               │ │
│  │  (PostgreSQL)    │  │  - Session Storage                  │ │
│  │                  │  │  - Token Blacklist                  │ │
│  │  - Entities      │  │  - Application Cache                │ │
│  │  - Repositories  │  │                                     │ │
│  └──────────────────┘  └─────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATA STORES                                │
├─────────────────────────┬───────────────────────────────────────┤
│   PostgreSQL (5432)     │         Redis (6379)                  │
│   - equiphub_dev        │         - Session Data                │
│   - Users, Requests,    │         - Cache                       │
│     Equipment, etc.     │         - Rate Limiting               │
└─────────────────────────┴───────────────────────────────────────┘
```

### User Roles & Permissions

| Role | Description | Key Permissions |
|------|-------------|-----------------|
| **SYSTEM_ADMIN** | Full platform control | CRUD all departments, users, view analytics |
| **DEPARTMENT_ADMIN** | Department-level control | CRUD staff, equipment, labs in department |
| **HOD** | Head of Department | Approve PERSONAL, EXTRACURRICULAR requests |
| **LECTURER** | Course management | Approve COURSEWORK, recommend RESEARCH/PERSONAL |
| **LAB_INSTRUCTOR** | Lab safety observation | Observe PERSONAL requests, recommend |
| **TECHNICAL_OFFICER** | Equipment management | Inspect, issue, receive equipment |
| **STUDENT** | Equipment requestor | Submit requests, track status, return equipment |

### Request Workflows

```
┌─────────────────────────────────────────────────────────────────┐
│                    REQUEST TYPES                                │
├─────────────────┬─────────────────┬────────────────────────────┤
│   COURSEWORK    │    RESEARCH     │   EXTRACURRICULAR          │
├─────────────────┼─────────────────┼────────────────────────────┤
│ 1. Auto-check   │ 1. Supervisor   │ 1. HOD Approval            │
│    (6 cond.)    │    Review       │ 2. TO Inspection           │
│ 2. OR Lecturer  │ 2. TO Inspection│                            │
│ 3. TO Inspection│                  │                            │
├─────────────────┴─────────────────┼────────────────────────────┤
│           PERSONAL                  │                            │
├─────────────────────────────────────┤                            │
│ 1. Lecturer Recommendation          │                            │
│ 2. HOD Approval                     │                            │
│ 3. Lab Instructor Observation       │                            │
│ 4. TO Inspection                    │                            │
└─────────────────────────────────────┘                            │
```

---

## Installation Details

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Java** | 21+ | JDK required for backend |
| **Node.js** | 18+ | For frontend development |
| **pnpm** | 8+ | Package manager (recommended) |
| **Docker** | 24+ | For containerized deployment |
| **Docker Compose** | 2.0+ | For orchestration |
| **PostgreSQL** | 15+ | Database (or use Docker) |
| **Redis** | 7+ | Cache (or use Docker) |

### Environment Variables

#### Backend (application.yml)

```yaml
# Database
DATABASE_URL: jdbc:postgresql://localhost:5432/equiphub_dev
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: 12345678

# Redis
REDIS_HOST: localhost
REDIS_PORT: 6379
REDIS_PASSWORD: equiphub_redis_pass

# JWT
JWT_SECRET: YourSuperSecretJWTKeyMinimum256BitsForHS256Algorithm...
JWT_ACCESS_EXPIRATION: 86400000  # 24 hours in ms
JWT_REFRESH_EXPIRATION: 604800000  # 7 days in ms

# Email (Gmail SMTP)
MAIL_USERNAME: eqiphub@gmail.com
MAIL_PASSWORD: your_app_password

# CORS
CORS_ALLOWED_ORIGINS: http://localhost:3000,http://localhost:5173

# Application
SERVER_PORT: 8080
SPRING_PROFILES_ACTIVE: dev
```

### Quick Start (Development)

#### 1. Clone and Setup

```bash
# Navigate to project directory
cd /Users/sasindumalhara/Workspace/EQuipHub

# Start infrastructure (PostgreSQL + Redis)
cd backend/api
docker-compose up -d postgres redis
```

#### 2. Backend Setup

```bash
# Navigate to backend
cd backend/api

# Build the project
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/api-1.0.0.jar
```

The backend will start at: `http://localhost:8080/api/v1`

#### 3. Frontend Setup

```bash
# Navigate to frontend
cd frontend

# Install dependencies
pnpm install
# or
npm install

# Run development server
pnpm dev
# or
npm run dev
```

The frontend will start at: `http://localhost:3000`

#### 4. Mobile App Setup

```bash
# Navigate to mobile app
cd app

# Install dependencies
npm install

# Start Expo
npm start
```

### Docker Deployment (Production)

```bash
# Navigate to backend
cd backend/api

# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

**Services:**
- Backend API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- PgAdmin: `http://localhost:5050` (optional)

---

## Development

### Backend Development

#### Project Structure

```
backend/api/src/main/java/com/equiphub/api/
├── EquipHubApplication.java          # Main entry point
├── config/                           # Configuration classes
│   ├── AppConfig.java               # Application config
│   ├── AsyncConfig.java             # Async processing
│   ├── DataInitializer.java         # Initial data seeding
│   ├── EmailConfig.java             # Email service config
│   ├── JpaAuditingConfig.java       # Audit fields
│   ├── JwtConfig.java               # JWT settings
│   ├── OpenApiConfig.java           # Swagger/OpenAPI
│   ├── RedisConfig.java             # Redis cache
│   └── SecurityConfig.java          # Security configuration
├── controller/                       # REST endpoints
│   ├── AdminController.java
│   ├── ApprovalController.java
│   ├── AuthController.java
│   ├── CourseController.java
│   ├── DepartmentAdminController.java
│   ├── DepartmentController.java
│   ├── EquipmentController.java
│   ├── InspectionController.java
│   ├── PenaltyController.java
│   ├── RequestController.java
│   └── UserManagementController.java
├── dto/                              # Data Transfer Objects
├── exception/                        # Exception handling
│   ├── BadRequestException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── model/                            # JPA Entities
│   ├── User.java
│   ├── Request.java
│   ├── Equipment.java
│   ├── Department.java
│   └── ... (25+ entities)
├── repository/                       # Spring Data Repositories
├── security/                         # Security components
│   ├── CustomUserDetails.java
│   └── CustomUserDetailsService.java
└── service/                          # Business logic
    ├── ApprovalService.java
    ├── CourseService.java
    ├── DepartmentService.java
    ├── EmailService.java
    ├── EquipmentService.java
    ├── RequestService.java
    └── UserManagementService.java
```

#### Key Configuration Files

**application.yml** (`backend/api/src/main/resources/application.yml`):
- Database connection (PostgreSQL)
- Redis cache configuration
- JWT settings
- CORS configuration
- Email (Gmail SMTP)
- API documentation (Swagger)

#### Running Tests

```bash
# Run unit tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run integration tests
./mvnw verify

# Skip tests
./mvnw clean package -DskipTests
```

### Frontend Development

#### Project Structure

```
frontend/src/
├── app/                              # Next.js App Router
│   ├── (auth)/                       # Auth pages
│   │   ├── login/
│   │   ├── register/
│   │   ├── forgot-password/
│   │   └── verify-email/
│   ├── admin/                        # Admin dashboard
│   │   ├── analytics/
│   │   ├── configuration/
│   │   ├── departments/
│   │   └── users/
│   ├── department-admin/             # Dept admin
│   ├── lecturer/                     # Lecturer
│   ├── student/                      # Student
│   ├── technical-officer/            # TO
│   ├── globals.css
│   └── layout.js
├── components/                       # Reusable components
│   ├── layouts/
│   └── ProtectedRoute.js
└── lib/                              # Utilities
    ├── api.js                        # API client
    └── auth.js                       # Auth utilities
```

#### Key Dependencies

```json
{
  "dependencies": {
    "next": "16.1.6",
    "react": "19.2.3",
    "react-dom": "19.2.3",
    "axios": "^1.13.6",
    "react-icons": "^5.6.0"
  }
}
```

#### Running Development Server

```bash
cd frontend
pnpm dev
# or
npm run dev
```

### Mobile App Development

#### Project Structure

```
app/src/
├── components/                       # UI components
│   └── UI.js
├── context/                          # React Context
│   └── AuthContext.js
├── lib/                              # Utilities
│   ├── api.js
│   └── theme.js
└── screens/                          # App screens
    ├── admin/
    ├── auth/
    ├── deptAdmin/
    ├── lecturer/
    ├── student/
    └── technicalOfficer/
```

#### Running Mobile App

```bash
cd app
npm start
# Then press 'w' for web, 'i' for iOS, 'a' for Android
```

---

## API Reference

### Base URL

```
Development: http://localhost:8080/api/v1
Production:  https://api.equiphub.edu.lk/api/v1
```

### Authentication

All endpoints (except `/auth/**`) require JWT Bearer token:

```http
Authorization: Bearer <jwt_token>
```

### API Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | Student registration |
| POST | `/auth/verify-email` | Verify email address |
| POST | `/auth/forgot-password` | Request password reset |
| POST | `/auth/reset-password` | Reset password |
| POST | `/auth/refresh-token` | Refresh access token |
| POST | `/auth/logout` | User logout |

#### Requests

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/requests/coursework` | Submit coursework request |
| POST | `/requests/research` | Submit research request |
| POST | `/requests/personal` | Submit personal request |
| POST | `/requests/extracurricular` | Submit extracurricular request |
| GET | `/requests` | List user requests |
| GET | `/requests/{id}` | Get request details |
| PUT | `/requests/{id}/withdraw` | Withdraw request |

#### Approvals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/approvals/pending` | Get pending approvals |
| PUT | `/requests/{id}/decide` | Approve/reject request |
| GET | `/approvals/history` | Approval history |

#### Equipment

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/equipment` | List equipment |
| GET | `/equipment/{id}` | Get equipment details |
| POST | `/equipment` | Add equipment (Admin) |
| PUT | `/equipment/{id}` | Update equipment |
| DELETE | `/equipment/{id}` | Delete equipment |
| GET | `/equipment/{id}/availability` | Check availability |

#### Technical Officer Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/to/pending-inspections` | Pending inspections |
| PUT | `/to/{id}/inspect-and-issue` | Inspect and issue |
| PUT | `/to/{id}/process-return` | Process return |
| GET | `/to/equipment/{id}/history` | Equipment history |

#### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | List all users |
| POST | `/admin/users` | Create user |
| PUT | `/admin/users/{id}` | Update user |
| DELETE | `/admin/users/{id}` | Delete user |
| GET | `/admin/departments` | List departments |
| POST | `/admin/departments` | Create department |
| GET | `/admin/analytics` | System analytics |

#### Departments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/departments` | List departments |
| GET | `/departments/{id}` | Get department |
| GET | `/departments/{id}/labs` | Get department labs |
| GET | `/departments/{id}/equipment` | Department equipment |

#### Courses

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/courses` | List courses |
| GET | `/courses/{id}` | Get course details |
| POST | `/courses` | Create course |
| PUT | `/courses/{id}` | Update course |

### Request/Response Examples

#### Login

**Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "student@unijaffna.edu.lk",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "email": "student@unijaffna.edu.lk",
    "fullName": "John Doe",
    "role": "STUDENT",
    "departmentId": 1
  }
}
```

#### Submit Coursework Request

**Request:**
```http
POST /api/v1/requests/coursework
Authorization: Bearer <token>
Content-Type: application/json

{
  "courseId": 5,
  "equipmentIds": [1, 2],
  "quantities": [1, 2],
  "requiredDate": "2026-03-20",
  "justification": "Lab assignment on SQL optimization"
}
```

**Response:**
```json
{
  "requestId": "REQ-2026-001",
  "status": "APPROVED",
  "autoApproved": true,
  "nextAction": "TO Inspection",
  "createdAt": "2026-03-17T10:00:00Z"
}
```

### Swagger Documentation

Access interactive API documentation at:

```
Development: http://localhost:8080/swagger-ui.html
Production:  https://api.equiphub.edu.lk/swagger-ui.html
```

### Error Responses

```json
{
  "timestamp": "2026-03-17T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Equipment not available",
  "path": "/api/v1/requests/coursework"
}
```

Common HTTP Status Codes:

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Testing

### Backend Testing

#### Test Structure

```
backend/api/src/test/java/com/equiphub/api/
└── EquipHubApplicationTests.java
```

#### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=EquipHubApplicationTests

# Run with coverage report
./mvnw test jacoco:report

# Skip tests during build
./mvnw clean package -DskipTests
```

#### Test Dependencies

```xml
<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

#### Test Configuration

Test profile configuration: `application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Frontend Testing

#### Running Tests

```bash
# Run linting
cd frontend
pnpm lint

# Run build to check for errors
pnpm build
```

### Integration Testing

#### Using Docker Compose

```bash
# Start test environment
cd backend/api
docker-compose up -d postgres redis

# Run integration tests
./mvnw verify

# Stop environment
docker-compose down
```

---

## Reference Details

### Database Schema

#### Core Tables

| Table | Description |
|-------|-------------|
| `users` | All system users |
| `departments` | Academic departments |
| `labs` | Laboratory rooms |
| `courses` | Academic courses |
| `equipment` | Equipment inventory |
| `equipment_categories` | Equipment categories |
| `requests` | Equipment requests |
| `request_items` | Items in requests |
| `request_approvals` | Approval records |
| `inspections` | Equipment inspections |
| `penalties` | Damage penalties |
| `audit_logs` | System audit trail |

#### Key Entities

**User Entity:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String fullName;
    private String email;
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @ManyToOne
    private Department department;
    
    private Boolean isActive;
    private Boolean isBanned;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
```

**Request Entity:**
```java
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String requestCode;
    
    @Enumerated(EnumType.STRING)
    private RequestType requestType;
    
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    
    @ManyToOne
    private User requester;
    
    @OneToMany(mappedBy = "request")
    private List<RequestItem> items;
    
    @OneToMany(mappedBy = "request")
    private List<RequestApproval> approvals;
}
```

### Configuration Reference

#### application.yml Full Reference

```yaml
spring:
  application:
    name: equiphub-api
  
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/equiphub_dev}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:12345678}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  jpa:
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:equiphub_redis_pass}
  
  session:
    store-type: redis
  
  mail:
    host: smtp.gmail.com
    port: 587

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api/v1

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  access-token-expiration: 86400000
  refresh-token-expiration: 604800000

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### Environment-Specific Configurations

| Profile | Database | Redis | Use Case |
|---------|----------|-------|----------|
| `dev` | localhost:5432 | localhost:6379 | Development |
| `test` | H2 In-Memory | Embedded | Testing |
| `prod` | Production DB | Production Redis | Production |

### Security Configuration

- **Authentication:** JWT-based with access/refresh tokens
- **Password:** BCrypt hashing
- **Session:** Redis-backed
- **CORS:** Configurable allowed origins
- **Rate Limiting:** Via Redis

### Monitoring & Health

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Metrics |
| `/api-docs` | OpenAPI JSON |
| `/swagger-ui.html` | API Documentation |

### Dependencies Version Reference

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.2.2 | Framework |
| Java | 21 | Language |
| PostgreSQL | 15 | Database |
| Redis | 7 | Cache/Session |
| JWT | 0.12.5 | Authentication |
| SpringDoc | 2.3.0 | API Documentation |
| Lombok | 1.18.36 | Code generation |
| MapStruct | 1.5.5.Final | DTO mapping |

### Troubleshooting

#### Common Issues

**Database Connection Failed:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Verify connection
psql -h localhost -U postgres -d equiphub_dev
```

**Redis Connection Failed:**
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli -h localhost -p 6379 ping
```

**Port Already in Use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Build Failures:**
```bash
# Clean and rebuild
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests
```

### License & Support

**Project:** EQuipHub - Equipment Request Management System  
**Organization:** University of Jaffna - Department of Computer Engineering & EEE  
**Version:** 3.8.0

For support and inquiries, contact the development team.

---

**End of Documentation**
