# EqipHub v3.3 - Equipment Request Management System
## Complete Project Definition & Implementation Guide

**Project Name:** EqipHub  
**Document Version:** 3.3 (Final with Clarifications)  
**Status:** Ready for Team Meeting & Implementation  
**Last Updated:** January 3, 2026  
**Purpose:** Complete project specifications with finalized roles, workflows, and implementation details  
**Audience:** Development Team, Project Managers, Stakeholders, CSE & EEE Departments

---

## TABLE OF CONTENTS

1. [Executive Summary](#executive-summary)
2. [Version 3.3 Clarifications & Changes](#version-33-clarifications--changes)
3. [System Overview & Objectives](#system-overview--objectives)
4. [Department & Lab Structure](#department--lab-structure)
5. [User Roles & System Architecture](#user-roles--system-architecture)
6. [Admin vs Staff Separation (Critical)](#admin-vs-staff-separation-critical)
7. [User Registration & Account Management](#user-registration--account-management)
8. [Four Activity Types - Final Definitions](#four-activity-types---final-definitions)
9. [Equipment Categorization & Mappings](#equipment-categorization--mappings)
10. [Request Status Management](#request-status-management)
11. [Priority-Based Processing](#priority-based-processing)
12. [Complete Approval Workflows](#complete-approval-workflows)
13. [Fixed Request Procedure (No Admin Configuration)](#fixed-request-procedure-no-admin-configuration)
14. [Technical Officer (TO) Specifications](#technical-officer-to-specifications)
15. [Equipment Status Management](#equipment-status-management)
16. [Business Rules by Activity Type](#business-rules-by-activity-type)
17. [Database Schema - Final](#database-schema---final)
18. [API Endpoints - Complete List](#api-endpoints---complete-list)
19. [Implementation Roadmap](#implementation-roadmap)
20. [Deployment & Go-Live](#deployment--go-live)

---

## EXECUTIVE SUMMARY

### What is EqipHub v3.3?

**EqipHub** is a comprehensive web-based platform for managing equipment requests across the Department of Computer Engineering and Department of Electrical & Electronics Engineering at the University of Jaffna.

### Key Objectives

✅ Streamline equipment borrowing process  
✅ Track equipment lifecycle (available → borrowed → returned)  
✅ Manage equipment across multiple departments  
✅ Ensure equipment safety and maintenance  
✅ Reduce manual paperwork  
✅ Provide real-time status tracking  
✅ Support fixed request procedures without admin configuration  

### Version 3.3 Changes from v3.2

| Aspect | v3.2 | v3.3 |
|--------|------|------|
| **Department Details** | Estimated | **CSE: 3 TOs, 6 labs, detailed structure** |
| **TO Lab Assignment** | Generic | **Each TO manages 2 labs, 1 backup** |
| **User Types** | 8 types | **9 types (added Department Admin)** |
| **Admin vs Staff** | Mixed | **CLEAR SEPARATION - Admin handles CRUD, Staff handles workflows** |
| **Registration Flows** | Basic | **Detailed: Students self-register, Staff admin-created** |
| **Request Procedure** | Configurable | **FIXED CODE-BASED - No admin configuration needed** |
| **Admin Permissions** | General | **Detailed CRUD matrix per admin type** |
| **Role Descriptions** | Brief | **Comprehensive with daily workflows** |

---

## VERSION 3.3 CLARIFICATIONS & CHANGES

### Clarification 1: Department Structure (Real Data)

**COMPUTER ENGINEERING DEPARTMENT**
```
Technical Officers: 3 (TO-1, TO-2, TO-3)
Labs: 6 total

TO-1 (Primary Labs):
├─ Lab 1: Digital Systems Lab
├─ Lab 2: Analog Circuits Lab
└─ Backup: Lab 3 (during TO-2 absence)

TO-2 (Primary Labs):
├─ Lab 3: Microprocessor Lab
├─ Lab 4: Control Systems Lab
└─ Backup: Lab 1 (during TO-1 absence)

TO-3 (Primary Labs):
├─ Lab 5: Communication Systems Lab
├─ Lab 6: Power Systems Lab
└─ Backup: Lab 4 (during TO-2 absence)

Equipment Inventory: ~190 items
Total Value: ~1.4 Million LKR
Lab Instructors: 6 (one per lab)
```

**ELECTRICAL & ELECTRONICS ENGINEERING DEPARTMENT**
```
Technical Officers: 3+ (estimated)
Labs: 6+ (estimated - details to be confirmed)
Structure: Similar to CSE scaling
Equipment Value: Similar magnitude to CSE

NOTE: Detailed mapping to be confirmed in Phase 1
      System designed to scale for both departments
```

### Clarification 2: Three User Categories (CRITICAL)

```
┌──────────────────────────────────────────────────────────┐
│ EqipHub USER CATEGORIES                                  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│ ADMINISTRATORS (System Configuration & CRUD)             │
│ ├─ System Administrator                                  │
│ └─ Department Administrator                              │
│                                                          │
│ ACADEMIC STAFF (Request Workflows & Operations)          │
│ ├─ Head of Department                                    │
│ ├─ Lecturer (with multiple sub-roles)                    │
│ ├─ Lab Instructor                                        │
│ └─ Technical Officer                                     │
│                                                          │
│ STUDENTS & GUESTS (Requestors & Viewers)                 │
│ ├─ Student (Requestor)                                   │
│ └─ Guest (View-only access - optional)                   │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### Clarification 3: Admin vs Staff Separation (CRITICAL DISTINCTION)

#### **ADMINISTRATOR LAYER** - Configuration & CRUD Operations

**System Administrator**
```
Scope: Entire platform (all departments)

CRUD Operations:
✓ Create, Read, Update, Delete ALL departments
✓ Create, Read, Update, Delete ALL labs
✓ Assign HODs to departments
✓ Create system user accounts (staff & students)
✓ Manage system-wide settings & configurations
✓ Decommission equipment
✓ View system analytics

Cannot:
✗ Approve/reject requests (not their role)
✗ Participate in request workflows (no decision authority)
✗ Modify request procedures (fixed in code)
✗ Manage academic staff assignments (dept admin role)

Tools/Dashboards:
- System settings panel
- User account management
- Department & lab configuration
- System reports & analytics
```

**Department Administrator**
```
Scope: Single department (CSE or EEE)

CRUD Operations:
✓ Manage academic staff (CRUD on lecturers, TOs, lab instructors)
✓ Assign HODs to their department
✓ Create/update equipment for department
✓ Configure department-level labs
✓ Create student accounts (or approve self-registration)
✓ View department analytics

Cannot:
✗ Approve/reject requests (not their role)
✗ Participate in request workflows
✗ Access other departments
✗ Create System Admins
✗ Override request decisions

Tools/Dashboards:
- Staff management panel
- Equipment configuration
- Lab management
- Department analytics
```

#### **ACADEMIC STAFF LAYER** - Request Workflows & Operations

**Head of Department (HOD)**
```
Scope: Own department, all requests

Request Workflow Authority:
✓ APPROVE/REJECT PERSONAL requests (final gate)
✓ APPROVE/REJECT EXTRACURRICULAR requests
✓ APPROVE/REJECT RESEARCH requests (escalation only)
✓ View all department requests
✓ Oversee labs and equipment
✓ Assign course lecturers and lab instructors
✓ Change staff status (promotion, leave, etc.)
✓ Book equipment (HOD is also a lecturer)

Daily Activities:
- Review pending requests (approvals queue)
- Make approval/rejection decisions
- Manage department staff
- Oversee equipment allocation
- Monitor lab operations
```

**Lecturer (Multiple Sub-Roles)**
```
Primary Roles:
├─ Course Lecturer: Teaches specific courses
├─ Course Coordinator: Manages course curriculum
├─ Research Supervisor: Supervises research students
├─ Research Co-Supervisor: Secondary supervision role
├─ Senior Lecturer: Seniority designation (but same workflow)
└─ HOD: Head of Department (also a lecturer)

Request Workflow Authority (Course Lecturer):
✓ APPROVE/REJECT COURSEWORK requests (for assigned courses)
✓ RECOMMEND on RESEARCH requests
✓ RECOMMEND on PERSONAL requests
✓ Can book equipment for coursework (HOD can too)

Daily Activities:
- Approve/reject coursework requests
- Provide recommendations on other request types
- Track assigned course requests
- Monitor equipment for course labs
```

**Lab Instructor**
```
Scope: Assigned lab(s)

Request Workflow Authority:
✓ OBSERVE PERSONAL requests (safety check)
✓ RECOMMEND approve/delay/escalate
✗ Cannot approve (advisory only)
✗ Cannot reject (can escalate to HOD)

Daily Activities:
- Assess student safety & handling capability
- Observe equipment usage
- Report safety concerns
- Monitor lab conditions

Actions:
- RECOMMEND_APPROVE → continues to TO
- RECOMMEND_DELAY → request queued with timeline
- FLAG_SAFETY → escalates to HOD
```

**Technical Officer (TO)**
```
Scope: Assigned labs (2 primary + 1 backup per TO)

Request Workflow Authority:
✓ FINAL decision on equipment condition before issue
✓ REJECT equipment if condition unacceptable
✓ ISSUE equipment after approval
✓ RECEIVE equipment returns
✓ ASSESS return damage
✓ Calculate penalties

Daily Activities:
- Inspect approved requests (priority ordered queue)
- Issue equipment to requestors
- Receive equipment returns
- Assess equipment condition
- Report maintenance needs
- Update equipment status
- Generate inspection reports
- Manage equipment inventory
```

### Clarification 4: No Admin Configuration of Workflows (FIXED PROCEDURE)

```
KEY PRINCIPLE: Request workflows are FIXED in code-base
               Admins CANNOT customize or configure workflows

System Design:
─────────────

COURSEWORK → Auto-check (6 conditions) OR Lecturer Review
              → TO Inspection
              → ISSUE or DECLINE

RESEARCH → Route to Supervisor
           → Supervisor Approves/Rejects
           → TO Inspection
           → ISSUE or DECLINE

EXTRACURRICULAR → Route to HOD
                  → HOD Approves/Rejects
                  → TO Inspection
                  → ISSUE or DECLINE

PERSONAL → Route to Lecturer
           → Lecturer Recommends
           → Route to HOD
           → HOD Approves/Rejects
           → Route to Lab Instructor (if lab equipment)
           → Lab Instructor Recommends
           → Route to TO
           → TO Inspection
           → ISSUE or DECLINE

WHY NO ADMIN CONFIGURATION?
──────────────────────────

1. Consistency: Same rules across all users
2. Compliance: Fixed procedures ensure fairness
3. Clarity: Stakeholders know exact workflow
4. Simplicity: Admin role is simplified (only CRUD)
5. Security: No workflow manipulation possible
6. Maintainability: Changes require code updates (controlled)

Admin Role ONLY:
✓ CRUD on users, departments, labs, equipment
✓ View reports & analytics
✗ CANNOT modify request workflows
✗ CANNOT change approval requirements
✗ CANNOT skip approval stages
```

### Clarification 5: User Registration Flows

#### **Student Registration**

```
Flow:
─────

Step 1: Student accesses system → Registration page
Step 2: Student fills form:
        - Index number
        - Name (First & Last)
        - Email
        - Phone
        - Department
        - Semester/Year

Step 3: Student submits registration

Step 4: System Admin approval:
        - Admin receives notification
        - Verifies student data (against academic system)
        - APPROVES or REJECTS registration

Step 5: If approved:
        - Account created
        - Temporary password generated
        - Email sent: "Account activated, use password: {temp}"
        - Student must change password on first login

Step 6: If rejected:
        - Email sent with reason
        - Student can resubmit with corrections

Timeline: 2-24 hours for approval
```

#### **Staff Registration**

```
Flow:
─────

Step 1: Department Admin (or System Admin) initiates account creation
        - Admin goes to: Staff Management → Add New Staff
        - Admin selects: Role (Lecturer, TO, Lab Instructor, HOD)
        - Admin fills:
          - Name (First & Last)
          - Email
          - Phone
          - Department
          - Designation (e.g., "Senior Lecturer", "Technical Officer")
          - Lab assignments (if TO or Lab Instructor)

Step 2: Admin creates account
        - System generates username (e.g., lecturer_001)
        - System generates temporary password
        - Email sent to staff: "Account created, credentials: {username}/{temp_password}"

Step 3: Staff receives email
        - Must log in within 7 days (or account deactivates)
        - First login: MUST change password
        - Complete profile (optional fields)

Step 4: Account active
        - Staff can now use system
        - Access based on role/permissions

NOTE: Staff CANNOT self-register
      Accounts must be created by Dept Admin or System Admin
      Credentials are provided by admin
      This ensures proper vetting before staff access
```

#### **Guest Access (Optional)**

```
Flow:
─────

Step 1: Guest (no account) accesses system
Step 2: System shows: "Guest" or "View as Guest" option
Step 3: Guest can:
        - View equipment catalog (read-only)
        - View equipment availability status
        - View equipment specifications
        - View equipment usage statistics (department-wide)

Step 4: Guest CANNOT:
        - Submit requests
        - Access user data
        - Access request details
        - Modify anything

Optional: Guest registration
         - Guest can register to become student
         - Follows student registration flow above
```

### Clarification 6: Staff Cannot CRUD Students

```
KEY RULE: Only Admins can manage student accounts

Who CAN manage students:
✓ System Admin: Create/Read/Update/Delete any student
✓ Department Admin: Create/Read/Update/Delete students in their dept

Who CANNOT manage students:
✗ HOD: Cannot create/delete student accounts
✗ Lecturer: Cannot create/delete student accounts
✗ Lab Instructor: Cannot create/delete student accounts
✗ Technical Officer: Cannot create/delete student accounts

Staff Role with Students:
─────────────────────────
HOD: Can view students in their courses/labs
     Can change staff status (but not student status)
     
Lecturer: Can view students in their courses
          
Lab Instructor: Can view students using their lab

Technical Officer: Can view students requesting equipment
                   (no student account management)

WHY THIS SEPARATION?
────────────────────
1. Account safety: Prevents accidental student deletions
2. Audit trail: Central control ensures all changes logged
3. Academic integrity: Student data protected
4. Compliance: Clear responsibility for account management
```

---

## SYSTEM OVERVIEW & OBJECTIVES

### System Scope

**Who Uses EqipHub?**
```
ADMINISTRATORS:
├─ System Administrator (Full platform control)
└─ Department Administrator (Department control)

ACADEMIC STAFF:
├─ Head of Department (Approvals, oversight)
├─ Lecturer (Course approvals, recommendations)
├─ Lab Instructor (Safety observation)
└─ Technical Officer (Equipment inspection, issue/return)

STUDENTS & GUESTS:
├─ Student (Request equipment)
└─ Guest (View equipment catalog - optional)
```

**What Equipment is Managed?**
- All lab equipment in CSE and EEE departments
- High-value instruments (oscilloscopes, programmers, etc.)
- Consumable kits and components
- Software licenses
- Testing and measurement devices

**Key Processes Automated**
- Equipment request submission (4 types)
- Multi-level approval workflows (automated routing)
- Auto-approval for qualified coursework requests
- Status tracking and notifications
- Equipment inspection and issue
- Return and damage assessment
- Maintenance scheduling
- Penalty calculation for damage

---

## DEPARTMENT & LAB STRUCTURE

### Computer Engineering Department (CSE)

**Organization**
```
Head of Department: 1 (HOD-CSE)
Academic Staff: Multiple lecturers & supervisors
Technical Officers: 3
Lab Instructors: 6 (one per lab)
Students: ~500+ across 8 semesters

Labs: 6 total
├─ Lab 1: Digital Systems Lab
├─ Lab 2: Analog Circuits Lab
├─ Lab 3: Microprocessor Lab
├─ Lab 4: Control Systems Lab
├─ Lab 5: Communication Systems Lab
└─ Lab 6: Power Systems Lab
```

**Technical Officer Structure (CSE)**
```
TO-1 (Primary Labs: 1 & 2, Backup: 3)
├─ Lab 1: Digital Systems Lab
├─ Lab 2: Analog Circuits Lab
└─ Backup: Lab 3 (when TO-2 unavailable)

TO-2 (Primary Labs: 3 & 4, Backup: 1)
├─ Lab 3: Microprocessor Lab
├─ Lab 4: Control Systems Lab
└─ Backup: Lab 1 (when TO-1 unavailable)

TO-3 (Primary Labs: 5 & 6, Backup: 4)
├─ Lab 5: Communication Systems Lab
├─ Lab 6: Power Systems Lab
└─ Backup: Lab 4 (when TO-2 unavailable)

Rule: Each TO manages 2 primary labs + 1 backup
      Ensures coverage during absences
```

**Equipment Inventory (CSE)**
```
Total Equipment: ~190 items
Total Value: ~1.4 Million LKR

Distribution by Lab:
├─ Lab 1 (Digital Systems): ~32 items
├─ Lab 2 (Analog Circuits): ~28 items
├─ Lab 3 (Microprocessor): ~35 items
├─ Lab 4 (Control Systems): ~38 items
├─ Lab 5 (Communication): ~30 items
└─ Lab 6 (Power Systems): ~27 items

Equipment Categories:
├─ Measurement Instruments (oscilloscopes, multimeters, etc.)
├─ Power Supply Equipment (DC, AC, programmable)
├─ Development Tools (microcontroller boards, FPGA kits, programmers)
├─ Components & Consumables (resistors, capacitors, ICs, kits)
├─ Specialized Lab Equipment (PLCs, data acquisition, test beds)
├─ Computer Equipment (laptops, external drives, licenses)
└─ Network & Communication Equipment (analyzers, wireless test kits)
```

### Electrical & Electronics Engineering Department (EEE)

**Organization** (To be confirmed in Phase 1)
```
Expected Structure:
├─ Head of Department: 1
├─ Technical Officers: 3+
├─ Labs: 6+
└─ Similar scaling to CSE

Details to Confirm:
- Exact number of TOs
- Exact number of labs
- Lab names & specializations
- Equipment inventory
- Lab instructor assignments
```

---

## USER ROLES & SYSTEM ARCHITECTURE

### 9 User Types (FINAL)

```
┌──────────────────────────────────────────┐
│ EqipHub User Roles (9 Types)             │
├──────────────────────────────────────────┤
│                                          │
│ 1. System Administrator                  │
│ 2. Department Administrator              │
│ 3. Head of Department (HOD)              │
│ 4. Lecturer (Multiple sub-roles)         │
│ 5. Lab Instructor                        │
│ 6. Technical Officer (TO)                │
│ 7. Student                               │
│ 8. Guest (optional)                      │
│                                          │
└──────────────────────────────────────────┘

Type 1: SYSTEM ADMINISTRATOR
────────────────────────────
Scope: Entire platform
Login: system_admin@equipment.edu.lk

Permissions:
✓ CRUD all departments
✓ CRUD all labs
✓ CRUD all users (all roles)
✓ Assign HODs
✓ Create academic staff accounts
✓ View all requests (read-only)
✓ Generate system reports
✓ Configure system settings
✓ Manage backups/restore

Request Authority:
✗ Cannot approve/reject requests
✗ Cannot participate in workflows

Dashboard:
- System-wide analytics
- Department comparisons
- User management
- Equipment lifecycle overview
- System health monitoring


Type 2: DEPARTMENT ADMINISTRATOR
────────────────────────────────
Scope: Single department (CSE or EEE)
Login: dept_admin_cse@equipment.edu.lk

Permissions:
✓ CRUD academic staff in department
✓ CRUD equipment in department
✓ CRUD labs in department
✓ Assign/manage HOD
✓ Create student accounts (or approve registration)
✓ Create staff accounts (with System Admin)
✓ View department requests (read-only)
✓ Generate department reports

Cannot:
✗ Access other departments
✗ Create System Admins
✗ Approve/reject requests
✗ Participate in request workflows

Daily Tasks:
- Add new staff (lecturer, TO, lab instructor)
- Configure equipment assignments
- Manage lab information
- Review student registrations
- Generate reports for HOD


Type 3: HEAD OF DEPARTMENT (HOD)
────────────────────────────────
Scope: Own department, all request types
Login: hod_cse@equipment.edu.lk

Permissions:
✓ View all department requests
✓ APPROVE/REJECT PERSONAL requests (final authority)
✓ APPROVE/REJECT EXTRACURRICULAR requests
✓ APPROVE/REJECT RESEARCH requests (escalation only)
✓ Assign lecturers to courses
✓ Assign lab instructors to labs
✓ Assign TOs to labs
✓ Change staff status (leave, promotion, etc.)
✓ Book equipment for coursework (HOD is also lecturer)
✓ View department analytics

Cannot:
✗ Create/delete user accounts (Dept Admin role)
✗ CRUD equipment (Dept Admin role)
✗ Approve coursework (auto-approved or lecturer role)

Daily Activities:
- Review approval queue (Personal, Extracurricular, Research escalations)
- Make approval decisions
- Manage department staff assignments
- Oversee lab operations
- Monitor equipment allocation


Type 4: LECTURER
────────────────
Scope: Own courses + assigned labs
Login: lecturer_smith@equipment.edu.lk

Sub-roles (same system, different contexts):
├─ Course Lecturer: Teaches specific course
├─ Course Coordinator: Manages course curriculum
├─ Research Supervisor: Supervises research students
├─ Research Co-Supervisor: Secondary supervision
├─ Senior Lecturer: (designation, same workflow)
└─ HOD: (also a lecturer with HOD privileges)

Permissions:
✓ APPROVE/REJECT COURSEWORK requests (assigned courses)
✓ RECOMMEND on RESEARCH requests
✓ RECOMMEND on PERSONAL requests
✓ Book equipment for courses
✓ View requests related to assigned courses
✓ View students in assigned courses

Cannot:
✗ APPROVE research directly (only recommend)
✗ APPROVE personal directly (only recommend)
✗ Create/delete student accounts
✗ Create/delete staff accounts

Daily Activities:
- Review coursework approval queue (for assigned courses)
- Make approval/rejection decisions on coursework
- Provide recommendations on other request types
- Track equipment needs for courses
- Communicate with students about requests


Type 5: LAB INSTRUCTOR
──────────────────────
Scope: Assigned lab(s)
Login: labinstructor_lab1@equipment.edu.lk

Permissions:
✓ OBSERVE PERSONAL requests (safety assessment)
✓ RECOMMEND approve/delay/escalate
✓ View lab-related requests
✓ Report safety concerns
✓ Monitor equipment in lab
✓ Suggest maintenance

Cannot:
✗ APPROVE requests (advisory only)
✗ REJECT requests (can escalate)
✗ Create/delete accounts
✗ CRUD equipment

Daily Activities:
- Assess student capability for PERSONAL requests
- Observe equipment handling in labs
- Report safety issues
- Recommend approval or delays
- Escalate safety concerns to HOD


Type 6: TECHNICAL OFFICER (TO)
──────────────────────────────
Scope: Assigned labs (2 primary + 1 backup)
Login: to_cse_1@equipment.edu.lk

Permissions:
✓ FINAL decision on equipment condition
✓ INSPECT approved requests
✓ ISSUE equipment to requestors
✓ RECEIVE equipment returns
✓ REJECT requests if equipment condition unacceptable
✓ ASSESS damage on return
✓ Update equipment status
✓ Generate inspection reports
✓ Document maintenance needs

Cannot:
✗ Approve requests (TO inspects after approval)
✗ Skip TO inspection (even if all approvers agree)
✗ Create/delete accounts
✗ CRUD equipment (Dept Admin role)

Daily Activities:
- Review inspection queue (priority ordered)
- Inspect equipment for approved requests
- Issue equipment with receipts
- Receive equipment returns
- Assess return condition
- Report damage & maintenance
- Update equipment inventory status
- Manage lab opening/closing


Type 7: STUDENT
───────────────
Scope: Own requests
Login: student_2023e001@equipment.edu.lk

Permissions:
✓ Submit COURSEWORK requests
✓ Submit RESEARCH requests (if supervisor assigned)
✓ Submit PERSONAL requests
✓ Submit EXTRACURRICULAR requests
✓ View own requests & status
✓ Track issued equipment (due dates, condition)
✓ Withdraw pending requests
✓ Return equipment via system
✓ View own request history

Cannot:
✗ Request other student's equipment
✗ View other student's requests
✗ Approve any requests
✗ Modify submitted requests (must withdraw & resubmit)

Daily Activities:
- Submit equipment requests
- Track request status
- Check due dates for issued equipment
- Return equipment
- View request history


Type 8: GUEST (Optional)
────────────────────────
Scope: Equipment catalog only
Login: Not required (anonymous browsing)

Permissions:
✓ View equipment catalog (read-only)
✓ View equipment availability
✓ View equipment specifications
✓ View availability statistics

Cannot:
✗ Submit requests
✗ Access user data
✗ Access request details
✗ Modify anything

Can become: Student by registering
```

---

## ADMIN vs STAFF SEPARATION (CRITICAL)

### Clear Role Boundaries

#### **ADMINISTRATOR RESPONSIBILITIES** (CRUD & Configuration)

| Task | System Admin | Dept Admin | HOD | Lecturer | Staff |
|------|-------------|-----------|-----|----------|-------|
| Create departments | ✅ YES | ❌ NO | ❌ NO | ❌ NO | ❌ NO |
| Create labs | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Add/edit equipment | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Assign HODs | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Create lecturer accounts | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Create student accounts | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Create TO accounts | ✅ YES | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Approve coursework | ❌ NO | ❌ NO | ❌ NO | ✅ YES | ❌ NO |
| Approve personal | ❌ NO | ❌ NO | ✅ YES | ❌ NO | ❌ NO |
| Approve research | ❌ NO | ❌ NO | ⚠️ ESC | ❌ NO | ✅ SUPER |
| Inspect equipment | ❌ NO | ❌ NO | ❌ NO | ❌ NO | ✅ TO |
| Modify workflows | ❌ NO | ❌ NO | ❌ NO | ❌ NO | ❌ NO |

#### **STAFF RESPONSIBILITIES** (Request Workflows & Operations)

| Task | HOD | Lecturer | Lab Instr | TO |
|------|-----|----------|-----------|-----|
| Approve coursework | ❌ NO | ✅ YES | ❌ NO | ❌ NO |
| Approve personal | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Approve research | ⚠️ ESC | ❌ NO | ❌ NO | ❌ NO |
| Recommend research | ❌ NO | ✅ YES | ❌ NO | ❌ NO |
| Recommend personal | ❌ NO | ✅ YES | ✅ YES | ❌ NO |
| Inspect equipment | ❌ NO | ❌ NO | ❌ NO | ✅ YES |
| Issue equipment | ❌ NO | ❌ NO | ❌ NO | ✅ YES |
| Receive returns | ❌ NO | ❌ NO | ❌ NO | ✅ YES |
| Assign staff | ✅ YES | ❌ NO | ❌ NO | ❌ NO |
| Book equipment | ✅ YES | ✅ YES | ❌ NO | ❌ NO |

**Legend:**
- ✅ YES = Has authority
- ❌ NO = Does not have authority
- ⚠️ ESC = Can act during escalation only
- ✅ SUPER = Supervisor approval (RESEARCH only)

### The Key Principle

```
ADMINS: Build & Configure the system
        - Create users (staff & students)
        - Create departments & labs
        - Configure equipment
        - Manage system settings
        - VIEW reports & analytics
        - CANNOT participate in request approval

STAFF: Operate the system workflows
       - Make request approval decisions
       - Inspect & issue equipment
       - Provide recommendations
       - CANNOT create/delete accounts
       - CANNOT modify workflows
       - CANNOT CRUD equipment

This separation ensures:
✓ Clear responsibility boundaries
✓ Better security (admins can't approve requests)
✓ Fair decision-making (not influenced by system admins)
✓ Scalability (easy to add admins without affecting workflows)
✓ Compliance (roles are distinct and documented)
```

---

## USER REGISTRATION & ACCOUNT MANAGEMENT

### Student Registration Flow

**Online Self-Registration**
```
1. Student visits EqipHub login page
   ↓
2. Clicks "Register" / "Create Account"
   ↓
3. Registration form with fields:
   ├─ Index number (e.g., 2023/E/001)
   ├─ Full name (First & Last)
   ├─ Email address
   ├─ Phone number
   ├─ Department (CSE / EEE)
   ├─ Current semester
   ├─ Agree to terms ☐
   └─ Submit
   ↓
4. System validation:
   ├─ Check index number exists in academic system
   ├─ Check email format valid
   ├─ Check no duplicate registration
   └─ Store as PENDING_APPROVAL
   ↓
5. Confirmation email to student:
   "Your registration is pending approval.
    We'll notify you when it's activated."
   ↓
6. System Admin notified:
   "New student registration pending: {name}"
   ↓
7. Admin reviews:
   ├─ Verify student data against academic records
   ├─ Check for duplicates
   ├─ APPROVE or REJECT
   ↓
8A. If APPROVED:
    - Account created
    - Status = ACTIVE
    - Temporary password generated (e.g., Temp@2026)
    - Email: "Account activated! Password: Temp@2026"
    - Student MUST change on first login
    ↓
8B. If REJECTED:
    - Email: "Registration not approved. Reason: {reason}"
    - Can resubmit corrected form
    ↓
9. Student logs in:
   - Username: index number (2023/E/001)
   - Password: temporary password
   ↓
10. First login:
    - System forces password change
    - Student sets permanent password
    - Account now fully active
    ↓
11. Ready to submit requests!

Timeline: 2-24 hours for approval
```

### Staff Account Creation Flow

**Admin-Initiated Account Creation**
```
1. Department Admin goes to:
   Dashboard → Staff Management → Add New Staff
   ↓
2. Admin selects role:
   ├─ Lecturer
   ├─ Technical Officer
   ├─ Lab Instructor
   └─ Head of Department
   ↓
3. Admin fills form:
   ├─ First name
   ├─ Last name
   ├─ Email address
   ├─ Phone number
   ├─ Designation (e.g., "Senior Lecturer")
   ├─ Department
   ├─ Lab assignments (if TO or Lab Instructor)
   ├─ Course assignments (if Lecturer)
   └─ Submit
   ↓
4. System processing:
   ├─ Generates username (e.g., lecturer_001)
   ├─ Generates temporary password (12-char random)
   ├─ Creates account with status = PENDING_FIRST_LOGIN
   ├─ Sends email to staff:
      "Your account has been created
       Username: {username}
       Password: {temp_password}
       You MUST log in within 7 days"
   └─ Creates audit log entry
   ↓
5. Staff receives email:
   ├─ Notes credentials
   ├─ Logs in within 7 days (or account expires)
   └─ First login action → change password
   ↓
6. First login process:
   ├─ System prompts: "Change your password"
   ├─ Staff sets new permanent password
   ├─ Account status → ACTIVE
   ├─ Can now use all system features
   └─ Email confirmation: "Password changed, account active"
   ↓
7. Staff fully active!
   - Can approve/reject requests
   - Can inspect equipment
   - Can book equipment
   - Full access to their role features

Expiration: If staff doesn't log in within 7 days
            Account status → INACTIVE
            Admin must reactivate with new password

Dept Admin Cannot Create:
✗ System Admins (only System Admin can)
✗ Department Admins (only System Admin can)

System Admin Can Create:
✓ All user types including System Admins
✓ Department Admins for other departments
```

### Password Reset

**For Forgotten Passwords**
```
Staff/Student forgets password:
  ↓
1. Click "Forgot Password" on login page
2. Enter email address
3. System sends reset link (valid 1 hour)
4. Staff/Student clicks link
5. Sets new password (8+ chars, must include number & symbol)
6. Login with new password

Security:
- Reset link valid only 1 hour
- Links are single-use only
- Resetting password logs out all sessions
- Audit log recorded
```

### Guest Access

**Optional - View Only**
```
Visitor wants to view equipment catalog:
  ↓
1. Visit system
2. Prompted: Login or Browse as Guest
3. Click "Browse as Guest"
   ↓
4. Can view:
   ✓ Equipment catalog
   ✓ Availability status (AVAILABLE / BORROWED / MAINTENANCE)
   ✓ Equipment specifications
   ✓ Department equipment statistics
   ✓ Lab information
   
5. Cannot view:
   ✗ Request details
   ✗ Student information
   ✗ Staff information
   ✗ Equipment usage history
   
6. To submit requests:
   - Must register as student
   - Follows student registration flow above
```

---

## FOUR ACTIVITY TYPES - FINAL DEFINITIONS

### Type 1: COURSEWORK (1st Priority - Highest Urgency)

**Purpose:** Equipment for classroom/lab delivery of courses

**Auto-Approval Conditions (ALL 6 must be true):**
1. Equipment status = AVAILABLE or BOOKINGS
2. Course code is valid & active in current semester
3. Lecturer is assigned to course
4. Quantity requested ≤ 10 items per request
5. Lecturer total ≤ 15 items per semester
6. Equipment not in MAINTENANCE

**Approval Chain:**
```
Student Submits (with Course Code)
    ↓
Auto-Check System (6 conditions)
    ├─ ALL conditions met? → AUTO-APPROVED (skip lecturer)
    └─ ANY condition fails? → Routes to Lecturer Review
                ↓
Lecturer Reviews & Approves/Rejects
    ↓
Technical Officer Inspects & Issues/Rejects
```

### Type 2: RESEARCH (2nd Priority - High Urgency - v3.2 Changed)

**Purpose:** Equipment for final year projects, independent study, research

**Supervisor Requirement:** Must have assigned supervisor (faculty member)

**Approval Chain (SIMPLIFIED v3.3):**
```
Researcher Submits (with Supervisor ID)
    ↓
System Routes to Assigned Supervisor
    ↓
Supervisor Reviews & Approves/Rejects
    ├─ Supervisor approves → Status = APPROVED
    └─ Supervisor rejects → Status = DECLINED (FINAL)
                ↓
Technical Officer Inspects & Issues/Rejects
```

**Key Change:** Research NOW requires ONLY Supervisor Approval (NO HOD layer)

### Type 3: EXTRACURRICULAR (3rd Priority - Medium Urgency)

**Purpose:** Equipment for clubs, events, tech fest, exhibitions

**HOD Approval Required:** Yes

**Approval Chain:**
```
Event Organizer Submits
    ↓
HOD Reviews (may consider lecturer input)
    ├─ HOD approves → Status = APPROVED
    └─ HOD rejects → Status = DECLINED
                ↓
Technical Officer Inspects & Issues/Rejects
```

### Type 4: PERSONAL (4th Priority - Low Urgency)

**Purpose:** Individual student projects, freelance work, internship projects

**Multi-Gate Approval:** Yes (Lecturer → HOD → Lab Instructor → TO)

**Approval Chain:**
```
Student Submits (with Lecturer ID)
    ↓
Lecturer Reviews & Recommends (REQUIRED)
    ├─ Positive → continues to HOD
    └─ Negative → DECLINED (FINAL)
                ↓
HOD Reviews & Approves/Rejects
    ├─ Approves → continues
    └─ Rejects → DECLINED
                ↓
Lab Instructor Observes (if lab equipment)
    ├─ Approves → continues to TO
    ├─ Recommends delay → queued
    └─ Flags safety → escalates to HOD
                ↓
Technical Officer Inspects & Issues/Rejects
```

---

## EQUIPMENT CATEGORIZATION & MAPPINGS

### Equipment Categories by Type

**CATEGORY A: Measurement & Testing Instruments**
```
Equipment Type: MEASUREMENT_INSTRUMENTS
Suitable For: COURSEWORK, RESEARCH
Examples: Oscilloscopes, Multimeters, Spectrum Analyzers, Power Meters
Maintenance: Quarterly calibration required
Safety: High voltage awareness needed
Labs: Digital Systems Lab, Analog Circuits Lab
Assigned TOs: TO-1, TO-2
```

**CATEGORY B: Power & Supply Equipment**
```
Equipment Type: POWER_SUPPLY
Suitable For: COURSEWORK, RESEARCH, PERSONAL
Examples: DC Power Supplies, AC Power Supplies, Programmable Supplies
Maintenance: Monthly inspection
Safety: Electrical hazard awareness
Assigned TOs: Multiple TOs
```

**CATEGORY C: Development & Programming Tools**
```
Equipment Type: DEVELOPMENT_TOOLS
Suitable For: COURSEWORK, RESEARCH, PERSONAL
Examples: Microcontroller Dev Boards, FPGA Dev Kits, Programmers
Maintenance: Weekly cleanliness check
Safety: Low safety risk
Assigned TOs: TO-2, TO-1
```

**CATEGORY D: Passive Components & Consumables**
```
Equipment Type: COMPONENTS
Suitable For: COURSEWORK, RESEARCH, PERSONAL
Examples: Component Kits, ICs, Connectors, Breadboards, Jumper Wires
Maintenance: No regular maintenance (consumable)
Safety: Low safety risk
Assigned TOs: All TOs
```

**CATEGORY E: Specialized Lab Equipment**
```
Equipment Type: SPECIALIZED_LAB_EQUIPMENT
Suitable For: COURSEWORK, RESEARCH
Examples: PLC Units, Data Acquisition, Sensor Kits, Control Test Beds
Maintenance: Quarterly servicing
Safety: Medium safety risk (mechanical, electrical)
Assigned TOs: TO-2, TO-3
```

**CATEGORY F: Computer & Software Equipment**
```
Equipment Type: COMPUTER_EQUIPMENT
Suitable For: COURSEWORK, RESEARCH, PERSONAL
Examples: Laptops, Desktop Computers, Tablets, Software Licenses
Maintenance: Monthly data backup check
Safety: Data security concern
Assigned TOs: TO-1
```

**CATEGORY G: Network & Communication Equipment**
```
Equipment Type: NETWORK_EQUIPMENT
Suitable For: COURSEWORK, RESEARCH
Examples: Network Analyzers, Wireless Test Equipment, RF Equipment
Maintenance: Quarterly inspection
Safety: RF safety awareness (high frequency)
Assigned TOs: TO-1, TO-2
```

---

## REQUEST STATUS MANAGEMENT

### Status Lifecycle

```
PENDING (Initial)
    ↓
[System decision - Auto-check or Route to Approver]
    ↓
IN_REVIEW_* (Waiting for specific approver)
    ├─ IN_REVIEW_LECTURER
    ├─ IN_REVIEW_SUPERVISOR
    ├─ IN_REVIEW_HOD
    ├─ IN_REVIEW_LAB_INSTRUCTOR
    └─ IN_REVIEW_TO
    ↓
APPROVED (All approvals granted, awaiting TO inspection)
    ↓
ISSUED (Equipment given to user)
    ↓
RETURNED (Equipment returned, process complete)
    
OR at any stage:
    ↓
DECLINED (Rejected by approver)
```

### 6 Status Types

| Status | Meaning | Next Action |
|--------|---------|-------------|
| **PENDING** | Submitted, awaiting review | Auto-check or route to approver |
| **IN_REVIEW_*** | Waiting for specific approver | Approver decision |
| **APPROVED** | Ready for TO inspection | TO inspection |
| **ISSUED** | Equipment given to user | User uses, then returns |
| **RETURNED** | Process complete | Archived |
| **DECLINED** | Rejected | Can resubmit new request |

---

## PRIORITY-BASED PROCESSING

### Queue Processing Order (Daily)

```
PRIORITY 1: COURSEWORK Requests
├─ Auto-approved coursework (process immediately)
├─ Coursework awaiting lecturer review (oldest first)
└─ Coursework ready for TO inspection

PRIORITY 2: RESEARCH Requests
├─ Research awaiting supervisor review
└─ Research ready for TO inspection

PRIORITY 3: EXTRACURRICULAR Requests
├─ Events with HOD review pending
└─ Events ready for TO inspection

PRIORITY 4: PERSONAL Requests
├─ All gates combined (lowest priority)
```

### SLA (Service Level Agreements)

| Request Type | Auto-Approve | Approver Review | TO Inspection | Total |
|--------------|------------|-----------------|---------------|-------|
| **COURSEWORK** | <30 min | 4-8 hrs | 12-24 hrs | <1 day |
| **RESEARCH** | N/A | 24-48 hrs | 12-24 hrs | 2-3 days |
| **EXTRACURRICULAR** | N/A | 24-48 hrs | 12-24 hrs | 2-5 days |
| **PERSONAL** | N/A | 3-5 days (multi) | 12-24 hrs | 3-5 days |

---

## COMPLETE APPROVAL WORKFLOWS

### Workflow 1: COURSEWORK (Auto-Approved Path)

```
Student Submits COURSEWORK Request
├─ Course: CS301 - Database Systems
├─ Equipment: Oscilloscope (Qty: 1)
└─ Lecturer: Dr. Smith
        ↓
SYSTEM AUTO-CHECK (All 6 conditions)
├─ Equipment available? ✓ YES
├─ Course valid? ✓ YES
├─ Lecturer assigned? ✓ YES
├─ Quantity ≤ 10? ✓ YES
├─ Semester total ≤ 15? ✓ YES (Dr. Smith has 8 items)
└─ Not in maintenance? ✓ YES
        ↓
Status: APPROVED (Auto-Approved)
Timeline: ~2 hours
        ↓
TO NOTIFICATION
├─ Request in TO-1 queue
├─ Equipment: Oscilloscope #OSC-001
└─ Need by: {required_date}
        ↓
TO INSPECTION (12-24 hours)
├─ Locate equipment ✓
├─ Visual check ✓
├─ Functional test ✓
└─ Ready for issue
        ↓
Status: ISSUED
├─ Equipment given to student
├─ Issue receipt generated
└─ Return date set: Semester end
        ↓
STUDENT RETURNS EQUIPMENT
├─ Equipment returned to Lab
├─ TO inspects return condition
├─ No damage detected ✓
└─ Equipment status = AVAILABLE
        ↓
Status: RETURNED ✓

Timeline: <1 day total (if TO available)
```

### Workflow 2: RESEARCH (v3.3)

```
Researcher Submits RESEARCH Request
├─ Project: "ML Hardware Acceleration"
├─ Supervisor: Dr. Johnson
├─ Equipment: GPU Server (Qty: 1)
└─ Duration: 4 months
        ↓
ROUTE TO SUPERVISOR
├─ System finds Dr. Johnson's queue
└─ Supervisor notified
        ↓
Status: IN_REVIEW_SUPERVISOR
SLA: 24-48 hours
        ↓
SUPERVISOR REVIEW (Dr. Johnson)
├─ Is research legitimate? ✓ YES
├─ Equipment necessary? ✓ YES
└─ Decision: APPROVE
        ↓
Status: APPROVED
└─ Routes to TO immediately
        ↓
TO INSPECTION (24-48 hours)
├─ Locate equipment ✓
├─ Condition check ✓
└─ Ready for handover
        ↓
Status: ISSUED
├─ Extended loan agreement
├─ Return date: {project_end + 2 days}
└─ Researcher briefed on care
        ↓
RESEARCHER RETURNS EQUIPMENT
├─ Condition: EXCELLENT ✓
└─ Return inspection pass
        ↓
Status: RETURNED ✓

Timeline: 2-3 days approval, 4-month use

v3.3: NO HOD layer, simplified from v3.2
```

### Workflow 3: EXTRACURRICULAR

```
Event Organizer Submits
├─ Event: Tech Fest 2026
├─ HOD: Prof. Khan
└─ Equipment: Oscilloscopes, Power Supplies
        ↓
Status: IN_REVIEW_HOD
SLA: 24-48 hours
        ↓
HOD REVIEW
├─ Is event valid? ✓ YES
├─ Equipment necessary? ✓ YES
└─ Decision: APPROVE
        ↓
Status: APPROVED
└─ Routes to TO
        ↓
TO INSPECTION & ISSUE
[Same as other types]
        ↓
Status: RETURNED ✓
```

### Workflow 4: PERSONAL (Multi-Gate)

```
Student Submits PERSONAL Request
├─ Project: "IoT Home Automation"
├─ Lecturer: Dr. Wilson
└─ Equipment: Microcontroller Kit
        ↓
GATE 1: LECTURER RECOMMENDATION
├─ Project legitimate? ✓ YES
├─ Student capable? ✓ YES
└─ Decision: RECOMMEND_YES
        ↓
Status: IN_REVIEW_HOD
        ↓
GATE 2: HOD APPROVAL
├─ Lecturer recommends YES ✓
├─ HOD reviews and decides
└─ Decision: APPROVE
        ↓
Status: IN_REVIEW_LAB_INSTRUCTOR
        ↓
GATE 3: LAB INSTRUCTOR OBSERVATION
├─ Student handling skills? ✓ GOOD
├─ Safety awareness? ✓ ADEQUATE
└─ Decision: RECOMMEND_APPROVE
        ↓
Status: IN_REVIEW_TO
        ↓
GATE 4: TO INSPECTION
├─ Equipment condition? ✓ GOOD
└─ Ready for issue
        ↓
Status: ISSUED
        ↓
STUDENT RETURNS EQUIPMENT
├─ Condition: EXCELLENT ✓
        ↓
Status: RETURNED ✓

Timeline: 3-5 days approval, 2-month use
```

---

## FIXED REQUEST PROCEDURE (No Admin Configuration)

### Why Fixed Procedures?

```
CORE PRINCIPLE: Request workflows are HARD-CODED
                Admins CANNOT customize or configure workflows

Reasons:
1. Consistency: Same rules apply to all users
2. Fairness: No favoritism possible
3. Compliance: University policies enforced
4. Simplicity: Admin role is only CRUD
5. Security: No workflow manipulation possible
6. Maintainability: Changes require code review

Admin Dashboard will NOT have:
✗ "Configure request approval chain"
✗ "Set approval stages for COURSEWORK"
✗ "Customize auto-approval conditions"
✗ "Change priority rules"
✗ "Modify SLA timelines"

All workflow logic is in code:
✓ Auto-approval conditions (hardcoded)
✓ Approval routing (hardcoded)
✓ Priority processing (hardcoded)
✓ Status transitions (hardcoded)
```

### Built-In Workflows (Fixed)

```
COURSEWORK → Auto-check OR Lecturer → TO Inspection
RESEARCH → Supervisor → TO Inspection
EXTRACURRICULAR → HOD → TO Inspection
PERSONAL → Lecturer → HOD → Lab Instr → TO Inspection

Changes to workflows:
- Require code review
- Require System Admin approval
- Documented in version control
- Not configurable via UI
```

---

## TECHNICAL OFFICER (TO) SPECIFICATIONS

### TO Lab Assignment (CSE Example)

**TO-1 Assignment**
```
Primary Labs: 2
├─ Lab 1: Digital Systems Lab
└─ Lab 2: Analog Circuits Lab

Backup Lab: 1
└─ Lab 3: Microprocessor Lab (when TO-2 unavailable)

Total Equipment Responsibility:
├─ Lab 1: ~32 items
├─ Lab 2: ~28 items
├─ Lab 3: ~35 items (backup only)
└─ Total: ~95 items (60 primary, 35 backup)
```

**TO-2 Assignment**
```
Primary Labs: 2
├─ Lab 3: Microprocessor Lab
└─ Lab 4: Control Systems Lab

Backup Lab: 1
└─ Lab 1: Digital Systems Lab (when TO-1 unavailable)

Total Equipment Responsibility:
├─ Lab 3: ~35 items
├─ Lab 4: ~38 items
├─ Lab 1: ~32 items (backup only)
└─ Total: ~105 items (73 primary, 32 backup)
```

**TO-3 Assignment**
```
Primary Labs: 2
├─ Lab 5: Communication Systems Lab
└─ Lab 6: Power Systems Lab

Backup Lab: 1
└─ Lab 4: Control Systems Lab (when TO-2 unavailable)

Total Equipment Responsibility:
├─ Lab 5: ~30 items
├─ Lab 6: ~27 items
├─ Lab 4: ~38 items (backup only)
└─ Total: ~95 items (57 primary, 38 backup)
```

### Daily TO Workflow

**Morning (08:00 AM)**
```
1. Arrive at lab
2. Unlock labs (TO responsible for 2 labs + 1 backup)
3. Visual inspection of all equipment
4. Check status of yesterday's issues
5. Update equipment availability status
6. Prepare inspection checklist
7. Review queue of pending requests
   (sorted by priority & timestamp)
```

**During Day (08:30 AM - 05:00 PM)**
```
1. Process inspection queue
   ├─ Review next request in queue
   ├─ Locate equipment
   ├─ Perform inspection (15-45 min depending on complexity)
   ├─ Document findings
   ├─ Issue equipment OR reject with reason code
   └─ Update status in system

2. Handle equipment returns
   ├─ Receive returned equipment
   ├─ Inspect return condition
   ├─ Assess for damage
   ├─ Update equipment status
   └─ Process penalties if needed

3. Maintenance tasks
   ├─ Perform routine maintenance
   ├─ Schedule equipment for calibration
   ├─ Document maintenance logs
   └─ Update next service dates

4. Respond to urgent requests
   ├─ Priority flagged requests get immediate attention
   ├─ Expedite inspection if possible
   └─ Communicate with students/approvers
```

**Evening (05:00 PM)**
```
1. End-of-day equipment check
2. Lock labs
3. Update daily report
4. Prepare queue for next day
5. Document any issues/maintenance needs
```

### TO Inspection Checklist (Complete)

```
STEP 1: PRE-INSPECTION (2 min)
☐ Verify equipment exists & serial number matches
☐ Check if equipment currently in use (wait if needed)
☐ Note ambient conditions

STEP 2: PHYSICAL INSPECTION (8 min)
☐ Visual damage assessment (cracks, dents, burns)
☐ Check all cables (no exposed wires, secure)
☐ Check for corrosion or oxidation
☐ Verify all parts present (lids, straps, stands)
☐ Cleanliness check (dust, spills, residue)
☐ Check power cord condition
☐ Take photos if issues found

STEP 3: FUNCTIONAL TESTING (10-20 min)
[Varies by equipment type]
☐ Power on test
☐ Basic function test
☐ Safety test
☐ Document any functional issues

STEP 4: CALIBRATION CHECK (2-5 min)
☐ For precision instruments only
☐ Check calibration expiration
☐ Verify calibration certificate present
☐ If overdue: Mark OUT OF SERVICE

STEP 5: FINAL ASSESSMENT (2 min)
☐ Overall condition rating (Excellent/Good/Fair/Poor)
☐ Ready for issue? YES / NO
☐ Take overall equipment photo

RESULT:
═════════════════════════════════════════
PASS ✓ - Equipment approved for issue
  Status = ISSUED
  Issue receipt generated

FAIL ✗ - Equipment cannot be issued
  Status = DECLINED (TO_REJECTED)
  Reason code documented
  Photos attached
  Requestor + Approver notified
═════════════════════════════════════════

Average Time per Equipment: 20-45 minutes
```

---

## EQUIPMENT STATUS MANAGEMENT

### 6 Equipment Status Types

```
AVAILABLE: Ready for use, no reservations
BOOKINGS: Reserved for approved requests (temporarily held)
BORROWED: Currently checked out to user
MAINTENANCE: Under repair or scheduled servicing
DAMAGED: Damaged but repairable
UNAVAILABLE: Out of service indefinitely (decommissioned)
```

### Status Transitions

```
AVAILABLE ──→ BOOKINGS ──→ BORROWED ──→ AVAILABLE
                                         (returned)

AVAILABLE ──→ MAINTENANCE ──→ AVAILABLE
                             (repaired)

BORROWED ──→ DAMAGED ──→ MAINTENANCE ──→ AVAILABLE
           (on return)              (repaired)

ANY STATUS ──→ UNAVAILABLE
              (end of life)
```

---

## BUSINESS RULES BY ACTIVITY TYPE

### COURSEWORK Business Rules

**CR1: Course Information Required**
```
WHEN: COURSEWORK request submitted
THEN: MUST include:
  - Course Code (e.g., "CS301")
  - Course Name (e.g., "Database Systems")
  - Semester/Year
  
VALIDATION:
  - Course must exist in academic system
  - Lecturer must be assigned to course
  - Course must be active in current semester
```

**CR2: Auto-Approval Logic (All 6 conditions)**
```
IF Equipment AVAILABLE/BOOKINGS AND
   Course valid AND
   Lecturer assigned AND
   Quantity ≤ 10 AND
   Semester total ≤ 15 AND
   Equipment not in MAINTENANCE
THEN: AUTO-APPROVED (skip lecturer review)
```

**CR3: Quantity Constraints**
```
Per Single Request: Max 10 items
Per Lecturer Per Semester: Max 15 items
```

**CR4: Lecturer Approval (If Not Auto-Approved)**
```
Lecturer can APPROVE or REJECT
Time: 4-8 hours
```

**CR5: Technical Officer Override**
```
TO CAN REJECT even if all approvers agree
Reasons: Equipment condition, safety, unavailability
```

**CR6: Return & Completion**
```
Student returns equipment
TO inspects & assesses damage
Equipment status updated
Request status = RETURNED
```

### RESEARCH Business Rules (v3.3)

**RR1: Research Definition**
```
VALID RESEARCH:
✓ Final year capstone projects
✓ Independent study courses
✓ Faculty-supervised research
✓ Honors projects

INVALID (use PERSONAL):
✗ Internship projects
✗ Consulting work
✗ Freelance assignments
```

**RR2: Supervisor Assignment**
```
REQUIREMENT: Student must have assigned supervisor
If no supervisor: DECLINED (NO_SUPERVISOR)
```

**RR3: Supervisor Approval (FINAL AUTHORITY v3.3)**
```
Supervisor options:
A) APPROVE → Routes to TO
B) REJECT → DECLINED (FINAL, no appeal)

Escalation: If supervisor unavailable >48 hours
            HOD can approve/reject on behalf
```

**RR4: TO Authority**
```
TO CAN REJECT even if supervisor approves
Reasons: Equipment condition, safety, unavailability
```

**RR5: Equipment for Research**
```
Suitable: Measurement, specialized, computer equipment
Not suitable: Consumable components (but allowed)
```

### EXTRACURRICULAR Business Rules

**ER1: Event Definition**
```
VALID:
✓ Tech club meetings
✓ Tech fest competitions
✓ Departmental exhibitions
✓ Student organization events
✓ Science week activities

INVALID:
✗ Guest lectures (use COURSEWORK)
✗ Faculty research (use RESEARCH)
```

**ER2: HOD Decision**
```
APPROVER: HOD (not lecturer, not committee)
Options: APPROVE or REJECT
Time: 24-48 hours
```

**ER3: Lecturer Input (Optional)**
```
HOD may consult lecturer
Lecturer opinion is advisory only
HOD makes final decision
```

### PERSONAL Business Rules

**PR1: Activity Definition**
```
VALID:
✓ Individual hobby projects
✓ Self-learning initiatives
✓ Freelance work
✓ Internship project activities

INVALID:
✗ Final year capstone (use RESEARCH)
✗ Published research (use RESEARCH)
✗ Classroom coursework (use COURSEWORK)
```

**PR2: Lecturer Recommendation (REQUIRED)**
```
Lecturer options:
A) RECOMMEND_YES → Routes to HOD
B) RECOMMEND_NO → DECLINED (FINAL, no appeal)
```

**PR3: HOD Approval (After Positive Recommendation)**
```
HOD options:
A) APPROVE → Routes to Lab Instr (if lab equipment) or TO
B) REJECT → DECLINED
```

**PR4: Lab Instructor Observation (If Lab Equipment)**
```
Lab Instructor options:
A) RECOMMEND_APPROVE → Routes to TO
B) RECOMMEND_DELAY → Request queued
C) FLAG_SAFETY → Escalates to HOD
```

**PR5: TO Inspection (Final Gate)**
```
TO options:
A) ISSUE → Equipment given
B) REJECT → DECLINED
```

---

## DATABASE SCHEMA - FINAL

### Core Tables (Summary)

```sql
-- Academic Structure
├─ departments
├─ semesters
├─ courses
├─ labs
└─ users (all user types)

-- Academic Staff
├─ lecturers
├─ technical_officers
├─ lab_instructors
├─ heads_of_department
└─ student_supervisor_relationships

-- Requests & Workflows
├─ requests (all 4 types)
├─ request_approvals (tracks each approval stage)
├─ request_status_history
└─ request_comments

-- Equipment Management
├─ equipment
├─ equipment_categories
├─ equipment_status_history
├─ equipment_assignments
└─ equipment_maintenance_logs

-- Transactions
├─ equipment_transactions (issue/return)
├─ equipment_damage_reports
└─ penalties

-- System
├─ audit_logs
├─ notifications
└─ user_sessions
```

---

## API ENDPOINTS - COMPLETE LIST

### Request Management APIs

**Submit COURSEWORK Request**
```
POST /api/requests/coursework
Authorization: Bearer {student_token}

Body:
{
  "course_id": 5,
  "course_name": "CS301 - Database Systems",
  "equipment_ids": [1, 2],
  "quantities": [1, 2],
  "required_date": "2026-01-15",
  "justification": "Lab assignment on SQL optimization"
}

Response:
{
  "request_id": "REQ-2026-001",
  "status": "APPROVED" or "IN_REVIEW_LECTURER",
  "auto_approved": true/false,
  "next_action": "TO Inspection" or "Awaiting Lecturer Review"
}
```

**Submit RESEARCH Request**
```
POST /api/requests/research
Authorization: Bearer {student_token}

Body:
{
  "research_topic": "ML Hardware Acceleration",
  "supervisor_id": 25,
  "equipment_ids": [10],
  "quantities": [1],
  "expected_duration_months": 4,
  "justification": "Final year capstone project"
}

Response:
{
  "request_id": "REQ-2026-002",
  "status": "IN_REVIEW_SUPERVISOR",
  "supervisor_name": "Dr. Johnson"
}
```

**Submit PERSONAL Request**
```
POST /api/requests/personal
Authorization: Bearer {student_token}

Body:
{
  "project_name": "IoT Home Automation System",
  "lecturer_id": 12,
  "equipment_ids": [15],
  "quantities": [1],
  "project_description": "Individual learning project",
  "expected_duration_days": 60
}

Response:
{
  "request_id": "REQ-2026-003",
  "status": "IN_REVIEW_LECTURER"
}
```

**Submit EXTRACURRICULAR Request**
```
POST /api/requests/extracurricular
Authorization: Bearer {organizer_token}

Body:
{
  "event_name": "Tech Fest 2026",
  "equipment_ids": [1, 2, 3],
  "quantities": [2, 1, 3],
  "event_date": "2026-01-25",
  "event_description": "Annual technology festival"
}

Response:
{
  "request_id": "REQ-2026-004",
  "status": "IN_REVIEW_HOD"
}
```

### Approval APIs

**Approve/Reject Request**
```
PUT /api/requests/{request_id}/decide
Authorization: Bearer {approver_token}

Body:
{
  "action": "APPROVE" or "REJECT" or "RECOMMEND_YES" or "RECOMMEND_NO",
  "approver_role": "LECTURER" or "SUPERVISOR" or "HOD" or "LAB_INSTRUCTOR",
  "comments": "Equipment is necessary for this course"
}

Response:
{
  "request_id": "REQ-2026-001",
  "status": "APPROVED" or "DECLINED",
  "next_action": "Pending HOD Review" or "TO Inspection"
}
```

### TO Operations APIs

**List Pending Inspections**
```
GET /api/to/pending-inspections
Authorization: Bearer {to_token}

Response:
{
  "pending_count": 12,
  "requests": [
    {
      "request_id": "REQ-2026-045",
      "equipment_name": "Oscilloscope",
      "submitted_hours_ago": 6,
      "priority": 1
    }
  ]
}
```

**Complete Inspection & Issue**
```
PUT /api/to/{request_id}/inspect-and-issue
Authorization: Bearer {to_token}

Body:
{
  "action": "ISSUE" or "REJECT",
  "condition_on_issue": "EXCELLENT" or "GOOD" or "FAIR",
  "condition_notes": "Equipment fully functional"
}

Response:
{
  "transaction_id": "TXN-2026-001",
  "status": "ISSUED",
  "issue_date": "2026-01-10T09:00:00Z",
  "expected_return_date": "2026-01-17"
}
```

**Process Equipment Return**
```
PUT /api/to/{request_id}/process-return
Authorization: Bearer {to_token}

Body:
{
  "return_condition": "EXCELLENT" or "GOOD" or "DAMAGED",
  "damage_description": "Slight scratch on back panel",
  "damage_severity": "LOW" or "MEDIUM" or "HIGH"
}

Response:
{
  "transaction_id": "TXN-2026-001",
  "status": "RETURNED",
  "damage_assessed": true,
  "penalty_applied": false
}
```

### Admin APIs

**Add Staff**
```
POST /api/admin/staff
Authorization: Bearer {admin_token}

Body:
{
  "role": "LECTURER" or "TECHNICAL_OFFICER" or "LAB_INSTRUCTOR",
  "first_name": "John",
  "last_name": "Smith",
  "email": "john.smith@unijaffna.edu.lk",
  "phone": "+94712345678",
  "department_id": 1,
  "designation": "Senior Lecturer",
  "lab_assignments": [1, 2] (if TO or Lab Instructor)
}

Response:
{
  "user_id": 101,
  "username": "lecturer_001",
  "temp_password": "TempPass@2026",
  "message": "Account created. Credentials sent to email."
}
```

**Add Equipment**
```
POST /api/admin/equipment
Authorization: Bearer {admin_token}

Body:
{
  "equipment_name": "Digital Oscilloscope",
  "equipment_code": "OSC-D-001",
  "category": "MEASUREMENT_INSTRUMENTS",
  "lab_id": 1,
  "serial_number": "SN123456",
  "manufacturer": "Agilent",
  "model_number": "DSO1012A",
  "purchase_price": 45000.00,
  "purchase_date": "2022-01-15"
}

Response:
{
  "equipment_id": 1,
  "status": "AVAILABLE",
  "created_at": "2026-01-03T10:00:00Z"
}
```

### Reporting APIs

**Equipment Status Report**
```
GET /api/reports/equipment-status
Authorization: Bearer {token}

Response:
{
  "total_equipment": 190,
  "available": 150,
  "borrowed": 25,
  "maintenance": 10,
  "damaged": 5,
  "unavailable": 0,
  "utilization_rate": "65%"
}
```

**Request Statistics**
```
GET /api/reports/requests-statistics
Authorization: Bearer {token}

Response:
{
  "total_requests": 1250,
  "coursework": 800,
  "research": 150,
  "extracurricular": 100,
  "personal": 200,
  "approved_count": 1150,
  "declined_count": 100,
  "average_approval_time_hours": 24
}
```

---

## IMPLEMENTATION ROADMAP

### Phase 1: Foundation & Database (Week 1-2)

**Week 1: Setup**
- [ ] Database schema finalization
- [ ] Create all tables with migrations
- [ ] Set up indexes for performance
- [ ] Configure backup strategy

**Week 2: Core Infrastructure**
- [ ] User authentication & authorization
- [ ] Role-based access control (RBAC)
- [ ] Request ID generation
- [ ] Status management engine
- [ ] Notification service

### Phase 2: Business Logic (Week 3-4)

**Week 3: Coursework & Research**
- [ ] Auto-approval conditions (6 logic)
- [ ] COURSEWORK workflow
- [ ] RESEARCH workflow
- [ ] Supervisor routing

**Week 4: Extracurricular & Personal**
- [ ] EXTRACURRICULAR approval
- [ ] PERSONAL multi-gate approval
- [ ] Escalation rules
- [ ] Complete testing

### Phase 3: TO & Equipment (Week 5-6)

**Week 5: TO Operations**
- [ ] Inspection workflow
- [ ] Rejection codes
- [ ] Issue/return system

**Week 6: Equipment Management**
- [ ] Status transitions
- [ ] Maintenance scheduling
- [ ] Damage assessment
- [ ] Penalty calculation

### Phase 4: API Development (Week 7-8)

**Week 7: Request APIs**
- [ ] All request endpoints
- [ ] Approval actions
- [ ] Status tracking

**Week 8: Admin & TO APIs**
- [ ] Admin CRUD endpoints
- [ ] TO inspection endpoints
- [ ] Analytics endpoints

### Phase 5: Frontend Development (Week 9-10)

**Week 9: Student Interface**
- [ ] Request forms (4 types)
- [ ] Status dashboard
- [ ] Equipment tracking

**Week 10: Admin & Staff Dashboards**
- [ ] Approver interfaces
- [ ] TO dashboard
- [ ] Admin panels
- [ ] Reports

### Phase 6: Testing & Deployment (Week 11-12)

**Week 11: Testing**
- [ ] Unit tests
- [ ] Integration tests
- [ ] Performance testing
- [ ] Security testing
- [ ] UAT with departments

**Week 12: Go-Live**
- [ ] Pilot deployment (CSE)
- [ ] Full deployment (CSE + EEE)
- [ ] Training & documentation
- [ ] Support & monitoring

---

## DEPLOYMENT & GO-LIVE

### Pilot Phase (Week 12 - Phase 1)

**CSE Department Only**
```
Week 1-2: Pilot Period
├─ All CSE requests routed through EqipHub
├─ CSE staff trained & using system
├─ Parallel paper records kept as backup
├─ Daily support team available
├─ Collect feedback

Week 3: Review & Adjust
├─ Analyze pilot data
├─ Fix identified issues
├─ Optimize workflows
├─ Update documentation

Week 4: CSE Goes Live
├─ Stop parallel paper records
├─ Full CSE deployment
├─ Celebrate milestone!
```

### Full Deployment (Phase 2)

**CSE + EEE Departments**
```
Week 1: EEE Training
├─ Train EEE staff
├─ Import EEE equipment data
├─ Configure EEE labs & TOs

Week 2: EEE Pilot
├─ EEE parallel testing
├─ Feedback collection
├─ Final adjustments

Week 3: Full Deployment
├─ Both departments live
├─ Support team ready
├─ Ongoing monitoring
```

### Go-Live Checklist

```
BEFORE GO-LIVE:
☐ Database fully populated (all equipment, staff, courses)
☐ All staff trained & tested
☐ System tested with real data
☐ Backups configured & tested
☐ Support team ready
☐ Help desk prepared
☐ Documentation finalized
☐ User manuals printed/available
☐ Email templates tested
☐ Notifications working

GO-LIVE DAY:
☐ System monitoring active
☐ Support team on-call
☐ Helpdesk staffed
☐ Email notifications sent
☐ Status page updated
☐ Acceptance feedback forms ready

POST GO-LIVE:
☐ Daily health checks (Week 1)
☐ Issue tracking & resolution
☐ Performance monitoring
☐ User feedback collection
☐ Weekly review meetings
☐ Gradual ramping up to full capacity
```

---

## SUMMARY OF VERSION CHANGES

### v3.3 vs v3.2

| Feature | v3.2 | v3.3 |
|---------|------|------|
| **Department Details** | Estimated | CSE: Real data (3 TOs, 6 labs) |
| **TO Assignment** | Generic | Each TO: 2 labs + 1 backup |
| **User Types** | 8 | **9 (added Dept Admin)** |
| **Admin vs Staff** | Mixed | **CLEAR SEPARATION** |
| **Staff CRUD Students** | Allowed | **NOT ALLOWED** |
| **Registration Flows** | Basic | Detailed: Students self-reg, Staff admin-created |
| **Request Workflows** | Fixed | **CONFIRMED: Code-based, no admin config** |
| **Admin Permissions** | General | Detailed CRUD matrix |
| **TODaily Workflow** | Basic | Morning/Day/Evening workflow |

### All v3.3 Features Finalized

✅ 9 User types with clear responsibilities  
✅ Admin vs Staff separation (critical)  
✅ CSE department real structure (3 TOs, 6 labs)  
✅ TO lab assignments (2 primary + 1 backup each)  
✅ Student registration (self-register, admin approve)  
✅ Staff account creation (admin-only)  
✅ Staff cannot CRUD students (admin-only)  
✅ Fixed request procedures (code-based)  
✅ No admin workflow configuration (by design)  
✅ Complete equipment categorization  
✅ 6-condition auto-approval for COURSEWORK  
✅ Supervisor-only RESEARCH approval  
✅ Multi-gate PERSONAL approval  
✅ Complete TO inspection workflow  
✅ 12-week implementation roadmap  

---

## NEXT STEPS FOR TEAM

### Immediate Actions

1. **Distribute Documentation**
   - [ ] Share v3.3 with all team members
   - [ ] Schedule reading time (2-3 hours)
   - [ ] Prepare questions/clarifications

2. **Stakeholder Review**
   - [ ] Present to CSE HOD
   - [ ] Present to EEE HOD
   - [ ] Get formal approval

3. **Technical Review**
   - [ ] Database schema review with DBA
   - [ ] API design review with backend lead
   - [ ] Frontend mockups with UI/UX team

4. **Team Planning**
   - [ ] Assign developers to features
   - [ ] Create detailed user stories
   - [ ] Estimate timeline
   - [ ] Set up development environment

### Week 1 Kickoff Activities

- [ ] Team meeting: Overview of all clarifications
- [ ] Database team: Start schema creation
- [ ] Backend team: Begin API structure
- [ ] Frontend team: Start wireframes
- [ ] Support: Prepare training materials

---

## DOCUMENT INFORMATION

**Project Name:** EqipHub  
**Version:** 3.3 (Final with v3.3 Clarifications)  
**Status:** Ready for Development  
**Date:** January 3, 2026, 9:12 PM (IST)  
**Location:** Ariviyal Nagar, Northern Province, LK  

**Total Pages:** 50+  
**Total Words:** 8,500+  
**Document Size:** Production-ready  

**Prepared By:** Software Engineering Team  
**Reviewed By:** Project Manager  
**Approved By:** Stakeholders (Pending)  

**Next Review:** Week 1 of implementation  
**Last Updated:** January 3, 2026  

---

**END OF EqipHub v3.3 COMPLETE DOCUMENTATION**

This document is comprehensive and ready for team implementation. All clarifications from your latest update have been incorporated into a professional, structured format suitable for development team handover.
