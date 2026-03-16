# EQuipHub — University Equipment Management System

<p align="center">
  <img src="https://img.shields.io/badge/React%20Native-0.81.5-61DAFB?style=for-the-badge&logo=react" alt="React Native">
  <img src="https://img.shields.io/badge/Expo-SDK%2054-000020?style=for-the-badge&logo=expo" alt="Expo SDK 54">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=for-the-badge&logo=spring" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Next.js-14-000000?style=for-the-badge&logo=next.js" alt="Next.js 14">
</p>

A comprehensive university equipment borrowing and management system with a **React Native mobile app**, **Spring Boot backend**, and **Next.js web frontend**. Designed for universities to manage equipment requests, approvals, inspections, and penalties across departments.

---

## 📋 Table of Contents

- [🏗️ Architecture](#🏗️-architecture)
- [📱 Mobile App](#📱-mobile-app)
- [⚙️ Backend API](#⚙️-backend-api)
- [🌐 Web Frontend](#🌐-web-frontend)
- [🚀 Quick Start](#🚀-quick-start)
- [👥 User Roles](#👥-user-roles)
- [🛠️ Tech Stack](#🛠️-tech-stack)
- [📁 Project Structure](#📁-project-structure)
- [🎨 Design System](#🎨-design-system)
- [🔌 API Documentation](#🔌-api-documentation)
- [🐛 Troubleshooting](#🐛-troubleshooting)
- [📦 Building for Production](#📦-building-for-production)

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        EQuipHub System                          │
├─────────────────┬─────────────────────┬─────────────────────────┤
│   Mobile App    │     Web Frontend    │      Backend API        │
│   (React Native)│     (Next.js)       │    (Spring Boot)        │
├─────────────────┼─────────────────────┼─────────────────────────┤
│  • Expo SDK 54  │  • Next.js 14       │  • Java 17              │
│  • React 19     │  • React 18         │  • Spring Boot 3.2      │
│  • Navigation 6 │  • Tailwind CSS     │  • Spring Security      │
│                 │                     │  • Spring Data JPA      │
└────────┬────────┴──────────┬──────────┴───────────┬─────────────┘
         │                   │                      │
         └───────────────────┴──────────────────────┘
                             │
                    ┌────────▼────────┐
                    │   PostgreSQL    │
                    │   (Database)    │
                    └─────────────────┘
```

### System Flow

1. **Students** browse equipment, create requests, and track status
2. **Lecturers** approve/reject requests from their students
3. **Technical Officers** issue equipment, process returns, conduct inspections
4. **Department Admins** manage department staff, equipment, and configurations
5. **System Admins** oversee the entire system and create departments

---

## 📱 Mobile App

A fully-featured React Native mobile application built with **Expo SDK 54**.

### Features

| Feature | Description |
|---------|-------------|
| **Authentication** | JWT-based login, registration, email verification with OTP |
| **Student Portal** | Equipment browsing, request creation, penalty tracking |
| **Dept Admin Portal** | Department management, staff oversight, request approval |
| **Technical Officer Portal** | Equipment issuing, return processing, inspections |
| **Lecturer Portal** | Request approval queue with approve/reject actions |
| **System Admin Portal** | System-wide analytics and user management |

### Screens

#### Authentication
- Login with email/password
- Student self-registration
- 6-digit OTP email verification

#### Student
- Dashboard with stats and quick actions
- Equipment catalog with search
- Request management (create, view, cancel)
- Penalty tracking and appeals

#### Department Admin
- Department dashboard and statistics
- Equipment management
- Request approval workflow
- Staff and student oversight

#### Technical Officer
- Issue equipment to approved requests
- Process returns with condition assessment
- Conduct inspections

#### Lecturer
- Approval queue for student requests
- Approve/reject with comments

---

## ⚙️ Backend API

A robust **Spring Boot 3.2** REST API with JWT authentication.

### Technology Stack

- **Java 17** with Spring Boot 3.2
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Redis** for caching and session management
- **Email Service** for notifications

### Key Modules

| Module | Purpose |
|--------|---------|
| `AuthController` | Login, registration, email verification |
| `UserManagementController` | User CRUD operations |
| `DepartmentController` | Department management |
| `EquipmentController` | Equipment CRUD and status updates |
| `RequestController` | Equipment request workflow |
| `ApprovalController` | Request approval process |
| `InspectionController` | Equipment issue/return/inspection |
| `PenaltyController` | Penalty management and appeals |
| `AdminController` | System administration |

### Database Schema

Key entities:
- **User** — Students, lecturers, admins with roles
- **Department** — Academic departments
- **Equipment** — Borrowable items with categories
- **Request** — Equipment borrowing requests
- **RequestItem** — Individual items in a request
- **RequestApproval** — Approval workflow tracking
- **Inspection** — Issue/return condition records
- **Penalty** — Violation penalties and appeals
- **AuditLog** — System activity tracking

---

## 🌐 Web Frontend

A modern **Next.js 14** web application with glassmorphism design.

### Features

- Responsive glassmorphism UI
- Role-based dashboards
- Equipment management
- Request approval workflows
- Analytics and reporting
- Department configuration

### Pages

| Role | Pages |
|------|-------|
| Student | Dashboard, Equipment, Requests, Penalties, History |
| Lecturer | Dashboard, Approvals |
| Technical Officer | Dashboard, Inspections, Issue, Returns |
| Department Admin | Dashboard, Equipment, Requests, Staff, Students, Penalties |
| System Admin | Dashboard, Users, Departments, Configuration |

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version |
|------|---------|
| Node.js | ≥ 18 |
| Java | 17 (JDK) |
| PostgreSQL | ≥ 14 |
| Redis | ≥ 6 |

### Step 1: Clone and Setup

```bash
git clone <repository-url>
cd EQuipHub
```

### Step 2: Start PostgreSQL & Redis

Using Docker:
```bash
docker run -d --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 postgres:14
docker run -d --name redis -p 6379:6379 redis:6
```

Or use local installations.

### Step 3: Start the Backend

```bash
cd backend/api

# Configure database connection in src/main/resources/application.yml
# Default: jdbc:postgresql://localhost:5432/equiphub

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Step 4: Start the Web Frontend

```bash
cd frontend
npm install
npm run dev
```

The web app will be available at `http://localhost:3000`

### Step 5: Start the Mobile App

```bash
cd app
npm install

# Configure API URL in .env
EXPO_PUBLIC_API_URL=http://localhost:8080/api/v1

# Start Expo
npx expo start
```

Scan the QR code with Expo Go on your phone, or press `i`/`a` to open in simulator.

---

## 👥 User Roles

| Role | Permissions |
|------|-------------|
| **STUDENT** | Browse equipment, create requests, view penalties |
| **LECTURER** | Approve/reject student requests |
| **APPOINTEDLECTURER** | Same as Lecturer |
| **INSTRUCTOR** | Same as Lecturer |
| **TECHNICALOFFICER** | Issue equipment, process returns, inspections |
| **DEPARTMENTADMIN** | Department management, staff oversight |
| **HEADOFDEPARTMENT** | Same as Dept Admin + department config |
| **SYSTEMADMIN** | System-wide admin, create departments |

---

## 🛠️ Tech Stack

### Mobile App
| Technology | Version |
|------------|---------|
| React Native | 0.81.5 |
| Expo SDK | 54 |
| React | 19.1.0 |
| React Navigation | 6.9.26 |
| Axios | 1.7.2 |
| Expo SecureStore | 15.0.8 |

### Backend
| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.x |
| Spring Security | 6.2.x |
| PostgreSQL | 14+ |
| Redis | 6+ |
| Maven | 3.9+ |

### Web Frontend
| Technology | Version |
|------------|---------|
| Next.js | 14.x |
| React | 18.x |
| Tailwind CSS | 3.x |
| TypeScript | 5.x |

---

## 📁 Project Structure

```
EQuipHub/
├── app/                          # React Native Mobile App (Expo)
│   ├── src/
│   │   ├── lib/
│   │   │   ├── api.js            # Axios client & API modules
│   │   │   └── theme.js          # Design tokens & glassmorphism
│   │   ├── context/
│   │   │   └── AuthContext.js    # Authentication state
│   │   ├── components/
│   │   │   └── UI.js             # Reusable UI components
│   │   └── screens/
│   │       ├── auth/             # Login, Register, Verify
│   │       ├── student/          # Student screens
│   │       ├── admin/            # Admin screens
│   │       ├── deptAdmin/        # Department admin screens
│   │       ├── technicalOfficer/ # TO screens
│   │       └── lecturer/         # Lecturer screens
│   ├── App.js                    # Navigation setup
│   └── package.json
│
├── backend/                      # Spring Boot API
│   └── api/
│       └── src/main/java/com/equiphub/api/
│           ├── config/           # Security, CORS, JWT config
│           ├── controller/       # REST endpoints
│           ├── model/            # JPA entities
│           ├── repository/       # Data access
│           ├── service/          # Business logic
│           └── security/         # Custom auth
│
├── frontend/                     # Next.js Web App
│   └── src/app/
│       ├── (auth)/               # Auth pages
│       ├── student/              # Student pages
│       ├── lecturer/             # Lecturer pages
│       ├── admin/                # Admin pages
│       └── components/           # Reusable components
│
└── docs/                         # Documentation
    ├── Architecture.md
    └── EqipHub_v3.3.md
```

---

## 🎨 Design System

### Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#3D52A0` | Buttons, active states, links |
| Primary Light | `#7091E6` | Highlights, gradients |
| Secondary | `#8697C4` | Secondary text, icons |
| Muted | `#ADBBDA` | Borders, placeholders |
| Background | `#EDE8F5` | Screen backgrounds |
| White | `#FFFFFF` | Cards, inputs |
| Black | `#000000` | Primary text |

### Glassmorphism Effects

The mobile app uses glassmorphism styling:
- Translucent cards with `rgba(255,255,255,0.6)` background
- Subtle borders with `rgba(255,255,255,0.5)`
- Soft shadows with primary color tint
- Gradient backgrounds on headers

---

## 🔌 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | Student registration |
| POST | `/auth/verify` | Email verification |
| POST | `/auth/refresh` | Token refresh |
| GET | `/auth/me` | Current user |

### Equipment Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/equipment` | List all equipment |
| GET | `/equipment/{id}` | Get equipment details |
| POST | `/equipment` | Create equipment (Admin) |
| PUT | `/equipment/{id}` | Update equipment |
| DELETE | `/equipment/{id}` | Delete equipment |

### Request Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/requests/my` | My requests |
| POST | `/requests` | Create request |
| PUT | `/requests/{id}/submit` | Submit request |
| PUT | `/requests/{id}/cancel` | Cancel request |

### Approval Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/approvals/queue` | Pending approvals |
| POST | `/approvals/{id}/approve` | Approve request |
| POST | `/approvals/{id}/reject` | Reject request |

---

## 🐛 Troubleshooting

### Backend Issues

**Database connection failed**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Verify connection in application.yml
```

**Redis connection refused**
```bash
# Check Redis is running
docker ps | grep redis

# Verify connection in application.yml
```

### Mobile App Issues

**Network Error on physical device**
- Ensure phone and computer on same WiFi
- Set `EXPO_PUBLIC_API_URL=http://<YOUR_IP>:8080/api/v1` in `.env`

**Metro bundler issues**
```bash
npx expo start --clear
```

### Web Frontend Issues

**Build errors**
```bash
npm run build
npm run dev -- --turbo
```

---

## 📦 Building for Production

### Mobile App (APK)

Using EAS Build:
```bash
cd app
npm install -g eas-cli
eas login
eas build --platform android --profile preview
```

### Backend (JAR)

```bash
cd backend/api
./mvnw clean package -DskipTests
java -jar target/equiphub-api-1.0.0.jar
```

### Web Frontend (Vercel)

```bash
cd frontend
vercel deploy --prod
```

---

## 📝 License

This project is part of the EQuipHub University Equipment Management System.

---

## 👨‍💻 Default Test Accounts

| Role | Email | Password |
|------|-------|----------|
| System Admin | admin@equiphub.test | Admin@1234 |
| Department Admin | deptadmin@equiphub.test | DeptAdmin@1234 |
| Lecturer | lecturer@equiphub.test | Lecturer@1234 |
| Technical Officer | tech@equiphub.test | TechOfficer@1234 |
| Student | student@equiphub.test | Student@1234 |

---

<p align="center">Built with ❤️ for university equipment management</p>
