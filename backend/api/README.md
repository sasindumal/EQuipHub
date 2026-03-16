# EQuipHub Backend API

This is a Spring Boot 3.2 application that manages the backend services for EQuipHub. It relies on PostgreSQL for the database and Redis for caching and session management.

## Prerequisites

- **Java 21**
- **PostgreSQL 15**
- **Redis 7**

---

## How to Run the Application

There are two main ways to run this project: using Docker (the easiest way), or running the application locally on your machine.

### Option 1: Using Docker Compose (Recommended)
If you have Docker Desktop installed, you can spin up the application, database, and cache all at once:

```bash
cd backend/api
docker compose up -d
```
*Note: This will start the API on port `8080`, Postgres on `5432`, and Redis on `6379`.*

### Option 2: Running Locally (Development)
For local development with hot-reload via Spring DevTools, you can run the services using Docker and start the app natively.

**1. Start the Database and Cache:**
```bash
docker compose up -d postgres redis
```

**2. Start the Spring Boot App:**
```bash
./mvnw spring-boot:run
```

---

### Mac / Homebrew Specific Instructions

If you **do not have Docker installed** and have instead installed PostgreSQL and Redis directly via Homebrew (which creates a local `postgres` user matching your Mac username with no password by default), you will need to override the default database properties.

**1. Start the services via Homebrew:**
```bash
brew services start postgresql@15
brew services start redis
```

**2. Run the application with credential overrides:**
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./mvnw spring-boot:run -DskipTests \
  -Dspring-boot.run.arguments="--spring.datasource.username=$(whoami) --spring.datasource.password= --spring.data.redis.password="
```

---

## Accessing the API

Once the backend is running, you can interact with the API endpoints or view the documentation.

- **Base API URL:** `http://localhost:8080/api/v1`
- **Swagger Documentation:** [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
- **Health Check:** `http://localhost:8080/api/v1/actuator/health`

---

## 🔑 Demo Login Accounts

### Bootstrap Admin (always created)

This account is created on every startup by `DataInitializer`, regardless of profile:

| Role | Email | Password |
|---|---|---|
| **System Admin** | `admin@equiphub.test` | `Admin@1234` |

### Demo Accounts (dev/demo profile)

The following accounts are seeded automatically when the app runs with the **`dev`** or **`demo`** Spring profile (default is `dev`). All demo accounts share the same password:

> **Password for ALL demo accounts:** `Demo@1234`

#### System Admin

| Role | Name | Email |
|---|---|---|
| System Admin | System Admin | `sysadmin@eng.jfn.ac.lk` |

#### CSE Department — Computer Science & Engineering

| Role | Name | Email |
|---|---|---|
| Head of Department | Amal Perera | `hod.cse@eng.jfn.ac.lk` |
| Department Admin | Nimal Silva | `deptadmin.cse@eng.jfn.ac.lk` |
| Lecturer | Saman Jayawardena | `saman.lecturer@eng.jfn.ac.lk` |
| Lecturer | Kumari Fernando | `kumari.lecturer@eng.jfn.ac.lk` |
| Appointed Lecturer | Ruwan Bandara | `ruwan.aptlecturer@eng.jfn.ac.lk` |
| Instructor | Thilina Rajapaksa | `thilina.instructor@eng.jfn.ac.lk` |
| Technical Officer | Pradeep Gunawardena | `pradeep.to@eng.jfn.ac.lk` |
| Student (Sem 2) | Kasun Madushanka | `2022E001@eng.jfn.ac.lk` |
| Student (Sem 2) | Dilini Wickramasinghe | `2022E002@eng.jfn.ac.lk` |
| Student (Sem 3) | Hasini Karunarathna | `2021E003@eng.jfn.ac.lk` |

#### EEE Department — Electrical & Electronics Engineering

| Role | Name | Email |
|---|---|---|
| Head of Department | Chaminda Dissanayake | `hod.eee@eng.jfn.ac.lk` |
| Department Admin | Sunethra Rathnayake | `deptadmin.eee@eng.jfn.ac.lk` |
| Lecturer | Buddhika Samarasinghe | `buddhika.lecturer@eng.jfn.ac.lk` |
| Instructor | Chatura Liyanage | `chatura.instructor@eng.jfn.ac.lk` |
| Technical Officer | Manjula Weerasinghe | `manjula.to@eng.jfn.ac.lk` |
| Student (Sem 2) | Ishara Pathirana | `2022E101@eng.jfn.ac.lk` |
| Student (Sem 3) | Lasith Madushan | `2021E102@eng.jfn.ac.lk` |

> **Note:** Demo accounts have email verification pre-approved, so you can log in immediately without going through the verification flow.

### Quick Test by Role

| To test as… | Log in with | Password |
|---|---|---|
| System Admin | `sysadmin@eng.jfn.ac.lk` | `Demo@1234` |
| Department Admin | `deptadmin.cse@eng.jfn.ac.lk` | `Demo@1234` |
| Head of Department | `hod.cse@eng.jfn.ac.lk` | `Demo@1234` |
| Lecturer | `saman.lecturer@eng.jfn.ac.lk` | `Demo@1234` |
| Technical Officer | `pradeep.to@eng.jfn.ac.lk` | `Demo@1234` |
| Student | `2022E001@eng.jfn.ac.lk` | `Demo@1234` |

---

## Environment Configuration

The application reads configuration from `src/main/resources/application.yml`. If you need to override the database host or credentials safely, you can create a `.env` file in the `backend/api` directory:

```env
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/equiphub_dev
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

