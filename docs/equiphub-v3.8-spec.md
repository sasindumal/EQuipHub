# EqipHub v3.8 - COMPREHENSIVE SYSTEM SPECIFICATION
## Equipment Request & Management System (REVISED CLARIFICATIONS)
### Faculty of Engineering | CSE & EEE Departments

**Document Version:** 3.8 (FINAL - Stakeholder Clarifications Integrated)  
**Date:** January 12, 2026, 10:57 PM +0530  
**Status:** ✅ READY FOR DEVELOPMENT  
**Supersedes:** v3.7  

---

## 📝 CHANGE LOG v3.7 → v3.8

| Section | Change | Reason |
|---------|--------|--------|
| **Role Model** | RESTRUCTURED (7-tier hierarchy) | Clarity on System Admin, Dept Admin, Lecturer, Instructor distinctions |
| **Instructor vs Lecturer** | REDEFINED | Instructors ≠ Lecturers (authority, responsibilities, eligibility) |
| **Lab Session Booking** | Students CANNOT submit | Only Instructors can book lab sessions |
| **Approval Notifications** | SIMULTANEOUS to all 5 parties | Reliability through parallel processing |
| **TO Authority** | Limited to equipment/quantity/condition | CANNOT modify dates (requests go back to requestor) |
| **Request Modifications** | By approvers/recommenders | System auto-notifies requestor, requires acceptance/rejection |
| **Urgent Lab Sessions** | <48h requests to HOD approval | HOD decides after TO inspection |
| **Extracurricular** | Appointed Lecturer approval (not Instructor) | Role-specific governance |
| **Penalty Approval** | Department Admin authority | Dept Admin approves/reduces/waives penalties |
| **Cross-Department Borrowing** | Allowed for Personal/Research/Extracurricular | NOT allowed for Coursework/Lab Sessions |
| **Department Isolation** | Enforced for academic courses | CSE courses → CSE labs, EEE courses → EEE labs |
| **Lab Session Equipment** | Can include Borrowable items | Not just Lab-Dedicated equipment |
| **Priority Levels** | REORDERED (1=Lab Session, 5=Personal) | INVERTED priority system |
| **Department Attributes** | Added to Instructor/TO roles | Enable department-specific equipment access |
| **Reasons Required** | All rejections/non-recommendations | Transparency and auditability |

---

## 🏛️ SECTION 1: ROLE-BASED ARCHITECTURE

### 1.1 Seven-Tier Role Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                    SYSTEM ADMIN (Super User)                │
│  • Manages all departments globally                         │
│  • Cannot approve individual requests                       │
│  • Configures system-wide parameters                        │
│  • Monitors all system activities                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│         DEPARTMENT ADMIN (Departmental Authority)           │
│  • Manages departmental staff (Instructors, TOs)            │
│  • APPROVES/REDUCES/WAIVES penalties                        │
│  • Monitors departmental requests                           │
│  • Configures department parameters                         │
│  • Cannot approve individual requests                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  HEAD OF DEPARTMENT (HOD - Highest in Department)           │
│  • Emergency approval authority (<48h lab sessions)         │
│  • Final appeals decision authority                         │
│  • Policy interpretation & exceptions                       │
│  • Can override normal approval workflows                   │
└─────────────────────────────────────────────────────────────┘
        ↙                   ↓                    ↘
┌──────────────┐  ┌─────────────────┐  ┌──────────────────┐
│  LECTURER    │  │ INSTRUCTOR      │  │ APPOINTED        │
│(Approval     │  │(Recommendation) │  │LECTURER          │
│Authority)    │  │(Limited)        │  │(Activity Lead)   │
│              │  │                 │  │                  │
│ • APPROVE/   │  │ • RECOMMEND     │  │ • APPROVE        │
│   REJECT     │  │   requests      │  │   Extracurricular│
│   requests   │  │ • INSPECT       │  │ • Not in         │
│ • REVERSE    │  │   equipment     │  │   hierarchy      │
│   approvals  │  │ • BOOK lab      │  │ • Special        │
│ • AUTO-      │  │   sessions      │  │   designation    │
│   APPROVE if │  │ • For PERSONAL, │  │                  │
│   conditions │  │   can also      │  │                  │
│   met        │  │   RECOMMEND     │  │                  │
└──────────────┘  └─────────────────┘  └──────────────────┘
                        ↓
        ┌───────────────────────────────┐
        │ TECHNICAL OFFICER (TO)        │
        │ • Equipment management        │
        │ • Availability checking       │
        │ • Condition assessment        │
        │ • CRUD on equipment/inventory │
        │ • CANNOT modify dates         │
        │ • CANNOT approve/reject       │
        └───────────────────────────────┘
                        ↓
        ┌───────────────────────────────┐
        │ STUDENT (Request Submitter)   │
        │ • Submit requests (4 types)   │
        │ • CANNOT submit Lab Sessions  │
        │ • Accept/reject modifications │
        │ • Appeal penalties            │
        │ • View own status             │
        └───────────────────────────────┘
```

---

### 1.2 DETAILED ROLE DEFINITIONS

#### **ROLE 1: SYSTEM ADMIN**

**Definition:** Super user with global system access and oversight.

**Attributes:**
```
user_role = "SYSTEM_ADMIN"
authority_scope = "GLOBAL"
can_manage_all_departments = true
department_assignment = null  // Not department-specific
```

**Permissions:**
- ✅ View all system data (all departments, all requests)
- ✅ Configure system-wide parameters (timeouts, thresholds)
- ✅ Manage Department Admins (assign/revoke)
- ✅ Monitor system health & performance
- ✅ Generate system-wide reports
- ❌ CANNOT approve individual student requests
- ❌ CANNOT waive penalties
- ❌ CANNOT modify approval workflows

**Responsibilities:**
- System infrastructure maintenance
- Role management (assign Dept Admin roles)
- System configuration oversight
- Audit trail management
- Security and data integrity

**Example:** IT Director managing entire EqipHub system across both departments

---

#### **ROLE 2: DEPARTMENT ADMIN**

**Definition:** Departmental authority managing staff and enforcing penalties.

**Attributes:**
```
user_role = "DEPARTMENT_ADMIN"
authority_scope = "DEPARTMENT"
department_id = "CSE" or "EEE"  // Must be department-specific
can_manage_instructors = true
can_manage_tos = true
can_manage_penalties = true
```

**Permissions:**
- ✅ Manage departmental staff (Instructors, TOs)
- ✅ **APPROVE penalties** (after TO assessment)
- ✅ **REDUCE penalties** (if appeal justifies)
- ✅ **WAIVE penalties** (for documented exceptions)
- ✅ Configure department parameters:
  - Max retention times per request type
  - Penalty rates (pts/day late)
  - Auto-approval thresholds
- ✅ Monitor departmental requests
- ✅ Review request rejection reasons
- ❌ CANNOT approve individual student requests
- ❌ CANNOT override lecturer decisions
- ❌ CANNOT modify approved requests (except penalties)

**Responsibilities:**
- Departmental staff management
- Penalty governance & fairness review
- Department configuration management
- Policy enforcement within department
- Departmental reporting

**Example:** CSE Department Admin approving penalty reduction for student with documented family emergency

---

#### **ROLE 3: HEAD OF DEPARTMENT (HOD)**

**Definition:** Highest departmental authority for emergencies and appeals.

**Attributes:**
```
user_role = "HEAD_OF_DEPARTMENT"
authority_scope = "DEPARTMENT"
department_id = "CSE" or "EEE"
is_final_authority = true
```

**Permissions:**
- ✅ **APPROVE <48h lab session requests** (emergency labs)
  - After TO inspection confirms equipment ready
  - Can set expedited SLA (4 hours vs 24 hours)
- ✅ **FINAL AUTHORITY on penalty appeals**
  - Reviews Dept Admin's appeal decision
  - Can overturn/modify appeal decisions
  - Provides final explanation to student
- ✅ **OVERRIDE normal workflows** for documented emergencies
  - Force-approve requests outside parameters
  - Grant exceptions (equipment borrowing, department crossing)
- ✅ Veto power on controversial decisions
- ❌ CANNOT bulk-approve regular requests

**Responsibilities:**
- Emergency request authority
- Final appeals decision-maker
- Exception handling
- Departmental policy oversight
- Crisis management

**Example:** HOD approves emergency lab session on Friday for Monday practical exam when 48h notice not met

---

#### **ROLE 4: LECTURER (Academic Approval Authority)**

**Definition:** Faculty member with APPROVAL authority for student requests (NOT recommendation).

**Attributes:**
```
user_role = "LECTURER"
authority_scope = "DEPARTMENT"
department_id = "CSE" or "EEE"
assigned_courses = ["CS301", "CS302", ...]  // Which courses they teach
is_approval_authority = true
```

**Permissions:**
- ✅ **APPROVE requests** (final approval decision)
  - Coursework requests (if assigned to course)
  - Research requests (if recommended by Supervisor)
  - Personal requests (if conditions met)
  - Lab Session requests (if submitted by assigned Instructor)
- ✅ **REJECT requests** (with required reason)
  - Reason must be specific (not "Not approved")
  - Reasons logged for student understanding
- ✅ **REVERSE approvals** (if error detected)
  - With reversal_reason field
  - Audit trail preserved
- ✅ **AUTO-APPROVE** if conditions met:
  - Equipment available
  - Student status not blocked
  - Retention within limits
  - Lecturer can configure auto-approval rules
- ❌ CANNOT recommend (lecturers don't recommend, they approve)
- ❌ CANNOT modify dates/equipment (those come from student or TO)
- ❌ CANNOT waive penalties (Dept Admin does that)
- ❌ CANNOT submit Lab Session requests (Instructors do that)

**Responsibilities:**
- Academic oversight through approval authority
- Quality control of coursework/research requests
- Policy enforcement
- Clear communication via reasons/feedback
- Approval decision-making

**Example:** Lecturer approves CS301 coursework request from enrolled student, rejects request from student not enrolled in course with reason "Student not enrolled in CS301 this semester"

---

#### **ROLE 5: INSTRUCTOR (Academic Support Staff - LIMITED Authority)**

**Definition:** Academic support staff with RECOMMENDATION authority (not approval).

**Attributes:**
```
user_role = "INSTRUCTOR"
authority_scope = "DEPARTMENT"
department_id = "CSE" or "EEE"
assigned_courses = ["CS301-Lab", "CS302-Lab", ...]
assigned_labs = ["CSE-Lab-3", "CSE-Lab-5", ...]
can_recommend = true
can_book_lab_sessions = true
can_inspect_equipment = true
```

**Permissions for Coursework Requests:**
- ✅ **AUTO-RECOMMEND** coursework requests (for assigned courses)
  - Automatic if student enrolled in course
  - Automatic if equipment available
  - Automatic if retention within limits
  - No manual intervention needed
  - Lecturer still must approve
- ✅ Add comments/suggestions to pending requests
- ❌ CANNOT APPROVE (that's Lecturer authority)
- ❌ CANNOT REJECT (that's Lecturer authority)

**Permissions for Lab Sessions:**
- ✅ **SUBMIT Lab Session requests** (Instructors only)
  - Only Instructors can book labs (not students)
  - Must specify: course, date, time-slot, equipment, student count
  - Lecturer co-approves within 24h
  - HOD auto-confirms (mandatory type)
- ✅ Choose time slots (8-12 or 1-4 PM)
- ✅ Request equipment list
- ✅ Specify student count for equipment allocation

**Permissions for Equipment Inspection:**
- ✅ **INSPECT equipment** before issuance
  - Check condition, completeness
  - Sign condition report (with student)
  - Take pre-use photos
- ✅ **INSPECT equipment** after return
  - Document damage level (if any)
  - Photograph damage
  - Provide assessment to TO

**Permissions for Personal Requests:**
- ✅ **AUTO-RECOMMEND** personal requests
  - Instructor from student's assigned course can recommend
  - Recommendation speeds up approval (Lecturer still must approve)

**Restrictions:**
- ❌ CANNOT modify request dates
- ❌ CANNOT approve (not approval authority)
- ❌ CANNOT book lab sessions for other departments
- ❌ CANNOT REJECT (not their role)

**Special Notes:**
- Instructors are academic support staff (may be fresh graduates)
- Different from Lecturer (senior faculty with approval authority)
- Instructors have department assignments
- Multiple Instructors per course are possible

**Example:** 
1. Instructor books CS301-Lab Monday 8-12, needs oscilloscopes, 60 students
2. Lecturer (who teaches CS301) co-approves lab request
3. HOD auto-confirms (mandatory)
4. TO prepares 10 oscilloscopes for lab

---

#### **ROLE 6: APPOINTED LECTURER (Activity-Specific Authority)**

**Definition:** Lecturer designated as lead for extracurricular activities (not hierarchical).

**Attributes:**
```
user_role = "APPOINTED_LECTURER"
authority_scope = "ACTIVITY_SPECIFIC"
department_id = "CSE" or "EEE"
assigned_activities = ["Robotics Club", "Coding Contest", ...]
is_special_designation = true
```

**Permissions:**
- ✅ **APPROVE Extracurricular requests** (for assigned activities)
  - Reviews activity safety, educational value
  - Ensures equipment used appropriately
  - Can request modifications (equipment, quantity, dates)
- ✅ Add activity-specific requirements
- ✅ Recommend equipment alternatives
- ❌ CANNOT approve other request types
- ❌ CANNOT approve requests outside assigned activities

**Responsibilities:**
- Activity oversight
- Equipment use safety
- Activity planning coordination
- Equipment return verification

**Example:** Faculty lead for Robotics Club approves equipment request for regional competition, specifies return deadline

---

#### **ROLE 7: TECHNICAL OFFICER (TO) - Logistics Authority**

**Definition:** Equipment logistics specialist managing availability and condition.

**Attributes:**
```
user_role = "TECHNICAL_OFFICER"
authority_scope = "DEPARTMENT"
department_id = "CSE" or "EEE"
assigned_labs = ["CSE-Lab-3", "CSE-Lab-5", ...]
can_check_availability = true
can_assess_condition = true
can_modify_requests = false  // KEY: NO date modifications
```

**Permissions - Equipment Availability:**
- ✅ Check real-time availability
- ✅ Create availability records
- ✅ Mark equipment as IN_USE, RESERVED, MAINTENANCE
- ✅ Reserve equipment for requests
- ✅ Suggest equipment alternatives (different equipment, different model)

**Permissions - Quantity Management:**
- ✅ **Adjust requested QUANTITY** (if not available)
  - Example: "Only 3 available instead of 5"
  - Student must accept/reject modification
  - If rejected: request cancels
  - Request auto-notifies student

**Permissions - Condition Assessment:**
- ✅ Inspect equipment condition (before issuance)
- ✅ Inspect equipment condition (after return)
- ✅ Assess damage level (0-5 scale)
- ✅ Take damage photos
- ✅ Document pre-use condition
- ✅ Create condition reports (with student signatures)

**Permissions - Inventory CRUD:**
- ✅ Create new equipment entries
- ✅ Update equipment details
- ✅ Read equipment information
- ✅ Delete equipment (archived)

**CRITICAL Restrictions:**
- ❌ CANNOT modify REQUEST DATES
  - If equipment unavailable for requested dates, TO must:
    1. Suggest alternative dates (separate request)
    2. Student accepts/rejects
    3. If rejected: request cancels
    4. Requestor decides, TO cannot force
- ❌ CANNOT approve/reject requests
- ❌ CANNOT modify approval decisions
- ❌ CANNOT waive penalties
- ❌ CANNOT access requests from other departments

**Responsibilities:**
- Equipment availability management
- Equipment condition assessment & documentation
- Inventory management
- Modification suggestions (equipment, quantity only)
- Condition report coordination with users

**Example:** TO discovers only 6 multimeters available (student requested 10). TO creates modification: "Quantity changed from 10 to 6". Student receives notification: "Accept 6 or reject request?" Student accepts → request continues with 6 multimeters.

---

### 1.3 Role Authority Matrix (Quick Reference)

| Action | System Admin | Dept Admin | HOD | Lecturer | Instructor | Appointed Lecturer | TO |
|--------|--------------|-----------|-----|----------|------------|-------------------|-----|
| Approve Requests | ❌ No | ❌ No | ✅ <48h Labs only | ✅ Yes | ❌ No | ✅ Extracurr. | ❌ No |
| Reject Requests | ❌ No | ❌ No | N/A | ✅ Yes + reason | ❌ No | ❌ No | ❌ No |
| Recommend Requests | ❌ No | ❌ No | N/A | ❌ No | ✅ Coursework/Personal | ❌ No | ❌ No |
| Reverse Approvals | ❌ No | ❌ No | N/A | ✅ Yes | ❌ No | ❌ No | ❌ No |
| Modify Dates | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ❌ NO |
| Suggest Alternatives | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Equipment/Qty |
| Approve Penalties | ❌ No | ✅ Yes | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No |
| Appeal Penalties | ❌ No | ❌ No | ✅ Final | ❌ No | ❌ No | ❌ No | ❌ No |
| Book Lab Sessions | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes | ❌ No | ❌ No |
| Inspect Equipment | ❌ No | ❌ No | ❌ No | ✅ Optional | ✅ Yes | ❌ No | ✅ Yes |
| Manage Staff | ✅ Yes | ✅ Yes | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No |

---

## 🔄 SECTION 2: REQUEST TYPES & APPROVAL WORKFLOWS

### 2.1 PRIORITY LEVELS (UPDATED - Inverted)

```
PRIORITY 1 (HIGHEST)    → Lab Session     (SLA: 4 hours)
PRIORITY 2              → Coursework      (SLA: 24 hours)
PRIORITY 3              → Research        (SLA: 48 hours)
PRIORITY 4              → Extracurricular (SLA: 48 hours)
PRIORITY 5 (LOWEST)     → Personal        (SLA: 72 hours)
```

**Rationale:**
- Lab Sessions are institutional requirement (cannot fail)
- Coursework supports active course learning
- Research is student-initiated scholarship
- Extracurricular is optional co-curricular
- Personal is non-academic (privilege)

---

### 2.2 LAB SESSION REQUEST TYPE (Priority 1 - HIGHEST)

#### **CRITICAL: Only Instructors Can Submit Lab Session Requests**

**Definition:** Structured booking of lab equipment for mandatory course lab sessions. Students CANNOT submit these directly.

**Who Can Submit:**
- ✅ Instructors (assigned to course labs)
- ❌ Students (NOT allowed)
- ❌ Lecturers (NOT allowed)

**Required Information:**
```json
{
  "course_id": "CS301",
  "lab_instructor_id": 12345,  // Submitting Instructor
  "course_lecturer_id": 67890,  // Co-approval required
  "session_date": "2026-02-15",
  "time_slot": "08-12",  // 8 AM to 12 PM OR
  "time_slot": "13-16",  // 1 PM to 4 PM (only these 2 slots)
  "student_count": 60,
  "equipment_list": [
    {
      "equipment_id": "MULTI-001",
      "equipment_type": "BORROWABLE",  // Can be Borrowable too!
      "quantity": 10,
      "purpose": "Voltage measurements"
    }
  ],
  "advance_notice_hours": 48  // MANDATORY minimum
}
```

**Approval Chain:**
```
Instructor submits Lab Session
    ↓
Notifications sent to:
├─ Lecturer (course lecturer co-approves)
├─ TO (equipment availability check)
├─ Dept Admin (monitoring)
└─ Student roster (optional notification)

Lecturer Review (within 24h):
├─ Verifies course correctness
├─ Checks instructor assignment
└─ APPROVES or REJECTS

TO Check (immediate):
├─ Checks equipment availability
├─ Suggests alternatives if needed
└─ CONFIRMS or SUGGESTS_ALTERNATIVE

HOD Auto-Confirms:
└─ No manual review (mandatory type)
   → Automatic APPROVED

Result: LAB SESSION APPROVED
    ↓
Equipment marked RESERVED
TO prepares equipment
Equipment issued to lab
```

**Advance Notice Requirement:**
- ✅ 48 hours MANDATORY
- ❌ <48 hours requests go to HOD for emergency approval
  - HOD must approve within 4 hours
  - TO inspects equipment immediately
  - If approved: expedited preparation

**Equipment Eligibility:**
- ✅ Lab-Dedicated equipment (primary)
- ✅ Borrowable equipment (allowed for labs)
- Example: Oscilloscopes (lab-dedicated) + Function Generators (borrowable)

**Return Policy:**
- Automatic upon session end
- TO collects equipment immediately after lab
- Same-day condition assessment

**SLA (Service Level Agreement):**
- 4 hours for approved lab sessions
- Cannot be delayed or postponed without HOD intervention

---

### 2.3 COURSEWORK REQUEST TYPE (Priority 2)

**Definition:** Course-related assignment/project requiring equipment.

**Who Can Submit:**
- ✅ Student (enrolled in course)
- ❌ Others

**Department Isolation (CRITICAL):**
- CSE coursework requests can ONLY use CSE labs/equipment
- EEE coursework requests can ONLY use EEE labs/equipment
- ❌ No cross-departmental borrowing for coursework

**Required Information:**
```json
{
  "request_type": "COURSEWORK",
  "course_id": "CS301",  // MUST match student enrollment
  "department_id": "CSE",  // Determined from course_id
  "from_datetime": "2026-02-10T08:00:00",
  "to_datetime": "2026-02-12T17:00:00",
  "max_retention_days": 2,  // Auto-enforced CSE limit
  "equipment_list": [
    {
      "equipment_id": "MULTI-001",
      "quantity": 2
    }
  ],
  "coursework_description": "Oscillator frequency analysis lab assignment"
}
```

**Approval Chain:**
```
Student submits Coursework Request
    ↓
Lecturer review:
└─ Verifies student enrolled in course
   APPROVES or REJECTS (with reason)

Instructor recommendation (optional, speeds approval):
└─ Auto-recommends if course enrollment verified
   Lecturer can auto-approve if recommended

TO check:
└─ Confirms equipment available for dates
   Suggests alternatives if needed

All notified SIMULTANEOUSLY
    ↓
Result: APPROVED → Equipment issued
        REJECTED → Student notified with reason
        MODIFICATION → Student decides
```

**Max Retention:**
- CSE: 7 days (configurable by Dept Admin)
- EEE: 10 days (configurable by Dept Admin)
- Auto-enforced at submission

**Instructor Recommendation:**
- Automatic if student enrolled in course
- Instructor can add comments
- Speeds up Lecturer approval

**SLA:** 24 hours

---

### 2.4 RESEARCH REQUEST TYPE (Priority 3)

**Definition:** Semester 6-8 research project requiring extended equipment use.

**Eligibility:**
- ✅ Students in semester 6, 7, or 8 ONLY
- ✅ Supervisor must be assigned and approve
- ✅ Can request from any department (cross-dept allowed)

**Semester Verification:**
```
System auto-checks:
├─ Current semester in [6, 7, 8]?
├─ Student has assigned supervisor?
└─ Supervisor email on record?

If NOT eligible:
└─ Request blocked with message:
   "Research requests eligible for semester 6-8 only.
    Complete your coursework semesters first."
```

**Who Approves:**
- Supervisor MUST recommend
- Lecturer MUST approve (if supervisor recommends)
- Cross-dept is OK (CSE student can request from EEE labs)

**Required Information:**
```json
{
  "request_type": "RESEARCH",
  "supervisor_id": 45678,  // From student record
  "from_datetime": "2026-02-10T08:00:00",
  "to_datetime": "2026-04-10T17:00:00",
  "max_retention_days": 30,  // Research can go longer
  "extension_requests_allowed": 2,  // Up to +10 days each
  "equipment_list": [...],
  "research_topic": "FPGA-based signal processing",
  "department_request": "EEE"  // Can cross departments
}
```

**Approval Chain:**
```
Student submits Research Request
    ↓
Supervisor review:
├─ Verifies semester (6-8)
├─ Verifies student assignment
├─ Reviews research merit
└─ RECOMMENDS or REJECTS (with reason)

Lecturer review:
├─ Checks supervisor recommendation
├─ Verifies equipment appropriateness
└─ APPROVES or REJECTS (with reason)

TO check:
└─ Equipment available for extended period
   Suggests alternatives if needed

All notified SIMULTANEOUSLY
    ↓
Result: APPROVED → Equipment issued for 30 days
        EXTENSION possible → +10 days (max 2×)
```

**Extensions:**
- Student can request +10 days
- Supervisor must re-recommend extension
- Lecturer auto-approves if supervisor recommends
- Maximum: 2 extensions (60 days total)

**SLA:** 48 hours

**Cross-Department Rules:**
- ✅ CSE student can request from EEE labs
- ✅ EEE student can request from CSE labs
- Department isolation NOT enforced for research
- Approval still flows through appropriate department

---

### 2.5 EXTRACURRICULAR REQUEST TYPE (Priority 4)

**Definition:** Co-curricular activities (clubs, competitions, events).

**Who Can Submit:**
- ✅ Activity representative / Club president
- ❌ Individual students (must be club activity)

**Who Approves:**
- ✅ Appointed Lecturer (activity lead) - REQUIRED
- ❌ Regular Instructor cannot approve
- ❌ Lecturer cannot approve (Appointed Lecturer only)

**Department Rules:**
- ✅ Cross-departmental allowed
- Example: Robotics Club (mixed CSE/EEE) can borrow from either department
- No department isolation enforced

**Required Information:**
```json
{
  "request_type": "EXTRACURRICULAR",
  "activity_id": "ROBOTICS-CLUB-2026",
  "appointed_lecturer_id": 98765,
  "activity_name": "Regional Robotics Competition",
  "from_datetime": "2026-03-01T08:00:00",
  "to_datetime": "2026-03-03T18:00:00",
  "equipment_list": [
    {
      "equipment_type": "BORROWABLE",
      "equipment_id": "POWER-SUPPLY-01",
      "quantity": 3
    }
  ],
  "event_location": "Off-campus",
  "safety_notes": "High-voltage testing planned"
}
```

**Approval Chain:**
```
Activity representative submits request
    ↓
Appointed Lecturer review:
├─ Verifies activity legitimacy
├─ Checks safety concerns
├─ Reviews equipment appropriateness
└─ APPROVES or REJECTS (with reason)

TO check:
└─ Equipment available for dates
   Suggests alternatives if needed

All notified SIMULTANEOUSLY
    ↓
Result: APPROVED → Equipment issued
        Safety requirements noted
```

**Special Considerations:**
- Equipment may leave campus (off-campus events)
- Extra insurance/liability may apply
- Appointed Lecturer verifies safety
- Condition documentation extra important

**SLA:** 48 hours

---

### 2.6 PERSONAL REQUEST TYPE (Priority 5 - LOWEST)

**Definition:** Personal learning projects (non-academic, non-course-related).

**Who Can Submit:**
- ✅ Students (with documented justification)
- ❌ Others

**Department Rules:**
- ✅ Cross-departmental allowed
- Student can request from CSE OR EEE labs
- Example: Personal hobby project can use any department's equipment

**Status Restrictions:**
- ✅ GOOD: Can submit personal requests
- ✅ WARNING: Can submit personal requests
- ✅ CAUTION: Can submit personal requests
- ❌ SERIOUS: BLOCKED - cannot submit personal requests
- ❌ RESTRICTED: BLOCKED - cannot submit personal requests
- ❌ SUSPENDED: BLOCKED - no requests of any type

**Required Information:**
```json
{
  "request_type": "PERSONAL",
  "student_id": 11111,
  "from_datetime": "2026-02-10T08:00:00",
  "to_datetime": "2026-02-11T17:00:00",
  "max_retention_days": 1,  // Personal capped at 14 days max
  "equipment_list": [
    {
      "equipment_type": "BORROWABLE",  // ONLY borrowable
      "equipment_id": "MULTI-001",
      "quantity": 1
    }
  ],
  "project_description": "Personal weather station prototype development (min 100 words required)",
  "learning_objective": "Understanding IoT sensor integration...",
  "department_request": "CSE"  // Can be any department
}
```

**Approval Chain:**
```
Student submits Personal Request
    ↓
Lecturer review (mandatory):
├─ Verifies project is personal/non-academic
├─ Verifies educational merit (100+ word justification)
├─ Checks student status (not SERIOUS/RESTRICTED/SUSPENDED)
└─ APPROVES or REJECTS (with reason)

Instructor recommendation (optional):
├─ If instructor from same course can recommend
└─ Speeds approval but not required

TO check:
└─ Equipment available
   Suggests alternatives if needed

All notified SIMULTANEOUSLY
    ↓
Result: APPROVED → Equipment issued with extra conditions
        Extra condition tracking: "Personal equipment"
        Higher damage penalties apply (2.0× multiplier)
```

**Restrictions:**
- ✅ Borrowable equipment ONLY (not lab-dedicated)
- ✅ Maximum 14 days retention
- ❌ No extensions allowed
- ❌ Highest late return penalties (20 pts/day)
- ❌ Highest damage penalties (2.0× multiplier)

**Extra Safeguards:**
- Pre-use condition photos MANDATORY
- Student signature on condition report MANDATORY
- Return condition photos MANDATORY
- Damage assessment extra strict

**SLA:** 72 hours (longest among request types)

---

## 🔔 SECTION 3: NOTIFICATION & APPROVAL SYSTEM

### 3.1 Simultaneous Multi-Party Notification (CRITICAL)

**Design Principle:** ALL relevant parties notified SIMULTANEOUSLY (not sequentially).

```
When request submitted:
    ↓
IMMEDIATELY notify (in parallel, same second):
├─ Lecturer (approval authority)
│   └─ "Request CS301-REQ-001 awaiting your approval"
├─ Instructor (recommendation, if applicable)
│   └─ "Request CS301-REQ-001 awaiting your recommendation"
├─ TO (equipment check)
│   └─ "Request CS301-REQ-001 needs equipment availability check"
├─ Dept Admin (monitoring)
│   └─ "Request CS301-REQ-001 submitted (monitoring)"
└─ Student (tracking)
    └─ "Your request submitted, tracking: CS301-REQ-001"
```

**Benefits:**
- ✅ **Reliability:** If one reviewer unavailable, others still reviewing
- ✅ **Speed:** No waiting for sequential approvals (parallel processing)
- ✅ **Transparency:** Student sees who is reviewing
- ✅ **Accountability:** All reviewers visible in timeline

**Implementation:**
```javascript
// Pseudo-code
async function submitRequest(request) {
  // All notifications sent in parallel
  const notifications = await Promise.all([
    notifyLecturer(request),
    notifyInstructor(request),
    notifyTO(request),
    notifyDeptAdmin(request),
    notifyStudent(request)
  ]);
  
  // Request moves to IN_APPROVAL state
  // All parties working simultaneously
}
```

---

### 3.2 Request State Machine (9 States)

```
                    START
                      ↓
                 ┌─────────┐
                 │ PENDING │ (validation)
                 └────┬────┘
                      ↓
              ┌─────────────────┐
              │  IN_APPROVAL    │ (all 5 notified)
              │ (Parallel work) │
              └────┬────────────┘
                   ├─ Lecturer APPROVES
                   ├─ TO SUGGESTS_ALTERNATIVE
                   └─ Any party REJECTS → REJECTED
                      ↓
              ┌──────────────────────┐
    ┌─────────│ MODIFICATION_        │
    │         │ REQUESTED            │
    │         │ (Student decides)    │
    │         └──────┬───────────────┘
    │                ↓
    │    ┌─────────────────────────────┐
    │    │ Student has 24 hours to:    │
    │    │ ACCEPT or REJECT            │
    │    │ (Timeout = AUTO-CANCEL)     │
    │    └──────┬──────────┬──────────┘
    │          │          │
    │    ACCEPT│          │REJECT/TIMEOUT
    │          ↓          ↓
    │    ┌──────────┐  ┌──────────┐
    │    │APPROVED  │  │CANCELLED │
    └────→ (continue)   └──────────┘
         │                 ↓
         │            (FINAL)
         │
         ↓
    ┌────────────────┐
    │    APPROVED    │ (equipment ready)
    └────┬───────────┘
         ├─ TO ISSUES → ISSUED
         ├─ Lecturer REVERSES → REJECTED
         └─ Equipment unavailable → CANCELLED
            ↓
    ┌────────────────┐
    │     ISSUED     │ (with student)
    └────┬───────────┘
         └─ Deadline exceeded → Penalty triggers
            (request still IN_ISSUED state)
            ↓
    ┌────────────────┐
    │    RETURNED    │ (inspection in progress)
    └────┬───────────┘
         └─ TO completes inspection
            ↓
    ┌────────────────┐
    │   COMPLETED    │ (FINAL - may have penalties)
    └────────────────┘

ALTERNATIVE PATHS:
    REJECTED (from IN_APPROVAL)
    └─ Student can appeal or resubmit
    
    CANCELLED (various causes)
    └─ Equipment unavailable, modification rejected, etc.
```

**State Definitions:**

| State | Meaning | Actions Possible |
|-------|---------|-----------------|
| PENDING | Initial validation | System validates format |
| IN_APPROVAL | All parties notified, reviewing in parallel | Lecturer approves/rejects, TO suggests, others comment |
| MODIFICATION_REQUESTED | TO suggested alternative | Student accepts/rejects (24h window) |
| APPROVED | All approvals complete | TO issues equipment |
| ISSUED | Equipment with student | Student uses equipment |
| RETURNED | Student returned, inspection in progress | TO assesses condition |
| COMPLETED | Request finished, closed | Archived (penalties may apply) |
| REJECTED | Lecturer rejected | Student can appeal/resubmit |
| CANCELLED | Request cancelled (various causes) | Student can resubmit |

---

### 3.3 Approval Decision Reasons (MANDATORY)

**Requirement:** Every rejection, non-recommendation, or reversal MUST include specific reason.

**For Lecturer REJECTION:**
```json
{
  "decision": "REJECTED",
  "reason": "Student not enrolled in CS301 this semester",
  "reason_category": "ENROLLMENT_MISMATCH",
  "resolution_hint": "Enroll in course and resubmit"
}
```

**For Instructor NON-RECOMMENDATION:**
```json
{
  "decision": "NO_RECOMMENDATION",
  "reason": "Equipment not appropriate for stated coursework level",
  "reason_category": "EQUIPMENT_MISMATCH",
  "suggestion": "Consider using oscilloscopes instead of analog meters"
}
```

**For TO MODIFICATION:**
```json
{
  "decision": "MODIFICATION_REQUESTED",
  "modification_type": "QUANTITY_ADJUSTED",
  "original_quantity": 10,
  "available_quantity": 6,
  "reason": "Only 6 multimeters available for requested dates",
  "student_action": "Accept 6 units or reject request (24h to decide)"
}
```

**For Lecturer REVERSAL:**
```json
{
  "decision": "REVERSED",
  "reversal_reason": "Equipment found to be non-functional, resubmit when repaired",
  "timestamp": "2026-02-10T14:30:00"
}
```

**Reason Categories (Pre-defined):**
- ENROLLMENT_MISMATCH (student not in course)
- EQUIPMENT_UNAVAILABLE (not available for dates)
- RETENTION_EXCEEDED (more than max days)
- STUDENT_STATUS_BLOCKED (penalties, restrictions)
- SAFETY_CONCERN (equipment safety issue)
- EQUIPMENT_MISMATCH (wrong type of equipment)
- QUANTITY_UNAVAILABLE (insufficient quantity)
- DOCUMENTATION_INCOMPLETE (missing justification)
- POLICY_VIOLATION (against regulations)
- OTHER (free-text reason)

**Student Impact:**
- Reasons shown on dashboard
- Clear explanation helps student resubmit correctly
- Reasons logged for system analysis
- Transparent decision-making

---

## 📋 SECTION 4: TO MODIFICATION & REQUESTOR APPROVAL

### 4.1 TO Modification Process (Updated)

**Key Principle:** TO can suggest alternatives, but CANNOT force changes. Requestor must accept or reject.

**Types of Modifications TO Can Make:**

#### Type 1: Equipment Substitution
```json
{
  "modification_type": "EQUIPMENT_SUBSTITUTED",
  "original_equipment": "MULTI-001 (Digital Multimeter)",
  "suggested_equipment": "MULTI-002 (Analog Multimeter)",
  "reason": "Digital multimeter unavailable for requested dates",
  "student_decision_deadline": "2026-02-10T17:00:00",
  "student_action_options": [
    "ACCEPT: I'll use the analog multimeter",
    "REJECT: I need the digital multimeter or nothing"
  ]
}
```

#### Type 2: Quantity Adjustment
```json
{
  "modification_type": "QUANTITY_ADJUSTED",
  "equipment": "MULTI-001",
  "original_quantity": 10,
  "available_quantity": 6,
  "reason": "Only 6 multimeters available for Feb 10-12",
  "student_action": "Accept 6 or reject request"
}
```

#### Type 3: Date Suggestion (REQUIRES APPROVAL)
```json
{
  "modification_type": "DATE_SUGGESTION",
  "original_dates": "2026-02-10 to 2026-02-12",
  "suggested_dates": "2026-02-15 to 2026-02-17",
  "reason": "Requested dates have maintenance scheduled",
  "student_decision_deadline": "2026-02-09T17:00:00",
  "note": "This modification goes back to Lecturer for re-approval"
}
```

**CRITICAL: Date Modifications Require Re-Approval**

If TO suggests different dates:
1. Modification created: DATE_SUGGESTION
2. System notifies student
3. Student accepts/rejects (24h window)
4. **If ACCEPTED:**
   - Request goes back to IN_APPROVAL state
   - Lecturer & Instructor re-review (may auto-approve if conditions still met)
   - All parties notified again
5. **If REJECTED:**
   - Request cancelled
   - Student can resubmit with different dates

---

### 4.2 Student Modification Decision Flow

```
TO creates modification
    ↓
IMMEDIATE notification to student:
├─ Email + Dashboard notification
├─ Specific modification details
├─ Deadline to accept/reject (usually 24h)
└─ Consequences explained

Student decision:
├─ ACCEPT: "Yes, I'll proceed with alternative"
│   └─ If dates changed: Goes back to IN_APPROVAL
│       └─ Lecturer re-reviews & decides
│   └─ If equipment/qty: Continues to APPROVED
│       └─ Equipment issued with modification
│
└─ REJECT: "No, I need original or nothing"
    └─ Request CANCELLED
        └─ Student can resubmit with different dates
            or request different quantity
```

**Timeout Behavior:**
- If student doesn't respond within 24 hours
- Request automatically CANCELLED
- Student notified of cancellation
- Can resubmit anytime

---

## 🏥 SECTION 5: URGENT/EMERGENCY LAB SESSIONS (<48h)

### 5.1 Urgent Lab Session Approval Process

**Trigger:** Lab Session request with advance notice < 48 hours

**Approval Authority:** HEAD OF DEPARTMENT (HOD) only

**Process:**
```
Instructor submits <48h Lab Session request
    ↓
System flags as URGENT
    ↓
Immediate TO inspection (within 1 hour):
├─ Check equipment availability
├─ Verify equipment functionality
├─ Assess preparation time needed
└─ TO provides inspection report

HOD decision (within 4 hours):
├─ Reviews urgency justification (exam, emergency, etc.)
├─ Reviews TO inspection report
├─ APPROVES (expedited) or REJECTS
└─ If APPROVED:
    ├─ Expedited preparation (high priority)
    ├─ Equipment prepared immediately
    └─ Lab proceeds with available equipment

SLA: 4-hour HOD decision (vs 24h normal)
```

**Justification Requirements:**
- Why less than 48h notice?
  - Example: "Sudden makeup exam scheduled"
  - Example: "Lab instructor unexpected absence, rescheduled"
  - Example: "Equipment repair just completed"
- HOD assesses urgency vs convenience
- HOD can approve or reject based on justification

**CRITICAL:** 
- Emergency does NOT guarantee approval
- HOD evaluates legitimacy
- Cannot be used for casual last-minute bookings

---

## ⚖️ SECTION 6: EQUIPMENT DEPARTMENT ISOLATION

### 6.1 Department-Based Access Rules

#### **RULE 1: Academic Courses (STRICT Isolation)**

```
CSE Courses (CS-*, COM-*)
  └─ Can ONLY use CSE labs/equipment
     └─ Coursework requests: CSE equipment only
        Lab Sessions: CSE labs only

EEE Courses (ELE-*, PWR-*, COM-*)
  └─ Can ONLY use EEE labs/equipment
     └─ Coursework requests: EEE equipment only
        Lab Sessions: EEE labs only
```

**Enforcement:**
- System auto-determines department from course_id
- Coursework from CS301 → CSE department isolation
- Coursework from ELE201 → EEE department isolation
- Cross-departmental coursework requests REJECTED automatically

**Example Rejection:**
```
Student submits: CS301 Coursework requesting EEE power supplies
System response: "REJECTED - Coursework requests must use equipment 
                 from the same department as course.
                 CS301 is CSE course, only CSE equipment allowed."
```

---

#### **RULE 2: Research (Cross-Departmental Allowed)**

```
Research requests: ANY department can be requested
├─ CSE student can request from EEE equipment
├─ EEE student can request from CSE equipment
└─ Department isolation NOT enforced
```

**Rationale:** Research is scholarly work, requires equipment availability regardless of department

**Approval:**
- Supervisor + Lecturer from requested department still approve
- Dept Admin of REQUESTED department processes

---

#### **RULE 3: Personal (Cross-Departmental Allowed)**

```
Personal requests: ANY department can be requested
├─ Student can choose CSE or EEE
├─ No department requirement
└─ Department isolation NOT enforced
```

**Rationale:** Personal projects are optional, student chooses convenience

---

#### **RULE 4: Extracurricular (Cross-Departmental Allowed)**

```
Extracurricular: ANY department can be requested
├─ Club can use CSE equipment
├─ Club can use EEE equipment
├─ Club can use equipment from BOTH departments
└─ Department isolation NOT enforced
```

**Rationale:** Co-curricular activities often involve mixed departments

**Approval:**
- Appointed Lecturer of activity approves
- If equipment from multiple departments:
  - TO from each department verifies availability
  - Activity lead has flexibility

---

### 6.2 Department Assignment Enforcement

**For Instructors & TOs:**

```
instructor {
  id: 12345,
  name: "Mr. Smith",
  department_id: "CSE",  // MUST have department
  assigned_courses: ["CS301", "CS302"],
  assigned_labs: ["CSE-Lab-3", "CSE-Lab-5"],
  
  permissions: {
    can_book_labs_in: ["CSE-Lab-3", "CSE-Lab-5"],
    cannot_book_labs_in: ["EEE-Lab-1", "EEE-Lab-2"],
    can_recommend_for_courses: ["CS301", "CS302"],
    cannot_recommend_for_courses: ["ELE201", "ELE202"]
  }
}

to {
  id: 67890,
  name: "Mr. Ahmed",
  department_id: "EEE",  // MUST have department
  assigned_labs: ["EEE-Lab-1", "EEE-Lab-2"],
  
  permissions: {
    can_manage_equipment_in: ["EEE-Lab-1", "EEE-Lab-2"],
    cannot_manage_equipment_in: ["CSE-Lab-3", "CSE-Lab-5"],
    can_suggest_from: ["EEE Equipment"]
  }
}
```

**Enforcement:**
- Instructor cannot book labs outside assigned department
- TO cannot manage equipment outside assigned department
- System enforces via role-based access control

---

## 💳 SECTION 7: BORROWABLE EQUIPMENT IN LAB SESSIONS

### 7.1 Lab Sessions Can Include Borrowable Equipment

**Key Change:** Lab Sessions are not limited to Lab-Dedicated equipment.

**Scenario:**
```
Lab Session: CS301-Lab-Week5
├─ Lab-Dedicated equipment: 10 Oscilloscopes (CSE-Lab-3)
└─ Borrowable equipment: 5 Function Generators (from inventory)

Instructor specifies:
├─ Equipment #1: Oscilloscope (lab-dedicated, cannot remove)
└─ Equipment #2: Function Generator (borrowable, reserve for lab)

Both reserved for lab session
Lab equipment retrieved from lab + inventory
Lab proceeds with complete setup
Equipment returned after lab
```

**Approval:**
- Both types listed in lab session request
- TO confirms availability for both types
- Lecturer approves combined equipment list

**Return:**
- Lab-Dedicated returns immediately to lab
- Borrowable returns to inventory after lab

---

## 📊 SECTION 8: DATABASE SCHEMA UPDATES

### 8.1 New/Updated Tables for v3.8

```sql
-- EXISTING: users table (add role hierarchy info)
ALTER TABLE users ADD COLUMN (
  user_role ENUM('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN', 'HOD', 
                 'LECTURER', 'INSTRUCTOR', 'APPOINTED_LECTURER', 'TO', 'STUDENT'),
  department_id VARCHAR(20),  -- CSE or EEE
  assigned_courses JSON,  -- For Instructors/Lecturers
  assigned_labs JSON,  -- For Instructors/TOs
  authority_scope ENUM('GLOBAL', 'DEPARTMENT', 'ACTIVITY_SPECIFIC'),
  is_final_authority BOOLEAN DEFAULT false,
  can_manage_staff BOOLEAN DEFAULT false,
  can_approve_requests BOOLEAN DEFAULT false,
  can_recommend_requests BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT NOW()
);

-- NEW: request_modifications table
CREATE TABLE request_modifications (
  id VARCHAR(50) PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  modification_type ENUM('EQUIPMENT_SUBSTITUTED', 'QUANTITY_ADJUSTED', 'DATE_SUGGESTION'),
  original_value VARCHAR(255),
  modified_value VARCHAR(255),
  reason TEXT NOT NULL,
  suggested_by_user_id INT REFERENCES users(id),
  student_decision VARCHAR(20),  -- 'PENDING', 'ACCEPTED', 'REJECTED', 'TIMEOUT'
  student_decision_deadline TIMESTAMP NOT NULL,
  student_decision_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_request (request_id),
  INDEX idx_status (student_decision)
);

-- NEW: request_approval_history table
CREATE TABLE request_approval_history (
  id SERIAL PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  approver_id INT REFERENCES users(id),
  approver_role VARCHAR(30),
  decision VARCHAR(20),  -- 'APPROVED', 'REJECTED', 'RECOMMENDED', 'REVERSED'
  decision_reason TEXT,  -- MANDATORY for REJECTED/REVERSED
  reason_category VARCHAR(50),  -- ENROLLMENT_MISMATCH, etc.
  decided_at TIMESTAMP DEFAULT NOW(),
  notification_sent_at TIMESTAMP,
  INDEX idx_request (request_id),
  INDEX idx_approver (approver_id),
  INDEX idx_decision (decision)
);

-- NEW: request_notifications table
CREATE TABLE request_notifications (
  id SERIAL PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  recipient_id INT REFERENCES users(id),
  recipient_role VARCHAR(30),
  notification_type VARCHAR(30),  -- 'PENDING_APPROVAL', 'MODIFICATION', 'REJECTION', etc.
  sent_at TIMESTAMP DEFAULT NOW(),
  read_at TIMESTAMP,
  INDEX idx_request (request_id),
  INDEX idx_recipient (recipient_id),
  INDEX idx_sent_at (sent_at)
);

-- EXISTING: requests table (add new fields)
ALTER TABLE requests ADD COLUMN (
  advance_notice_hours INT,  -- For lab sessions
  semester_eligible INT,  -- For research (6, 7, 8)
  cross_departmental BOOLEAN DEFAULT false,
  is_urgent BOOLEAN DEFAULT false,  -- For <48h lab sessions
  has_modifications BOOLEAN DEFAULT false,
  modification_count INT DEFAULT 0
);

-- NEW: lab_session_details table
CREATE TABLE lab_session_details (
  id VARCHAR(50) PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id),
  course_id VARCHAR(20) NOT NULL,
  lab_instructor_id INT REFERENCES users(id) NOT NULL,
  course_lecturer_id INT REFERENCES users(id) NOT NULL,
  session_date DATE NOT NULL,
  time_slot VARCHAR(10),  -- '08-12' or '13-16'
  student_count INT,
  equipment_list JSON,
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_course (course_id),
  INDEX idx_date (session_date)
);

-- NEW: emergency_lab_approvals table
CREATE TABLE emergency_lab_approvals (
  id SERIAL PRIMARY KEY,
  lab_session_id VARCHAR(50) REFERENCES lab_session_details(id),
  hod_id INT REFERENCES users(id),
  urgency_justification TEXT NOT NULL,
  to_inspection_report TEXT,
  approval_decision VARCHAR(20),  -- 'APPROVED', 'REJECTED'
  decided_at TIMESTAMP DEFAULT NOW(),
  approval_sla_hours INT DEFAULT 4
);

-- NEW: appointment_lecturers table (for extracurricular activities)
CREATE TABLE appointed_lecturers (
  id SERIAL PRIMARY KEY,
  lecturer_id INT REFERENCES users(id) NOT NULL,
  activity_id VARCHAR(50) NOT NULL,
  activity_name VARCHAR(255),
  department_id VARCHAR(20),
  appointment_date DATE,
  expiry_date DATE,
  is_active BOOLEAN DEFAULT true,
  INDEX idx_lecturer (lecturer_id),
  INDEX idx_activity (activity_id)
);
```

---

## 📋 SECTION 9: CONFIGURATION PARAMETERS BY DEPARTMENT

### 9.1 Department-Specific Configuration

**CSE Department:**
```json
{
  "department_id": "CSE",
  "department_name": "Computer Science & Engineering",
  "max_retention_days": {
    "COURSEWORK": 7,
    "RESEARCH": 30,
    "EXTRACURRICULAR": 14,
    "PERSONAL": 14
  },
  "penalty_rates": {
    "COURSEWORK_late_per_day": 15,
    "RESEARCH_late_per_day": 20,
    "EXTRACURRICULAR_late_per_day": 10,
    "PERSONAL_late_per_day": 5
  },
  "damage_multipliers": {
    "COURSEWORK": 1.0,
    "RESEARCH": 1.0,
    "EXTRACURRICULAR": 1.0,
    "PERSONAL": 2.0
  },
  "auto_approval_conditions": {
    "equipment_value_under": 5000,
    "student_grade_minimum": "C",
    "student_status_minimum": "GOOD"
  }
}
```

**EEE Department:**
```json
{
  "department_id": "EEE",
  "department_name": "Electrical & Electronics Engineering",
  "max_retention_days": {
    "COURSEWORK": 10,
    "RESEARCH": 30,
    "EXTRACURRICULAR": 14,
    "PERSONAL": 14
  },
  "penalty_rates": {
    "COURSEWORK_late_per_day": 12,
    "RESEARCH_late_per_day": 20,
    "EXTRACURRICULAR_late_per_day": 10,
    "PERSONAL_late_per_day": 5
  },
  "damage_multipliers": {
    "COURSEWORK": 1.0,
    "RESEARCH": 1.0,
    "EXTRACURRICULAR": 1.0,
    "PERSONAL": 2.0
  },
  "auto_approval_conditions": {
    "equipment_value_under": 5000,
    "student_grade_minimum": "C",
    "student_status_minimum": "GOOD"
  }
}
```

---

## ✅ SECTION 10: IMPLEMENTATION CHECKLIST

### Pre-Development Verification

- [ ] 7-tier role hierarchy understood by all stakeholders
- [ ] Instructor vs Lecturer distinction clear
- [ ] Lab Session booking by Instructors only (not students)
- [ ] Simultaneous notification system defined
- [ ] TO modification process (no date changes) documented
- [ ] Urgent lab session approval authority (HOD) confirmed
- [ ] Extracurricular approval (Appointed Lecturer) confirmed
- [ ] Department isolation rules (academic vs research) confirmed
- [ ] Penalty approval authority (Dept Admin) confirmed
- [ ] Cross-departmental rules clear
- [ ] Reason requirements for all rejections documented
- [ ] Emergency exception procedures defined
- [ ] Database schema updates prepared
- [ ] API endpoints for all modifications defined
- [ ] State machine tests prepared

---

## 🚀 SECTION 11: DEVELOPMENT PHASES (UPDATED)

### Phase 1: Authentication & Role Management (Weeks 1-2)
- [ ] Implement 7-tier role model
- [ ] User authentication (JWT)
- [ ] Role-based access control (RBAC)
- [ ] Department assignments
- [ ] Authority matrix enforcement

### Phase 2: Request Workflows (Weeks 3-4)
- [ ] Lab Session booking (Instructors only)
- [ ] Coursework requests (students)
- [ ] Research requests (semester validation)
- [ ] Extracurricular requests
- [ ] Personal requests (status checks)

### Phase 3: Approval & Modification System (Weeks 5-6)
- [ ] Simultaneous notifications (5 parties)
- [ ] Parallel approval processing
- [ ] TO modifications (equipment, quantity)
- [ ] Student modification decisions (24h window)
- [ ] Reason requirement enforcement
- [ ] Date modification re-approval

### Phase 4: Governance & Penalties (Weeks 7-8)
- [ ] Urgent lab session approval (HOD)
- [ ] Emergency exception handling
- [ ] Penalty approval (Dept Admin)
- [ ] Penalty appeals
- [ ] Penalty decay
- [ ] Status level enforcement

### Phase 5: Testing & Deployment (Weeks 9-10)
- [ ] Integration testing (all workflows)
- [ ] UAT with stakeholders
- [ ] Performance testing (concurrent approvals)
- [ ] Security testing (role isolation)
- [ ] Production deployment
- [ ] Training & documentation

---

## 📞 VERSION HISTORY

| Version | Date | Key Changes |
|---------|------|------------|
| **v3.8** | Jan 12, 2026 | Role hierarchy clarification, Instructor/Lecturer distinction, Student cannot submit Lab Sessions, Simultaneous notifications, TO cannot modify dates, Urgent lab approval, Appointed Lecturer for extracurricular, Dept Admin penalties, Cross-dept rules |
| **v3.7** | Jan 10, 2026 | Equipment types, 5 request types, From/To model, Penalties, Lab priority |
| **v3.6** | Prior | Foundation version |

---

**Document Status:** ✅ FINAL - READY FOR DEVELOPMENT  
**Last Updated:** January 12, 2026, 10:57 PM +0530  
**Confidence Level:** 98% (all stakeholder clarifications integrated)  
**Next Step:** Development Kickoff - January 27, 2026  

---

**Prepared By:** Software Engineering Team  
**For:** Faculty of Engineering, CSE & EEE Departments  
**Classification:** INTERNAL - Implementation Guide