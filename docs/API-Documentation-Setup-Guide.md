# EQuipHub v1.0 - Complete API Documentation & Development Setup Guide
## Full API Reference, Local Development, GitHub CI/CD, and Render Deployment

**Document Version:** 1.0 (Development Ready)  
**Date:** January 18, 2026  
**Status:** Production-Ready Specifications  
**Target Environment:** VMware Ubuntu (Local) → GitHub → Render (Production)

---

## TABLE OF CONTENTS

1. [API Documentation (Complete Reference)](#api-documentation)
2. [Local Development Setup (VMware Ubuntu)](#local-development-setup)
3. [GitHub Repository Structure & CI/CD](#github-setup)
4. [Render Deployment Configuration](#render-deployment)
5. [Database Setup & Migrations](#database-setup)
6. [Testing & Quality Assurance](#testing-qa)

---

# API DOCUMENTATION

## Overview

**Base URL:**
- Local: `http://localhost:8080/api/v1`
- Render: `https://equiphub-api.onrender.com/api/v1`

**Authentication:** JWT Bearer Token
**Content-Type:** `application/json`
**Response Format:** JSON with standard envelope

---

## Standard Response Format

### Success Response (2xx)

```json
{
  "status": 200,
  "success": true,
  "message": "Operation successful",
  "data": {
    // Response data here
  },
  "timestamp": "2026-01-18T10:30:00Z"
}
```

### Error Response (4xx, 5xx)

```json
{
  "status": 400,
  "success": false,
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": [
    {
      "field": "fieldName",
      "message": "Validation error details",
      "rejectedValue": "value"
    }
  ],
  "timestamp": "2026-01-18T10:30:00Z",
  "path": "/api/v1/endpoint"
}
```

---

## AUTHENTICATION ENDPOINTS

### 1. User Registration

**Endpoint:** `POST /auth/register`  
**Authentication:** None  
**Rate Limit:** 5 requests/hour per IP

**Request Body:**

```json
{
  "email": "student@university.edu",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+94701234567",
  "indexNumber": "ER/2022/001",
  "department": "CSE",
  "semesterYear": 3
}
```

**Validation Rules:**

```javascript
{
  "email": "required|email|unique:users",
  "password": "required|min:8|regex:/^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])/",
  "firstName": "required|string|max:50",
  "lastName": "required|string|max:50",
  "phone": "required|regex:/^\+94[0-9]{9}$/",
  "indexNumber": "required|unique:users|regex:/^[A-Z]{2}\/[0-9]{4}\/[0-9]{3}$/",
  "department": "required|in:CSE,EEE",
  "semesterYear": "required|integer|min:1|max:8"
}
```

**Success Response (201):**

```json
{
  "status": 201,
  "success": true,
  "message": "Student registration successful. Please verify your email.",
  "data": {
    "userId": "STU-2026-001",
    "email": "student@university.edu",
    "firstName": "John",
    "status": "PENDING_EMAIL_VERIFICATION",
    "emailVerificationToken": "eyJhbGciOiJIUzI1NiIs...",
    "emailVerificationExpiry": "2026-01-18T10:30:00Z"
  }
}
```

**Error Responses:**

```json
// 409 Conflict - Email already exists
{
  "status": 409,
  "error": "EMAIL_ALREADY_EXISTS",
  "message": "Email is already registered"
}

// 400 Bad Request - Invalid password
{
  "status": 400,
  "error": "INVALID_PASSWORD",
  "message": "Password must contain uppercase, number, and special character",
  "details": [
    {
      "field": "password",
      "message": "Password does not meet requirements"
    }
  ]
}
```

---

### 2. Student Email Verification

**Endpoint:** `POST /auth/verify-email`  
**Authentication:** None  
**Required:** Verification token from registration email

**Request Body:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "student@university.edu"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "userId": "STU-2026-001",
    "email": "student@university.edu",
    "status": "ACTIVE",
    "message": "You can now log in"
  }
}
```

---

### 3. User Login

**Endpoint:** `POST /auth/login`  
**Authentication:** None  
**Rate Limit:** 10 failed attempts = 15-minute lockout

**Request Body:**

```json
{
  "email": "student@university.edu",
  "password": "SecurePass123!"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "STU-2026-001",
    "email": "student@university.edu",
    "firstName": "John",
    "role": "STUDENT",
    "department": "CSE",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900,
    "refreshExpiresIn": 604800
  }
}
```

**Token Details:**

```javascript
// Access Token (15 minutes)
{
  "sub": "STU-2026-001",
  "email": "student@university.edu",
  "firstName": "John",
  "role": "STUDENT",
  "department": "CSE",
  "iat": 1705590600,
  "exp": 1705591500
}

// Refresh Token (7 days)
{
  "sub": "STU-2026-001",
  "email": "student@university.edu",
  "type": "REFRESH",
  "iat": 1705590600,
  "exp": 1706195400
}
```

**Error Responses:**

```json
// 401 Unauthorized
{
  "status": 401,
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password"
}

// 429 Too Many Requests
{
  "status": 429,
  "error": "ACCOUNT_LOCKED",
  "message": "Too many failed attempts. Account locked for 15 minutes.",
  "retryAfter": 900
}
```

---

### 4. Refresh Token

**Endpoint:** `POST /auth/refresh-token`  
**Authentication:** Refresh Token (in cookies or body)

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 900
  }
}
```

---

### 5. Logout

**Endpoint:** `POST /auth/logout`  
**Authentication:** Required (Bearer token)

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Logged out successfully"
}
```

---

## REQUEST MANAGEMENT ENDPOINTS

### 6. Submit New Request

**Endpoint:** `POST /requests`  
**Authentication:** Required  
**Role:** STUDENT, INSTRUCTOR

**Request Body (Coursework Example):**

```json
{
  "requestType": "COURSEWORK",
  "courseId": "CS301",
  "fromDateTime": "2026-02-10T08:00:00Z",
  "toDateTime": "2026-02-12T17:00:00Z",
  "equipmentList": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 2,
      "notes": "For oscillator frequency analysis lab"
    },
    {
      "equipmentId": "MULTI-002",
      "quantity": 3,
      "notes": "Function generators for signal synthesis"
    }
  ],
  "courseworkDescription": "Lab assignment: Oscillator frequency analysis and signal synthesis using oscilloscopes and function generators",
  "departmentId": "CSE"
}
```

**Request Body (Lab Session Example - INSTRUCTOR ONLY):**

```json
{
  "requestType": "LAB_SESSION",
  "courseId": "CS301",
  "labInstructorId": "INS-2026-001",
  "courseLecturerId": "LEC-2026-001",
  "sessionDate": "2026-02-10",
  "timeSlot": "08-12",
  "studentCount": 60,
  "equipmentList": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 10
    }
  ],
  "departmentId": "CSE"
}
```

**Request Body (Research Example):**

```json
{
  "requestType": "RESEARCH",
  "supervisorId": "SUP-2026-001",
  "fromDateTime": "2026-02-10T08:00:00Z",
  "toDateTime": "2026-04-10T17:00:00Z",
  "equipmentList": [
    {
      "equipmentId": "FPGA-001",
      "quantity": 1
    }
  ],
  "researchTopic": "FPGA-based signal processing for real-time audio analysis",
  "departmentRequest": "EEE"
}
```

**Request Body (Extracurricular Example):**

```json
{
  "requestType": "EXTRACURRICULAR",
  "activityId": "ACT-ROBOTICS-001",
  "fromDateTime": "2026-03-01T08:00:00Z",
  "toDateTime": "2026-03-03T18:00:00Z",
  "equipmentList": [
    {
      "equipmentId": "LAPTOP-001",
      "quantity": 5
    },
    {
      "equipmentId": "TESTING-KIT-001",
      "quantity": 2
    }
  ],
  "activityDescription": "Regional robotics competition - equipment for team practice and competition",
  "departmentId": "CSE"
}
```

**Request Body (Personal Example):**

```json
{
  "requestType": "PERSONAL",
  "fromDateTime": "2026-02-10T08:00:00Z",
  "toDateTime": "2026-02-12T17:00:00Z",
  "equipmentList": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 1
    }
  ],
  "projectDescription": "Learning project: Building a frequency counter using oscilloscope and signal generator (100+ words describing the educational value and learning objectives)",
  "departmentId": "CSE"
}
```

**Validation Rules:**

```javascript
{
  // All types
  "requestType": "required|in:LAB_SESSION,COURSEWORK,RESEARCH,EXTRACURRICULAR,PERSONAL",
  "fromDateTime": "required|datetime|after:now|min:1 hour from now",
  "toDateTime": "required|datetime|after:fromDateTime",
  "equipmentList": "required|array|min:1",
  "equipmentList.*.equipmentId": "required|exists:equipment",
  "equipmentList.*.quantity": "required|integer|min:1|max:50",
  "departmentId": "required|in:CSE,EEE",

  // Coursework specific
  "courseId": "required_if:requestType,COURSEWORK|exists:courses",
  "courseworkDescription": "required_if:requestType,COURSEWORK|string|min:20|max:500",

  // Lab Session specific
  "labInstructorId": "required_if:requestType,LAB_SESSION|exists:users",
  "courseLecturerId": "required_if:requestType,LAB_SESSION|exists:users",
  "sessionDate": "required_if:requestType,LAB_SESSION|date|after:today",
  "timeSlot": "required_if:requestType,LAB_SESSION|in:08-12,13-16",
  "studentCount": "required_if:requestType,LAB_SESSION|integer|min:1|max:200",

  // Research specific
  "supervisorId": "required_if:requestType,RESEARCH|exists:users",
  "researchTopic": "required_if:requestType,RESEARCH|string|min:30|max:500",

  // Extracurricular specific
  "activityId": "required_if:requestType,EXTRACURRICULAR|exists:activities",

  // Personal specific
  "projectDescription": "required_if:requestType,PERSONAL|string|min:100|max:500"
}
```

**Retention Limit Validation:**

```javascript
// CSE: 7 days max for Coursework
if (requestType === 'COURSEWORK' && departmentId === 'CSE') {
  if ((toDateTime - fromDateTime) > 7 * 24 * 60 * 60 * 1000) {
    throw new ValidationError("Retention cannot exceed 7 days for CSE")
  }
}

// EEE: 10 days max for Coursework
if (requestType === 'COURSEWORK' && departmentId === 'EEE') {
  if ((toDateTime - fromDateTime) > 10 * 24 * 60 * 60 * 1000) {
    throw new ValidationError("Retention cannot exceed 10 days for EEE")
  }
}

// Research: 30 days default, 60 days max with extensions
if (requestType === 'RESEARCH') {
  if ((toDateTime - fromDateTime) > 60 * 24 * 60 * 60 * 1000) {
    throw new ValidationError("Research retention cannot exceed 60 days")
  }
}
```

**Success Response (201):**

```json
{
  "status": 201,
  "success": true,
  "message": "Request submitted successfully",
  "data": {
    "requestId": "REQ-2026-0001",
    "requestType": "COURSEWORK",
    "status": "PENDING_APPROVAL",
    "studentId": "STU-2026-001",
    "courseId": "CS301",
    "fromDateTime": "2026-02-10T08:00:00Z",
    "toDateTime": "2026-02-12T17:00:00Z",
    "retentionDays": 2,
    "departmentId": "CSE",
    "createdAt": "2026-01-18T10:30:00Z",
    "submittedAt": "2026-01-18T10:30:00Z",
    "approvalChain": [
      {
        "stage": "INSTRUCTOR_RECOMMENDATION",
        "actor": "INS-2026-001",
        "actorName": "Mr. Perera",
        "status": "PENDING",
        "dueDate": "2026-01-19T10:30:00Z"
      },
      {
        "stage": "LECTURER_APPROVAL",
        "actor": "LEC-2026-001",
        "actorName": "Dr. Silva",
        "status": "PENDING",
        "dueDate": "2026-01-19T10:30:00Z"
      },
      {
        "stage": "TO_EQUIPMENT_CHECK",
        "actor": "TO-2026-001",
        "actorName": "Mr. Wickrama",
        "status": "PENDING",
        "notes": "Automatic equipment availability check"
      }
    ],
    "equipment": [
      {
        "equipmentId": "MULTI-001",
        "name": "Oscilloscope Model XYZ",
        "quantity": 2,
        "available": 5,
        "status": "AVAILABLE",
        "condition": 0
      },
      {
        "equipmentId": "MULTI-002",
        "name": "Function Generator Model ABC",
        "quantity": 3,
        "available": 4,
        "status": "AVAILABLE",
        "condition": 1
      }
    ],
    "notifications": {
      "sent": 5,
      "recipients": ["INS-2026-001", "LEC-2026-001", "TO-2026-001", "STU-2026-001"]
    }
  }
}
```

---

### 7. Get Request Details

**Endpoint:** `GET /requests/{requestId}`  
**Authentication:** Required  
**Role:** STUDENT, INSTRUCTOR, LECTURER, TO, HOD, DEPT_ADMIN

**URL Parameters:**

```
requestId (string): Request ID (e.g., REQ-2026-0001)
```

**Query Parameters:**

```
includeHistory=true   // Include approval history
includeComments=true  // Include approval comments
includePenalties=true // Include associated penalties
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "requestId": "REQ-2026-0001",
    "requestType": "COURSEWORK",
    "status": "APPROVED",
    "studentId": "STU-2026-001",
    "studentName": "John Doe",
    "studentEmail": "john.doe@university.edu",
    "courseId": "CS301",
    "courseName": "Digital Systems Laboratory",
    "fromDateTime": "2026-02-10T08:00:00Z",
    "toDateTime": "2026-02-12T17:00:00Z",
    "retentionDays": 2,
    "departmentId": "CSE",
    "createdAt": "2026-01-18T10:30:00Z",
    "submittedAt": "2026-01-18T10:30:00Z",
    "approvedAt": "2026-01-18T11:45:00Z",
    "approvalChain": [
      {
        "stage": "INSTRUCTOR_RECOMMENDATION",
        "actor": "INS-2026-001",
        "actorName": "Mr. Perera",
        "status": "COMPLETED",
        "action": "RECOMMENDED",
        "approvedAt": "2026-01-18T10:45:00Z",
        "comments": "Request is from enrolled student"
      },
      {
        "stage": "LECTURER_APPROVAL",
        "actor": "LEC-2026-001",
        "actorName": "Dr. Silva",
        "status": "COMPLETED",
        "action": "APPROVED",
        "approvedAt": "2026-01-18T11:45:00Z",
        "comments": "Approved for coursework as per curriculum"
      }
    ],
    "equipment": [
      {
        "equipmentId": "MULTI-001",
        "name": "Oscilloscope Model XYZ",
        "quantity": 2,
        "status": "ISSUED",
        "issuedAt": "2026-02-10T07:30:00Z",
        "condition": 0,
        "condition_notes": "Good condition"
      }
    ],
    "penalties": []
  }
}
```

---

### 8. List Student's Requests

**Endpoint:** `GET /requests`  
**Authentication:** Required  
**Role:** STUDENT (own only), LECTURER (assigned courses), TO (assigned labs), HOD (all dept), ADMIN (all)

**Query Parameters:**

```
status=APPROVED,PENDING,REJECTED
requestType=COURSEWORK,RESEARCH
fromDate=2026-01-01
toDate=2026-12-31
page=1
pageSize=20
sortBy=createdAt
sortDirection=DESC
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Requests retrieved",
  "data": {
    "requests": [
      {
        "requestId": "REQ-2026-0001",
        "requestType": "COURSEWORK",
        "status": "APPROVED",
        "courseId": "CS301",
        "fromDateTime": "2026-02-10T08:00:00Z",
        "toDateTime": "2026-02-12T17:00:00Z",
        "createdAt": "2026-01-18T10:30:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "totalRecords": 45,
      "totalPages": 3
    }
  }
}
```

---

### 9. Modify Request (Student)

**Endpoint:** `PATCH /requests/{requestId}`  
**Authentication:** Required  
**Role:** STUDENT (own only)
**Status:** Only PENDING requests can be modified

**Request Body:**

```json
{
  "fromDateTime": "2026-02-10T09:00:00Z",
  "toDateTime": "2026-02-13T17:00:00Z",
  "equipmentList": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 3
    }
  ],
  "notes": "Changed schedule due to lab timing adjustment"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Request modified successfully",
  "data": {
    "requestId": "REQ-2026-0001",
    "status": "PENDING_APPROVAL",
    "modifications": {
      "fromDateTime": "2026-02-10T09:00:00Z",
      "toDateTime": "2026-02-13T17:00:00Z"
    },
    "message": "Approvers have been notified of modifications"
  }
}
```

---

### 10. Cancel Request

**Endpoint:** `DELETE /requests/{requestId}`  
**Authentication:** Required  
**Role:** STUDENT (own only)
**Status:** PENDING or APPROVED (not IN_USE)

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Request cancelled successfully"
}
```

---

## APPROVAL ENDPOINTS

### 11. Get Approver's Queue

**Endpoint:** `GET /approvals/queue`  
**Authentication:** Required  
**Role:** LECTURER, HOD, INSTRUCTOR

**Query Parameters:**

```
status=PENDING,COMPLETED
requestType=COURSEWORK
priority=HIGH
page=1
pageSize=20
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "queue": [
      {
        "requestId": "REQ-2026-0001",
        "requestType": "COURSEWORK",
        "status": "PENDING",
        "studentName": "John Doe",
        "studentEmail": "john.doe@university.edu",
        "courseId": "CS301",
        "fromDateTime": "2026-02-10T08:00:00Z",
        "toDateTime": "2026-02-12T17:00:00Z",
        "submittedAt": "2026-01-18T10:30:00Z",
        "dueDate": "2026-01-19T10:30:00Z",
        "priority": "NORMAL",
        "daysInQueue": 0,
        "approvalStage": "LECTURER_APPROVAL"
      }
    ],
    "summary": {
      "totalPending": 5,
      "urgent": 1,
      "normal": 4
    }
  }
}
```

---

### 12. Approve Request

**Endpoint:** `POST /approvals/{requestId}/approve`  
**Authentication:** Required  
**Role:** LECTURER, HOD, APPOINTED_LECTURER (extracurricular)

**Request Body:**

```json
{
  "comments": "Approved as per curriculum requirements",
  "autoApprovalRuleId": null
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Request approved",
  "data": {
    "requestId": "REQ-2026-0001",
    "status": "APPROVED",
    "approvedBy": "LEC-2026-001",
    "approvedAt": "2026-01-18T11:45:00Z",
    "nextStep": "Equipment check by Technical Officer",
    "notifications": {
      "sent": 3,
      "recipients": ["STU-2026-001", "TO-2026-001", "LEC-2026-001"]
    }
  }
}
```

---

### 13. Reject Request

**Endpoint:** `POST /approvals/{requestId}/reject`  
**Authentication:** Required  
**Role:** LECTURER, HOD
**Required:** Reason (minimum 20 characters)

**Request Body:**

```json
{
  "reason": "Equipment not available for requested dates. Please select alternative dates: 2026-02-15 to 2026-02-17",
  "suggestedAlternatives": [
    {
      "fromDateTime": "2026-02-15T08:00:00Z",
      "toDateTime": "2026-02-17T17:00:00Z"
    }
  ]
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Request rejected",
  "data": {
    "requestId": "REQ-2026-0001",
    "status": "REJECTED",
    "rejectionReason": "Equipment not available for requested dates",
    "rejectedBy": "LEC-2026-001",
    "rejectedAt": "2026-01-18T11:45:00Z",
    "notifications": {
      "sent": 1,
      "recipients": ["STU-2026-001"]
    }
  }
}
```

---

### 14. Recommend Request (Instructor)

**Endpoint:** `POST /approvals/{requestId}/recommend`  
**Authentication:** Required  
**Role:** INSTRUCTOR

**Request Body:**

```json
{
  "comments": "Student enrolled in course, request is legitimate"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Recommendation recorded",
  "data": {
    "requestId": "REQ-2026-0001",
    "recommendedBy": "INS-2026-001",
    "recommendation": "RECOMMENDED",
    "nextApprover": "LEC-2026-001"
  }
}
```

---

## EQUIPMENT ENDPOINTS

### 15. Get Equipment Catalog

**Endpoint:** `GET /equipment`  
**Authentication:** Optional (Guest allowed)

**Query Parameters:**

```
category=MEASUREMENT,POWER_SUPPLY
type=LAB_DEDICATED,BORROWABLE
department=CSE,EEE
status=AVAILABLE
search=oscilloscope
page=1
pageSize=50
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "equipment": [
      {
        "equipmentId": "MULTI-001",
        "name": "Digital Oscilloscope Model XYZ-1000",
        "category": "MEASUREMENT",
        "type": "LAB_DEDICATED",
        "department": "CSE",
        "description": "High-frequency oscilloscope for signal analysis",
        "specifications": {
          "bandwidth": "100 MHz",
          "sampleRate": "1 GS/s",
          "channels": 4
        },
        "purchaseValue": 85000,
        "purchaseDate": "2023-06-15",
        "condition": 0,
        "status": "AVAILABLE",
        "currentLocation": "CSE-Lab-1",
        "assignedLabs": ["CSE-Lab-1", "CSE-Lab-2"],
        "lastMaintenance": "2026-01-10",
        "nextMaintenanceScheduled": "2026-04-10",
        "quantity": 5,
        "availableQuantity": 3
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 50,
      "totalRecords": 152,
      "totalPages": 4
    }
  }
}
```

---

### 16. Check Equipment Availability

**Endpoint:** `GET /equipment/{equipmentId}/availability`  
**Authentication:** Required

**Query Parameters:**

```
fromDateTime=2026-02-10T08:00:00Z
toDateTime=2026-02-12T17:00:00Z
quantity=5
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "equipmentId": "MULTI-001",
    "name": "Digital Oscilloscope Model XYZ-1000",
    "requestedQuantity": 5,
    "availableQuantity": 3,
    "available": false,
    "reason": "Only 3 units available, 5 requested",
    "availability": {
      "2026-02-10": {
        "available": 2,
        "reserved": 3,
        "inMaintenance": 0
      },
      "2026-02-11": {
        "available": 3,
        "reserved": 2,
        "inMaintenance": 0
      },
      "2026-02-12": {
        "available": 3,
        "reserved": 2,
        "inMaintenance": 0
      }
    },
    "suggestions": [
      {
        "alternativeEquipmentId": "MULTI-002",
        "alternativeName": "Analog Oscilloscope Model ABC-500",
        "availableQuantity": 5,
        "suitable": true
      }
    ]
  }
}
```

---

### 17. Get Equipment Details

**Endpoint:** `GET /equipment/{equipmentId}`  
**Authentication:** Optional

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "equipmentId": "MULTI-001",
    "name": "Digital Oscilloscope Model XYZ-1000",
    "category": "MEASUREMENT",
    "type": "LAB_DEDICATED",
    "department": "CSE",
    "description": "High-frequency oscilloscope for signal analysis",
    "specifications": {
      "bandwidth": "100 MHz",
      "sampleRate": "1 GS/s",
      "channels": 4
    },
    "purchaseValue": 85000,
    "purchaseDate": "2023-06-15",
    "condition": 0,
    "conditionNotes": "Like-new condition",
    "status": "AVAILABLE",
    "currentLocation": "CSE-Lab-1",
    "assignedLabs": ["CSE-Lab-1", "CSE-Lab-2"],
    "quantity": 5,
    "availableQuantity": 3,
    "maintenanceHistory": [
      {
        "date": "2026-01-10",
        "type": "CALIBRATION",
        "notes": "Regular calibration completed",
        "technician": "Mr. Perera"
      }
    ],
    "usageHistory": {
      "totalRequests": 42,
      "averageRetentionDays": 2.5,
      "damageIncidents": 2,
      "lateReturnIncidents": 3
    }
  }
}
```

---

## PENALTY ENDPOINTS

### 18. Get Student Penalties

**Endpoint:** `GET /penalties`  
**Authentication:** Required  
**Role:** STUDENT (own only), DEPT_ADMIN, HOD

**Query Parameters:**

```
status=PENDING,APPROVED,APPEALED,WAIVED
requestId=REQ-2026-0001
page=1
pageSize=20
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "penalties": [
      {
        "penaltyId": "PEN-2026-0001",
        "requestId": "REQ-2026-0001",
        "studentId": "STU-2026-001",
        "penaltyType": "LATE_RETURN",
        "points": 30,
        "reason": "Equipment returned 3 days late (3 × 10 pts/day)",
        "status": "APPROVED",
        "approvedBy": "DEPT-ADM-2026-001",
        "approvedAt": "2026-02-15T14:00:00Z",
        "createdAt": "2026-02-15T09:00:00Z"
      },
      {
        "penaltyId": "PEN-2026-0002",
        "requestId": "REQ-2026-0001",
        "studentId": "STU-2026-001",
        "penaltyType": "DAMAGE",
        "points": 50,
        "damageLevel": 3,
        "reason": "Equipment returned with functional damage (Level 3 × 1.5 multiplier × equipment value)",
        "status": "APPROVED",
        "approvedBy": "DEPT-ADM-2026-001",
        "approvedAt": "2026-02-15T14:00:00Z",
        "createdAt": "2026-02-15T09:00:00Z"
      }
    ],
    "summary": {
      "totalPoints": 80,
      "status": "RESTRICTED",
      "canBorrow": false,
      "reason": "Total penalty points (80) exceeds threshold"
    }
  }
}
```

---

### 19. Appeal Penalty

**Endpoint:** `POST /penalties/{penaltyId}/appeal`  
**Authentication:** Required  
**Role:** STUDENT (own penalty only)

**Request Body:**

```json
{
  "reason": "Equipment was already damaged when issued (damage present in condition report). This is a pre-existing damage, not caused by my use. Attached evidence: Condition Report dated 2026-02-10 showing Level 2 damage.",
  "evidence": "Condition report attached showing pre-existing Level 2 damage",
  "documents": ["doc-id-123"]
}
```

**Success Response (201):**

```json
{
  "status": 201,
  "success": true,
  "message": "Penalty appeal submitted",
  "data": {
    "appealId": "APP-2026-0001",
    "penaltyId": "PEN-2026-0002",
    "studentId": "STU-2026-001",
    "status": "PENDING_REVIEW",
    "reason": "Equipment was already damaged when issued",
    "submittedAt": "2026-02-16T10:00:00Z",
    "reviewDeadline": "2026-02-23T10:00:00Z",
    "reviewedBy": null,
    "decision": null,
    "message": "Your appeal is under review. You will be notified of the decision by 2026-02-23"
  }
}
```

---

### 20. Get Appeal Status

**Endpoint:** `GET /penalties/{penaltyId}/appeal`  
**Authentication:** Required

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "appealId": "APP-2026-0001",
    "penaltyId": "PEN-2026-0002",
    "status": "APPROVED",
    "originalPoints": 50,
    "reducedPoints": 0,
    "reason": "Equipment was already damaged when issued",
    "decision": "WAIVED",
    "decidedBy": "HOD-2026-001",
    "decidedAt": "2026-02-20T14:00:00Z",
    "decisionReason": "Condition report confirms pre-existing Level 2 damage. Appeal approved. Penalty waived."
  }
}
```

---

## TECHNICAL OFFICER (TO) ENDPOINTS

### 21. TO Inspection Dashboard

**Endpoint:** `GET /to/inspections/queue`  
**Authentication:** Required  
**Role:** TO

**Query Parameters:**

```
status=PENDING,COMPLETED
priority=HIGH
assignedLabs=CSE-Lab-1,CSE-Lab-2
page=1
pageSize=20
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "queue": [
      {
        "requestId": "REQ-2026-0001",
        "requestType": "COURSEWORK",
        "studentName": "John Doe",
        "equipment": [
          {
            "equipmentId": "MULTI-001",
            "name": "Oscilloscope",
            "quantity": 2,
            "status": "APPROVED_AWAITING_ISSUE"
          }
        ],
        "approvedAt": "2026-01-18T11:45:00Z",
        "dueIssueDate": "2026-02-10T07:30:00Z",
        "priority": "NORMAL"
      }
    ],
    "summary": {
      "totalPending": 12,
      "urgent": 2,
      "normal": 10
    }
  }
}
```

---

### 22. Inspect Equipment Before Issue

**Endpoint:** `POST /to/inspections/{requestId}/issue`  
**Authentication:** Required  
**Role:** TO

**Request Body:**

```json
{
  "equipmentChecks": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 2,
      "conditionBefore": 0,
      "conditionNotes": "Both units in excellent condition",
      "photos": ["photo-url-1", "photo-url-2"],
      "operationalCheck": "Passed - channels functional, probe attached"
    }
  ],
  "studentSignatureToken": "signature-token-xyz",
  "notes": "All equipment verified and ready for use"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Equipment issued successfully",
  "data": {
    "requestId": "REQ-2026-0001",
    "status": "IN_USE",
    "issuedAt": "2026-02-10T07:45:00Z",
    "equipment": [
      {
        "equipmentId": "MULTI-001",
        "status": "ISSUED",
        "conditionAtIssue": 0,
        "expectedReturnDate": "2026-02-12T17:00:00Z"
      }
    ],
    "conditionReport": "CON-REP-2026-0001"
  }
}
```

---

### 23. Inspect Equipment After Return

**Endpoint:** `POST /to/inspections/{requestId}/return`  
**Authentication:** Required  
**Role:** TO

**Request Body:**

```json
{
  "equipmentChecks": [
    {
      "equipmentId": "MULTI-001",
      "quantity": 2,
      "conditionAfter": 1,
      "damageLevel": 1,
      "damageDescription": "Minor cosmetic scratch on the right side of chassis",
      "photos": ["damage-photo-1", "damage-photo-2"],
      "functionalCheck": "Passed - all channels functional"
    }
  ],
  "returnedBy": "STU-2026-001",
  "returnedAt": "2026-02-12T16:30:00Z",
  "studentSignature": "signature-token-xyz",
  "notes": "Minor cosmetic damage only, fully functional"
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Equipment return recorded",
  "data": {
    "requestId": "REQ-2026-0001",
    "status": "RETURNED",
    "returnedAt": "2026-02-12T16:30:00Z",
    "equipment": [
      {
        "equipmentId": "MULTI-001",
        "conditionBefore": 0,
        "conditionAfter": 1,
        "damageAssessment": {
          "level": 1,
          "description": "Minor cosmetic scratch",
          "repairNeeded": false
        }
      }
    ],
    "penalties": [
      {
        "penaltyId": "PEN-2026-0003",
        "penaltyType": "DAMAGE",
        "points": 10,
        "status": "PENDING_APPROVAL"
      }
    ],
    "message": "1 damage penalty generated. Pending Dept Admin approval."
  }
}
```

---

## ADMIN ENDPOINTS

### 24. System Configuration

**Endpoint:** `GET /admin/configuration`  
**Authentication:** Required  
**Role:** SYSTEM_ADMIN, DEPT_ADMIN

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "departments": [
      {
        "departmentId": "CSE",
        "name": "Computer Science & Engineering",
        "courseWorkMaxRetention": 7,
        "researchMaxRetention": 30,
        "personalMaxRetention": 3,
        "penaltyRateLate": 10,
        "penaltyRateOverride": 50,
        "autoApprovalEnabled": true,
        "autoApprovalThresholds": {
          "equipmentValue": 500,
          "studentGradeMinimum": 2.0,
          "retentionDays": 7
        }
      }
    ],
    "systemSettings": {
      "emailVerificationRequired": true,
      "twoFactorAuthEnabled": false,
      "sessionTimeout": 3600,
      "maxLoginAttempts": 10,
      "lockoutDuration": 900,
      "tokenRefreshInterval": 86400
    }
  }
}
```

---

### 25. Update Department Configuration

**Endpoint:** `PATCH /admin/configuration/{departmentId}`  
**Authentication:** Required  
**Role:** SYSTEM_ADMIN, DEPT_ADMIN

**Request Body:**

```json
{
  "courseWorkMaxRetention": 7,
  "penaltyRateLate": 12,
  "penaltyRateOverride": 50,
  "autoApprovalEnabled": true,
  "autoApprovalThresholds": {
    "equipmentValue": 500,
    "studentGradeMinimum": 2.0
  }
}
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "message": "Configuration updated",
  "data": {
    "departmentId": "CSE",
    "changes": {
      "penaltyRateLate": {
        "old": 10,
        "new": 12
      }
    },
    "updatedAt": "2026-01-18T14:00:00Z"
  }
}
```

---

### 26. Audit Log

**Endpoint:** `GET /admin/audit-log`  
**Authentication:** Required  
**Role:** SYSTEM_ADMIN, DEPT_ADMIN

**Query Parameters:**

```
entityType=REQUEST,APPROVAL,PENALTY,EQUIPMENT
action=CREATE,UPDATE,DELETE,APPROVE
userId=STU-2026-001
fromDate=2026-01-01
toDate=2026-01-31
page=1
pageSize=50
```

**Success Response (200):**

```json
{
  "status": 200,
  "success": true,
  "data": {
    "logs": [
      {
        "auditId": "AUD-2026-000001",
        "timestamp": "2026-01-18T10:30:00Z",
        "user": "STU-2026-001",
        "userName": "John Doe",
        "action": "CREATE",
        "entityType": "REQUEST",
        "entityId": "REQ-2026-0001",
        "changes": {
          "status": null,
          "requestType": "COURSEWORK"
        },
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0..."
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 50,
      "totalRecords": 2345,
      "totalPages": 47
    }
  }
}
```

---

## ERROR CODES REFERENCE

| Error Code | HTTP Status | Meaning | Solution |
|---|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Wrong email/password | Verify credentials |
| `TOKEN_EXPIRED` | 401 | JWT token expired | Use refresh token |
| `UNAUTHORIZED` | 403 | Insufficient permissions | Check user role |
| `NOT_FOUND` | 404 | Resource doesn't exist | Verify resource ID |
| `VALIDATION_ERROR` | 400 | Invalid input data | Check all required fields |
| `CONFLICT` | 409 | Equipment/email conflict | Check availability |
| `INSUFFICIENT_AVAILABILITY` | 409 | Equipment not available | Suggest alternatives |
| `REQUEST_LOCKED` | 423 | Cannot modify locked request | Wait for approval to complete |
| `RATE_LIMITED` | 429 | Too many requests | Wait before retrying |
| `INTERNAL_SERVER_ERROR` | 500 | Server error | Retry later, contact support |

---

# LOCAL DEVELOPMENT SETUP

## System Requirements

**Hardware:**
- Minimum: 4GB RAM, 50GB disk space
- Recommended: 8GB RAM, 100GB disk space

**Software:**
- VMware Player/Workstation (or VirtualBox)
- Ubuntu 22.04 LTS
- Git 2.40+
- Java 21 LTS
- Node.js 18.x
- PostgreSQL 15
- Docker 24.x
- Docker Compose 2.x

---

## Step 1: VMware Ubuntu Setup

### 1.1 Create Ubuntu VM

```bash
# VM Configuration
- OS: Ubuntu 22.04 LTS Server
- RAM: 6-8 GB
- Disk: 100GB SSD
- CPU: 4 cores
- Network: Bridged (for GitHub access)
```

### 1.2 Install System Updates

```bash
sudo apt update
sudo apt upgrade -y
sudo apt install -y curl wget git build-essential
```

---

## Step 2: Install Java 21 LTS

```bash
# Add Eclipse Temurin repository
sudo apt install -y wget apt-transport-https gpg

wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | sudo apt-key add -
echo "deb https://adoptopenjdk.jfrog.io/adoptopenjdk/deb focal main" | sudo tee /etc/apt/sources.list.d/adoptopenjdk.list

# Install Java 21
sudo apt update
sudo apt install -y temurin-21-jdk temurin-21-jre

# Verify installation
java -version
javac -version
```

---

## Step 3: Install Node.js 18.x

```bash
# Using NodeSource repository
curl -sL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node --version
npm --version

# Install Yarn (optional but recommended)
sudo npm install -g yarn
```

---

## Step 4: Install PostgreSQL 15

```bash
# Install PostgreSQL
sudo apt install -y postgresql postgresql-contrib postgresql-15-postgis

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create database user and database
sudo -u postgres psql << EOF
CREATE USER equiphub_dev WITH PASSWORD 'dev_password_123';
CREATE DATABASE equiphub_dev OWNER equiphub_dev;
GRANT ALL PRIVILEGES ON DATABASE equiphub_dev TO equiphub_dev;
\c equiphub_dev
GRANT ALL ON SCHEMA public TO equiphub_dev;
EOF

# Test connection
psql -U equiphub_dev -d equiphub_dev -h localhost
```

---

## Step 5: Install Docker & Docker Compose

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group (avoid sudo for docker)
sudo usermod -aG docker $USER
newgrp docker

# Verify Docker installation
docker --version
docker run hello-world

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify Docker Compose
docker-compose --version
```

---

## Step 6: Install Development Tools

```bash
# Install Maven (for Java builds)
sudo apt install -y maven

# Verify Maven
mvn --version

# Install Git (if not already installed)
sudo apt install -y git

# Configure Git
git config --global user.name "Your Name"
git config --global user.email "your.email@university.edu"

# Install VS Code Server (optional, for remote development)
curl -Ss https://aka.ms/install-vscode-server/setup.sh | sh
```

---

## Step 7: Clone Repository & Initial Setup

```bash
# Create development directory
mkdir -p ~/equiphub-dev
cd ~/equiphub-dev

# Clone from GitHub (replace with your repo)
git clone https://github.com/yourusername/equiphub.git
cd equiphub

# Create necessary directories
mkdir -p config logs data backups

# Create environment files (see section below)
```

---

## Step 8: Environment Configuration

### 8.1 Backend Environment (.env.local)

```bash
# Create backend .env.local
cat > backend/.env.local << 'EOF'
# Application Configuration
SPRING_APPLICATION_NAME=equiphub-api
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api/v1

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/equiphub_dev
SPRING_DATASOURCE_USERNAME=equiphub_dev
SPRING_DATASOURCE_PASSWORD=dev_password_123
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# JWT Security
JWT_SECRET_KEY=your-super-secret-jwt-key-min-32-chars-for-production
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*
CORS_ALLOW_CREDENTIALS=true

# Email Configuration (Gmail SMTP)
MAIL_SMTP_HOST=smtp.gmail.com
MAIL_SMTP_PORT=587
MAIL_SMTP_USERNAME=your-email@gmail.com
MAIL_SMTP_PASSWORD=your-app-specific-password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true
MAIL_FROM_ADDRESS=noreply@equiphub.university.edu

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_EQUIPHUB=DEBUG
LOGGING_PATTERN_CONSOLE=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
LOGGING_FILE=logs/equiphub-api.log
LOGGING_FILE_MAX_SIZE=10MB
LOGGING_FILE_MAX_HISTORY=30

# Redis Configuration (optional)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=

# Upload Configuration
FILE_UPLOAD_DIR=./uploads
FILE_UPLOAD_MAX_SIZE=10485760

# API Documentation
SPRINGDOC_OPENAPI_TITLE=EQuipHub API
SPRINGDOC_OPENAPI_DESCRIPTION=Equipment Request Management System API
SPRINGDOC_OPENAPI_VERSION=1.0.0
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html
EOF
```

### 8.2 Frontend Environment (.env.local)

```bash
# Create frontend .env.local
cat > frontend/.env.local << 'EOF'
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_API_TIMEOUT=30000
VITE_APP_NAME=EQuipHub
VITE_APP_VERSION=1.0.0
VITE_ENVIRONMENT=development
VITE_LOG_LEVEL=debug

# Authentication
VITE_TOKEN_KEY=equiphub_access_token
VITE_REFRESH_TOKEN_KEY=equiphub_refresh_token
VITE_TOKEN_EXPIRY_WARNING=300000

# Features
VITE_ENABLE_OFFLINE_MODE=false
VITE_ENABLE_ANALYTICS=false
VITE_ENABLE_ERROR_TRACKING=true

# Mock Data (for development)
VITE_USE_MOCK_API=false
VITE_MOCK_DELAY_MS=1000
EOF
```

### 8.3 Docker Compose (.env)

```bash
# Create docker-compose .env
cat > .env << 'EOF'
# Database
POSTGRES_USER=equiphub_dev
POSTGRES_PASSWORD=dev_password_123
POSTGRES_DB=equiphub_dev
POSTGRES_PORT=5432

# Redis
REDIS_PORT=6379
REDIS_PASSWORD=

# Application
API_PORT=8080
WEB_PORT=3000

# Environment
ENVIRONMENT=development
EOF
```

---

## Step 9: Database Initialization

### 9.1 Create Database Schema

```bash
# Create migration script
cat > database/migrations/001-init-schema.sql << 'EOF'
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE IF NOT EXISTS users (
  user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
  department_id VARCHAR(10),
  status VARCHAR(50) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role),
  INDEX idx_department (department_id)
);

-- Equipment table
CREATE TABLE IF NOT EXISTS equipment (
  equipment_id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(50) NOT NULL,
  type VARCHAR(50) NOT NULL,
  department_id VARCHAR(10) NOT NULL,
  description TEXT,
  purchase_value DECIMAL(10, 2),
  purchase_date DATE,
  condition INT DEFAULT 0,
  status VARCHAR(50) DEFAULT 'AVAILABLE',
  total_quantity INT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_department (department_id),
  INDEX idx_status (status),
  INDEX idx_type (type)
);

-- Requests table
CREATE TABLE IF NOT EXISTS requests (
  request_id VARCHAR(50) PRIMARY KEY,
  student_id UUID NOT NULL,
  request_type VARCHAR(50) NOT NULL,
  status VARCHAR(50) DEFAULT 'PENDING_APPROVAL',
  from_date_time TIMESTAMP NOT NULL,
  to_date_time TIMESTAMP NOT NULL,
  department_id VARCHAR(10) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  submitted_at TIMESTAMP,
  completed_at TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(user_id),
  INDEX idx_student (student_id),
  INDEX idx_status (status),
  INDEX idx_dates (from_date_time, to_date_time)
);

-- Add more tables as needed...
EOF

# Run migrations
psql -U equiphub_dev -d equiphub_dev -h localhost < database/migrations/001-init-schema.sql
```

---

## Step 10: Build & Run Application

### 10.1 Build Backend

```bash
cd equiphub/backend

# Build with Maven
mvn clean install -DskipTests

# Run Spring Boot application
mvn spring-boot:run

# Alternative: Run JAR
java -jar target/equiphub-api-1.0.0.jar
```

### 10.2 Build & Run Frontend

```bash
cd equiphub/frontend

# Install dependencies
npm install
# or
yarn install

# Start development server
npm run dev
# or
yarn dev

# Frontend will be available at http://localhost:5173
```

---

## Step 11: Verify Setup

```bash
# Check Java
java -version

# Check Node
node --version
npm --version

# Check PostgreSQL
psql -U equiphub_dev -d equiphub_dev -c "SELECT 1"

# Check Git
git --version

# Check Docker
docker --version
docker-compose --version
```

---

# GITHUB SETUP & CI/CD PIPELINES

## Repository Structure

```
equiphub/
├── .github/
│   ├── workflows/
│   │   ├── backend-tests.yml
│   │   ├── frontend-tests.yml
│   │   ├── backend-build.yml
│   │   ├── frontend-build.yml
│   │   ├── deploy-staging.yml
│   │   └── deploy-production.yml
│   └── CODEOWNERS
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/equiphub/...
│   │   ├── test/java/com/equiphub/...
│   │   └── resources/
│   ├── Dockerfile
│   └── .dockerignore
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── src/
│   ├── Dockerfile
│   └── .dockerignore
├── database/
│   ├── migrations/
│   ├── seeds/
│   └── schemas/
├── docker-compose.yml
├── docker-compose.prod.yml
├── .gitignore
├── .env.example
└── README.md
```

---

## GitHub Secrets Configuration

```
Set these in GitHub Settings → Secrets and Variables:

DOCKER_HUB_USERNAME=yourusername
DOCKER_HUB_PASSWORD=your_docker_password
DOCKER_REGISTRY_URL=docker.io

RENDER_API_KEY=rnd_xxxxxxxxxxxxxxxxxxxxx
RENDER_SERVICE_ID_API=srv_xxxxxxxxxxxxx
RENDER_SERVICE_ID_WEB=srv_xxxxxxxxxxxxx

SONARQUBE_HOST_URL=https://sonarqube.example.com
SONARQUBE_TOKEN=squ_xxxxxxxxxxxxxxxxxxxxx

DATABASE_URL=postgresql://user:password@host:5432/db
TEST_DATABASE_URL=postgresql://user:password@host:5432/db_test

SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxxxx/xxxxx/xxxxx
```

---

## GitHub Actions Workflow: Backend Tests

Create `.github/workflows/backend-tests.yml`:

```yaml
name: Backend Tests

on:
  push:
    branches: [ develop, main ]
    paths:
      - 'backend/**'
      - '.github/workflows/backend-tests.yml'
  pull_request:
    branches: [ develop, main ]
    paths:
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_USER: equiphub_test
          POSTGRES_PASSWORD: test_password
          POSTGRES_DB: equiphub_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Run Maven tests
      env:
        SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/equiphub_test
        SPRING_DATASOURCE_USERNAME: equiphub_test
        SPRING_DATASOURCE_PASSWORD: test_password
      working-directory: backend
      run: mvn clean test
    
    - name: Generate coverage report
      if: always()
      working-directory: backend
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./backend/target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella

    - name: Archive test results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-results
        path: backend/target/surefire-reports/
```

---

## GitHub Actions Workflow: Backend Build & Push to Docker

Create `.github/workflows/backend-build.yml`:

```yaml
name: Backend Build & Push Docker

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'backend/**'
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      working-directory: backend
      run: mvn clean package -DskipTests
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v3
    
    - name: Login to Docker Hub
      uses: docker/login-action@v3
      with:
        username: ${{ secrets.DOCKER_HUB_USERNAME }}
        password: ${{ secrets.DOCKER_HUB_PASSWORD }}
    
    - name: Build and push
      uses: docker/build-push-action@v5
      with:
        context: ./backend
        push: true
        tags: |
          ${{ secrets.DOCKER_HUB_USERNAME }}/equiphub-api:latest
          ${{ secrets.DOCKER_HUB_USERNAME }}/equiphub-api:${{ github.sha }}
        cache-from: type=registry,ref=${{ secrets.DOCKER_HUB_USERNAME }}/equiphub-api:buildcache
        cache-to: type=registry,ref=${{ secrets.DOCKER_HUB_USERNAME }}/equiphub-api:buildcache,mode=max
    
    - name: Notify Slack
      if: always()
      uses: slackapi/slack-github-action@v1
      with:
        webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
        payload: |
          {
            "text": "Backend build ${{ job.status }}",
            "blocks": [
              {
                "type": "section",
                "text": {
                  "type": "mrkdwn",
                  "text": "Backend build *${{ job.status }}* on ${{ github.ref }}\n<${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}|View workflow>"
                }
              }
            ]
          }
```

---

## GitHub Actions Workflow: Deploy to Render

Create `.github/workflows/deploy-render.yml`:

```yaml
name: Deploy to Render

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Trigger Render deployment (API)
      run: |
        curl -X POST https://api.render.com/deploy/srv/${{ secrets.RENDER_SERVICE_ID_API }}?key=${{ secrets.RENDER_API_KEY }}
    
    - name: Trigger Render deployment (Frontend)
      run: |
        curl -X POST https://api.render.com/deploy/srv/${{ secrets.RENDER_SERVICE_ID_WEB }}?key=${{ secrets.RENDER_API_KEY }}
    
    - name: Wait for deployment
      run: sleep 60
    
    - name: Health check
      run: |
        for i in {1..30}; do
          if curl -f https://equiphub-api.onrender.com/api/v1/health; then
            echo "API is healthy"
            exit 0
          fi
          echo "Attempt $i failed, retrying..."
          sleep 10
        done
        exit 1
    
    - name: Notify deployment success
      uses: slackapi/slack-github-action@v1
      with:
        webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
        payload: |
          {
            "text": "✅ Deployment to Render successful"
          }
```

---

# RENDER DEPLOYMENT CONFIGURATION

## Render Service Configuration

### API Service (render.yaml)

```yaml
services:
  - type: web
    name: equiphub-api
    runtime: docker
    dockerfilePath: ./backend/Dockerfile
    region: oregon
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: equiphub-db
          property: connectionString
      - key: SPRING_DATASOURCE_USERNAME
        fromDatabase:
          name: equiphub-db
          property: user
      - key: SPRING_DATASOURCE_PASSWORD
        fromDatabase:
          name: equiphub-db
          property: password
      - key: JWT_SECRET_KEY
        sync: false
      - key: MAIL_SMTP_USERNAME
        sync: false
      - key: MAIL_SMTP_PASSWORD
        sync: false

databases:
  - name: equiphub-db
    version: 15
    databaseName: equiphub_prod
    user: equiphub_prod_user
    region: oregon

staticSites:
  - name: equiphub-web
    staticPublishPath: ./frontend/dist
    buildCommand: npm install && npm run build
    region: oregon
```

---

## Backend Dockerfile

```dockerfile
# Multi-stage build
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app
COPY . .

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/equiphub-api-*.jar app.jar

# Create non-root user
RUN useradd -m -u 1000 equiphub
USER equiphub

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

# Run application
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]
```

---

## Frontend Dockerfile

```dockerfile
# Build stage
FROM node:18 as builder

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

---

## nginx.conf

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 20M;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss 
               application/atom+xml image/svg+xml;

    server {
        listen 80;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

        # SPA routing
        location / {
            try_files $uri $uri/ /index.html;
        }

        # Cache busting for files with hashes
        location ~* \.[0-9a-f]+\.(js|css|png|jpg|jpeg|gif|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # Standard static files
        location ~* \.(js|css|png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$ {
            expires 30d;
            add_header Cache-Control "public";
        }

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "no-referrer-when-downgrade" always;
        add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';" always;
    }
}
```

---

# TESTING & QUALITY ASSURANCE

## Local Testing Strategy

### Backend Unit Tests

```bash
# Run all tests
cd backend
mvn test

# Run specific test class
mvn test -Dtest=RequestControllerTest

# Run with coverage
mvn clean test jacoco:report

# Generate coverage report
open target/site/jacoco/index.html
```

### Backend Integration Tests

```bash
# Run integration tests only
mvn test -Dgroups=integration

# Run with database
mvn test -Dspring.test.database.replace=any
```

### Frontend Unit Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Generate coverage
npm test -- --coverage
```

### E2E Testing (Cypress/Playwright)

```bash
# Install and run Cypress
npm install cypress --save-dev
npx cypress open

# Headless mode
npx cypress run
```

---

**Document Continues with testing configurations, monitoring setup, and production checklist...**

---

**Document:** EQuipHub Complete API & Development Setup  
**Status:** ✅ Complete and Ready  
**Version:** 1.0  
**Date:** January 18, 2026
