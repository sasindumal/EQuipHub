# EqipHub v3.7 - Complete System Specification
## Equipment Request & Inventory Management System
### Faculty of Engineering | Department of Computer Engineering & Electrical & Electronics Engineering

**Document Version:** 3.7 (Major Revision - Policy & Workflow Overhaul)  
**Date:** January 10, 2026  
**Status:** FINAL - READY FOR IMPLEMENTATION  
**Supersedes:** v3.6  

---

## 📋 EXECUTIVE SUMMARY

**EqipHub v3.7** is a comprehensive equipment request and inventory management system designed for the Faculty of Engineering. This version introduces **critical structural changes** to support two distinct equipment type categories with different request workflows, new lab session mandatory requests, enhanced deadline policies, and revised penalty calculation based on damage assessment and retention periods.

### Major Changes from v3.6 to v3.7

| Feature | v3.6 | v3.7 | Impact |
|---------|------|------|--------|
| **Equipment Types** | Single category | Lab-Dedicated & Borrowable (separate rules) | Workflow differentiation, time-based vs day-based |
| **Request Types** | 4 types | **5 types** (NEW: Lab Session mandatory) | Priority hierarchy, mandatory pre-booking |
| **Time Model** | Semester-long (vague) | Specific From/To dates+times per request | Exact retention & deadline tracking |
| **Approval Flow** | Simple linear | Multi-role, context-aware, type-specific | Role authority differs by request type |
| **Penalties** | Damage-only | **Damage + Late Return + Lab Priority Override** | Penalty points calculated on multiple factors |
| **Lab Priority** | Not defined | **HIGHEST (5)** - overrides all other types | Immediate return policy on lab request |
| **Deadline Policy** | Vague semester limits | **Max retention time per request** (configurable) | Realistic, enforced retention windows |
| **Instructor Sign-off** | For research only | **Mandatory for coursework + all types** | Quality control, academic oversight |
| **Auto-Approval** | 6-condition check | **Conditional per equipment type + request type** | Smarter business logic |
| **Lab Session Booking** | Single 3-hour booking | **Lab Session Type**: structured, priority, 2-day advance notice | Prevents equipment shortage during mandated labs |

---

## 🏗️ SECTION 1: SYSTEM ARCHITECTURE & DATA MODEL

### 1.1 Equipment Type Classification

#### **Type 1: Lab-Dedicated Equipment**

**Definition:** Equipment permanently assigned to specific labs, used during scheduled lab sessions or controlled coursework activities. Cannot be removed from lab premises.

**Characteristics:**
- Fixed location (specific lab)
- Time-slot based consumption (8 AM - 12 PM, 1 PM - 4 PM, weekdays only)
- No overnight/weekend borrowing
- Instructor oversight required for all requests
- Immediate availability visibility

**Request Types Eligible:**
- ✅ Lab Session (NEW - mandatory, highest priority)
- ✅ Coursework (during lab hours only)
- ✅ Research (with supervisor + instructor co-approval)
- ⚠️ Extracurricular (with HOD recommendation + instructor approval)
- ❌ Personal (NOT allowed)

**Return Policy:**
- Automatic upon session end
- TO inspection required
- Same-day reporting mandatory

**Example Equipment:**
- Digital Oscilloscopes (CSE Lab 3)
- Power Supplies (Electrical Lab)
- Network Analyzers (CSE Lab 5)
- PCB Soldering Stations (Electronics Lab)

---

#### **Type 2: Borrowable Equipment**

**Definition:** Equipment that can be borrowed from the TO and taken outside lab premises for specified periods. Must be returned by deadline.

**Characteristics:**
- Day-based borrowing (not hour-based)
- Can be borrowed for 1-N days (configurable max)
- Overnight/weekend borrowing allowed
- Approval chain required before TO issuance
- Portable, returnable condition

**Request Types Eligible:**
- ✅ Coursework (day-based deadline)
- ✅ Research (day-based deadline)
- ✅ Extracurricular (day-based deadline)
- ✅ Personal (with HOD + instructor approval)
- ❌ Lab Session (N/A - not applicable)

**Return Policy:**
- Strict deadline enforcement
- Late penalty applied per day late
- Damage inspection at return
- Condition verification

**Example Equipment:**
- Multimeters
- Function Generators
- Logic Probes
- USB Measurement Devices
- Portable Test Equipment

---

### 1.2 Request Type Classification (5 Total)

#### **Priority Hierarchy**

```
PRIORITY LEVEL 5 (HIGHEST) → Lab Session
PRIORITY LEVEL 4           → Research
PRIORITY LEVEL 3           → Coursework
PRIORITY LEVEL 2           → Extracurricular
PRIORITY LEVEL 1 (LOWEST)  → Personal
```

---

#### **Request Type 1: Lab Session (NEW - MANDATORY)**

**Purpose:** Structured request for equipment needed during scheduled course lab sessions (mandatory coursework component).

**Characteristics:**
- ✅ **Mandatory** - All lab sessions must have equipment pre-booked
- ✅ **Highest Priority (Level 5)** - Overrides all other request types
- ✅ **Lab-Dedicated Equipment Only** - Cannot use borrowable items
- ✅ **Advance Notice** - Must request ≥2 days before lab date
- ✅ **Structured Workflow** - Specific time slots, automatic return

**Requestor:**
- Primary: Lab Instructor (for the course)
- Co-requestor: Course Lecturer (for the course)
- Both must submit/approve

**Approval Chain:**
1. Lab Instructor creates request
2. Course Lecturer approves (verifies accuracy)
3. HOD auto-confirms (mandatory type)
4. TO receives (highest priority, reserved immediately)

**Equipment Specification:**
- Equipment type: LAB-DEDICATED only
- Time slot: 8-12 AM or 1-4 PM (no custom times)
- Duration: Fixed per course module
- Date: Lab session date (weekday only)
- Cancellation: Must cancel ≥48 hours before (or penalty applies)

**Return Policy:**
- **Immediate return** at session end (typically 3 or 4 hours)
- TO collects immediately after session
- Inspection before next session booking

**Deadline Policy:**
- Request deadline: 2 days (48 hours) before lab date
- No extensions (fixed lab schedule)
- Late requests: Rejected (notify instructor + HOD)

**Immediate Return Override:**
- If equipment needed for another Lab Session request:
  - Current borrower MUST return immediately
  - Penalty applies if not returned on time
  - Lab Session request takes precedence

**Example:**
```
LAB SESSION REQUEST: CSE301-Lab-W3
├─ Course: CS301 (Database Systems)
├─ Lab Instructor: Dr. Kumar
├─ Course Lecturer: Dr. Smith
├─ Date: Wednesday, Jan 22, 2026 (Lab Week 3)
├─ Time Slot: 10:00 AM - 1:00 PM (3 hours)
├─ Equipment: Digital Oscilloscope (2 units)
├─ Student Count: 30
├─ Status: AUTO-RESERVED (highest priority)
└─ Return: Immediate at 1:00 PM (TO collects)
```

---

#### **Request Type 2: Coursework**

**Purpose:** Equipment request for coursework assignments and lab practicals (not mandatory lab sessions).

**Characteristics:**
- For course assignments, projects, non-mandatory lab activities
- Both Lab-Dedicated and Borrowable equipment eligible
- Variable duration (per assignment requirements)
- Instructor sign-off required
- Semester-long deadline policy

**Requestor:** Student (with course enrollment)

**Approval Chain:**
```
Student submits request
    ↓
Instructor reviews & approves (mandatory sign-off)
    ↓
[If Lab-Dedicated Equipment] → TO schedules
[If Borrowable Equipment] → TO issues immediately
```

**For Lab-Dedicated Equipment:**
- Time slot: 8-12 AM or 1-4 PM (weekdays only)
- Max duration: 4 hours per session
- Max total: 2 sessions per semester per item
- Instructor must verify classroom/lab availability

**For Borrowable Equipment:**
- Borrowing period: Specified as From Date/Time to To Date/Time
- Max retention: **Configurable per department** (default: 7 days)
- Student specifies purpose and exact deadline
- Automatic return reminder: 1 day before deadline

**Deadline Policy (NEW):**
- Students specify: From Date-Time to To Date-Time
- Maximum retention time enforced (e.g., 7 days)
- Student responsible for exact return time
- Late return penalty: Points per day late

**Return Policy:**
- Borrowable: TO inspection at return (damage assessment)
- Lab-Dedicated: Instructor releases, TO cleans
- Immediate return required if Lab Session conflict

**Example:**
```
COURSEWORK REQUEST: CSE301-Assignment-3
├─ Student: John Doe (2023E001)
├─ Course: CS301 (Database Systems)
├─ Instructor Approval: Dr. Smith (signed)
├─ Equipment: Multimeter (1 unit) [BORROWABLE]
├─ From: Jan 20, 2026 at 10:00 AM
├─ To: Jan 22, 2026 at 5:00 PM (2.5 days max)
├─ Purpose: Analyze power consumption in DB server
├─ Status: ISSUED (ready to borrow)
└─ Late Return Penalty: 10 points per day over deadline
```

---

#### **Request Type 3: Research**

**Purpose:** Equipment for research projects (honors/graduate research, thesis work).

**Characteristics:**
- Longer retention periods (weeks to months)
- Supervisor oversight (academic advisor)
- Lecturer recommendation (optional but valued)
- Both equipment types eligible
- SLA: Research gets priority over coursework/personal

**Requestor:** Student with research project enrollment

**Approval Chain:**
```
Student submits request
    ↓
Supervisor reviews & APPROVES (mandatory)
    ↓
Lecturer recommends (optional, accelerates approval)
    ↓
HOD reviews (if >14 days retention requested)
    ↓
TO issues
```

**Duration Policy:**
- Student specifies: From Date-Time to To Date-Time
- No automatic semester limit
- Max retention: **Configurable per supervisor** (default: 30 days)
- Extension possible with supervisor authorization

**Return Policy:**
- Lab-Dedicated: Cannot borrow (not eligible)
- Borrowable: Full inspection at return
- Damage assessment triggers penalty calculation

**Example:**
```
RESEARCH REQUEST: CSE-THESIS-001
├─ Student: Jane Smith (2023G001)
├─ Research Title: Distributed Database Optimization
├─ Supervisor: Dr. Johnson (approved)
├─ Lecturer Note: Dr. Brown (recommends)
├─ Equipment: Network Analyzer (1) [BORROWABLE]
├─ From: Jan 15, 2026 at 9:00 AM
├─ To: Feb 15, 2026 at 5:00 PM (31 days)
├─ Status: APPROVED (28 day max, extension needed)
└─ Revised To: Feb 12, 2026 (30 days, within limit)
```

---

#### **Request Type 4: Extracurricular**

**Purpose:** Equipment for student clubs, competitions, innovation events.

**Characteristics:**
- HOD recommendation required
- Event-based timeline
- Instructor co-approval for student safety
- Borrowable equipment only (portable)
- Time-limited (event-specific)

**Requestor:** Student club representative or event coordinator

**Approval Chain:**
```
Student submits request
    ↓
Instructor recommends (safety oversight)
    ↓
HOD APPROVES (final decision)
    ↓
TO issues
```

**Duration Policy:**
- Event-specific: From event start to event end
- Max duration: **7 days** (configurable by HOD)
- No multi-day extensions (re-request required)

**Return Policy:**
- Immediate after event
- Damage inspection (event-related wear)
- Penalty for damage during event

**Example:**
```
EXTRACURRICULAR REQUEST: CSE-ROBOTICS-001
├─ Club: Robotics Club
├─ Event: IEEE Robotics Competition (Jan 25-27)
├─ Instructor: Dr. Kumar (recommends)
├─ HOD: Dr. Johnson (approves)
├─ Equipment: Power Supply (2) [BORROWABLE]
├─ From: Jan 25, 2026 at 8:00 AM
├─ To: Jan 27, 2026 at 6:00 PM
├─ Status: APPROVED
└─ Damage Noted: Minor scratch on unit 1 → 5 penalty points
```

---

#### **Request Type 5: Personal**

**Purpose:** Personal learning/experimentation projects (lowest priority).

**Characteristics:**
- HOD + Instructor co-approval required
- Borrowable equipment only
- Strictly limited (short-term only)
- Personal responsibility emphasized
- Deposit/collateral tracking

**Requestor:** Student (any status)

**Approval Chain:**
```
Student submits request
    ↓
Instructor APPROVES (verifies educational value)
    ↓
HOD APPROVES (verifies institutional safety)
    ↓
TO issues (with additional safeguards)
```

**Duration Policy:**
- Max: **3 days** (configurable by HOD)
- No extensions (return and re-request)
- Weekend/overnight allowed

**Return Policy:**
- Strict deadline (TO will follow up)
- Damage inspection (personal use assessed differently)
- Condition must match checkout state

**Example:**
```
PERSONAL REQUEST: STUDENT-PERSONAL-001
├─ Student: Alex Kumar (2023E045)
├─ Project: DIY Power Measurement Device
├─ Instructor: Dr. Smith (approves - educational merit)
├─ HOD: Dr. Johnson (approves)
├─ Equipment: Multimeter (1) [BORROWABLE]
├─ From: Jan 20, 2026 at 2:00 PM
├─ To: Jan 21, 2026 at 2:00 PM (24 hours max)
├─ Status: ISSUED
└─ Note: Personal use category - strict damage penalties
```

---

### 1.3 Temporal Model (New: From/To DateTime)

**All Requests Now Include:**

```
request {
  from_date: DATE (YYYY-MM-DD)
  from_time: TIME (HH:MM, 24-hour format)
  from_datetime: TIMESTAMP (calculated)
  
  to_date: DATE (YYYY-MM-DD)
  to_time: TIME (HH:MM, 24-hour format)
  to_datetime: TIMESTAMP (calculated)
  
  duration_hours: INTEGER (calculated = to_datetime - from_datetime)
  duration_days: DECIMAL (calculated = duration_hours / 24)
  
  status: ENUM {
    SUBMITTED,
    IN_REVIEW,
    APPROVED,
    ISSUED,
    IN_USE,
    RETURNED,
    OVERDUE,
    RETURNED_DAMAGED
  }
}
```

**Validation Rules:**

```
✓ from_datetime < to_datetime (to must be after from)
✓ from_datetime >= NOW() (cannot request past dates)
✓ to_datetime - from_datetime <= max_retention (per type)
✓ Equipment available during entire period
✓ No conflicts with Lab Session requests (immediate return rule)
✓ No double-booking of same equipment
```

---

## 📋 SECTION 2: REQUEST WORKFLOWS (DETAILED)

### 2.1 Lab Session Request Workflow (NEW - HIGHEST PRIORITY)

**Diagram:**

```
┌─────────────────────────────────────────────────────────────┐
│               LAB SESSION REQUEST WORKFLOW                  │
│                  (MANDATORY, PRIORITY 5)                    │
└─────────────────────────────────────────────────────────────┘

TIMELINE REQUIREMENT: ≥2 days (48 hours) before lab date

Day -2  : MUST SUBMIT REQUEST
  ↓
  Lab Instructor selects:
  ├─ Equipment needed (lab-dedicated only)
  ├─ Time slot (8-12 or 1-4 PM, fixed)
  ├─ Lab date (future weekday)
  ├─ Student count
  └─ Duration (auto-calculated per time slot)

  Validation:
  ├─ Equipment available during entire slot
  ├─ No Lab Session conflicts
  ├─ No Borrowable equipment conflicts
  └─ TO status check

Day -1  : COURSE LECTURER APPROVAL (AUTO-CONFIRM)
  ↓
  Course Lecturer verifies:
  ├─ Lab session is scheduled (matches course calendar)
  ├─ Equipment list is appropriate
  ├─ Student count reasonable
  └─ Co-signs request

  Status: APPROVED (auto-escalated)

Day 0   : HOD CONFIRMATION (AUTOMATIC)
  ↓
  HOD receives notification:
  ├─ Mandatory request type (no discretion)
  ├─ Resource reserved immediately
  ├─ TO receives HIGH PRIORITY flag
  └─ Status: RESERVED

Day 0 (AM): TO PREPARATION
  ↓
  TO prepares equipment:
  ├─ Verify all items available & functional
  ├─ Inspect for cleanliness/damage
  ├─ Transport to designated lab
  ├─ Setup testing (if applicable)
  └─ Status: READY

Day 0 (Lab Time): USAGE
  ├─ Lab Instructor receives equipment
  ├─ Students use for duration (3-4 hours fixed)
  └─ Return TO immediately at session end

Day 0 (After Lab): RETURN & INSPECTION
  ├─ Lab Instructor releases equipment to TO
  ├─ TO inspects condition
  │  ├─ ✓ Perfect/Excellent → Next session use OK
  │  ├─ ⚠ Minor damage → Assess, note damage
  │  └─ ✗ Severe damage → Quarantine, report
  ├─ Status: RETURNED
  └─ Penalty (if applicable): Assessed

OVERRIDE RULE - IMMEDIATE RETURN:
If another Lab Session request needs equipment:
├─ Current holder MUST return immediately
├─ Delay penalty applies automatically
├─ Equipment prioritized to next Lab Session
└─ User notified: "Equipment needed for priority lab session"

ERROR SCENARIOS:

❌ Submitted <48 hours before:
  └─ REJECTED - Cannot process
     Message: "Lab session requests require 48h advance notice"
     Suggestion: Contact HOD for emergency approval

❌ No equipment available:
  └─ REJECTED - Insufficient resources
     Message: "Equipment unavailable during requested time"
     Suggestion: Propose alternative date or equipment

❌ Cancellation <48 hours before:
  └─ PENALTY APPLIED - Breach of commitment
     Points: 20 (department configurable)
     Reason: Wasted preparation & TO time
```

---

### 2.2 Coursework Request Workflow (UPDATED)

**Diagram:**

```
┌─────────────────────────────────────────────────────────────┐
│              COURSEWORK REQUEST WORKFLOW                    │
│                  (PRIORITY 3)                               │
└─────────────────────────────────────────────────────────────┘

STEP 1: STUDENT SUBMITS REQUEST

Student specifies:
├─ Course enrolled in
├─ Equipment type:
│  ├─ Lab-Dedicated: Time slot (8-12 or 1-4), date, duration
│  └─ Borrowable: From date-time, to date-time (max X days)
├─ Quantity & specific items
├─ Purpose (assignment description)
├─ From date-time
├─ To date-time
└─ MAX retention enforced (configurable, e.g., 7 days)

Validation:
├─ Equipment available during entire period
├─ No Lab Session conflicts (immediate return rule)
├─ Retention within max limit
├─ Equipment type matches course usage pattern
└─ Student has no overdue items

STEP 2: INSTRUCTOR SIGN-OFF (MANDATORY)

Instructor verifies:
├─ Assignment is legitimate (verified against course syllabus)
├─ Equipment request is proportionate
├─ Duration is reasonable
├─ Confirms student competence for equipment use
└─ APPROVES or REJECTS

Instructor can:
├─ APPROVE → Proceed to TO
├─ REJECT → Student receives feedback
├─ REQUEST MODIFICATION → Adjust quantities/dates
└─ Auto-approval if conditions met (configurable)

STEP 3: EQUIPMENT TYPE-SPECIFIC PROCESSING

IF LAB-DEDICATED:
  ├─ TO schedules specific time slot
  ├─ Instructor gets lab room assignment
  ├─ Equipment transported to lab
  ├─ Student uses during assigned slot
  └─ Auto-return after 4 hours max

IF BORROWABLE:
  ├─ TO prepares equipment immediately
  ├─ Creates issue ticket with from/to dates
  ├─ Student signs equipment checkout sheet
  ├─ Equipment given to student
  └─ Return date tracked (automatic reminder)

STEP 4: EQUIPMENT USAGE & RETURN

FOR BORROWABLE:
  ├─ Student has until TO date-time
  ├─ Automatic SMS/email reminder: 1 day before
  ├─ Late return: Penalty points accumulated
  ├─ TO inspection upon return
  │  ├─ Condition verified
  │  ├─ Damage assessed & recorded
  │  └─ Penalty calculated if damaged
  └─ Status: RETURNED (or OVERDUE if late)

FOR LAB-DEDICATED:
  ├─ Session ends at slot time (3-4 hours)
  ├─ Equipment returns to lab storage
  ├─ TO collects & inspects immediately
  ├─ Ready for next Lab Session request
  └─ Status: RETURNED

MAXIMUM RETENTION POLICY (NEW):

Default (per department):
├─ CSE: Max 7 days per coursework request
└─ EEE: Max 10 days per coursework request

Configuration:
├─ Department Coordinator can adjust
├─ Course-specific overrides possible
└─ Auto-enforcement at system level

Validation at Submission:
├─ System checks: (to_datetime - from_datetime) / 24 ≤ max_days
├─ If exceeds: Error message with max limit
├─ Student can adjust dates & resubmit
└─ Override by instructor (with HOD approval)

PENALTIES FOR COURSEWORK:

Late Return: 5 points per day
├─ Calculated as: CEILING((return_actual - to_datetime) / 86400) × 5
├─ Example: 2 days 6 hours late = 15 points
└─ Automatic deduction from student record

Damage Assessment: See Penalty System (Section 3.2)

Lab Session Override:
├─ If Lab Session needs equipment before coursework return date
├─ Coursework request CANCELLED, equipment returned immediately
├─ Late return penalty NOT applied
├─ Student notified: "Lab session priority - equipment recalled"
├─ Lab Session gets equipment, coursework adjusted
└─ No penalty for recall (system priority)
```

---

### 2.3 Research Request Workflow (UPDATED)

**Diagram:**

```
┌─────────────────────────────────────────────────────────────┐
│               RESEARCH REQUEST WORKFLOW                     │
│                  (PRIORITY 4)                               │
└─────────────────────────────────────────────────────────────┘

STUDENT SUBMITS REQUEST

Student provides:
├─ Research title & description
├─ Supervisor name & department
├─ Equipment needs (borrowable only)
├─ From date-time (project start)
├─ To date-time (project end)
├─ Expected total duration
└─ Justification for equipment use

Validation:
├─ Supervisor exists & confirmed
├─ Equipment available during period
├─ No Lab Session conflicts
├─ Retention ≤ supervisor max (default: 30 days)
└─ Reasonable for research purpose

SUPERVISOR REVIEW (MANDATORY)

Supervisor verifies:
├─ Student is enrolled in research project
├─ Equipment request is scientifically justified
├─ Duration is reasonable (not excessive)
├─ Lab safety clearance confirmed
└─ APPROVES or REJECTS

If APPROVED:
├─ Supervisor signature added
├─ Request auto-escalates to TO
└─ Status: APPROVED

LECTURER RECOMMENDATION (OPTIONAL - ACCELERATES)

Lecturer (if student's coursework instructor):
├─ Can add recommendation (not required)
├─ Accelerates processing (fast-tracked)
├─ Lecturer confidence enhances approval
└─ Note: Lecturer role is advisory only

HOD REVIEW (IF >14 DAYS)

If to_datetime - from_datetime > 14 days:
├─ Request escalated to HOD
├─ HOD verifies resource allocation
├─ HOD can:
│  ├─ APPROVE → Proceed to TO
│  ├─ REQUEST MODIFICATION → Reduce duration
│  └─ REJECT → Student appeals to Supervisor
└─ Status: HOD_APPROVED (or adjusted)

TO ISSUANCE

TO prepares equipment:
├─ Verify all items functional & clean
├─ Create research project equipment log
├─ Student signs research use agreement
├─ Equipment provided with instructions
└─ Status: ISSUED

RESEARCH DURATION MANAGEMENT

Student can request extension:
├─ Submit extension request before to_datetime
├─ Supervisor must re-approve
├─ Max total duration: 60 days (configurable)
├─ Extensions add 10 more days per request
└─ Max extensions: 2 per research project

Return Process:
├─ Student returns by to_datetime
├─ TO inspection (full condition check)
├─ Damage assessment & penalties applied
└─ Status: RETURNED

PENALTIES FOR RESEARCH:

Late Return: 5 points per day
├─ Research typically allows flexibility
├─ But strict deadline still enforced
└─ Late penalties same as coursework

Lab Session Override:
├─ If Lab Session needs equipment
├─ Research request SUSPENDED (not cancelled)
├─ Equipment temporarily recalled
├─ Coursework/Extracurricular hold
├─ Student notified: "Equipment temporarily unavailable for lab session"
├─ Return date EXTENDED by lab duration
└─ No penalty for recall
```

---

### 2.4 Extracurricular Request Workflow

**Diagram:**

```
┌─────────────────────────────────────────────────────────────┐
│           EXTRACURRICULAR REQUEST WORKFLOW                  │
│                  (PRIORITY 2)                               │
└─────────────────────────────────────────────────────────────┘

STUDENT CLUB SUBMITS REQUEST

Club representative provides:
├─ Event name & description
├─ Event dates (from-to with times)
├─ Equipment list (borrowable only, portable)
├─ Participant count
├─ Venue (on-campus or off-campus)
└─ Justification (educational value)

Validation:
├─ Club officially registered
├─ Representative authorized to request
├─ Event within semester calendar
├─ Equipment appropriate for event
└─ Max duration: 7 days enforced

INSTRUCTOR RECOMMENDATION (MANDATORY - SAFETY)

Instructor (faculty advisor):
├─ Reviews event details
├─ Verifies safety measures
├─ Confirms participant supervision
├─ Equipment use is appropriate
└─ Approves or rejects

If REJECTED:
├─ Feedback provided to club
├─ Can resubmit with modifications
└─ HOD can override (if needed)

HOD APPROVAL (FINAL)

HOD makes final decision:
├─ Verifies resource availability
├─ Checks department policy alignment
├─ Reviews student impact
└─ APPROVES, REQUESTS MODIFICATION, or REJECTS

If APPROVED:
├─ Event confirmed with TO
├─ Equipment reserved with HIGH flag
└─ Status: APPROVED_EXTRACURRICULAR

TO PREPARATION

TO prepares equipment:
├─ Special handling for event use
├─ Protective cases/padding (if needed)
├─ Usage instructions provided
├─ Damage expectations set
└─ Sign-off sheet prepared

DURING EVENT

Club responsible for:
├─ Equipment security
├─ Appropriate use
├─ Damage prevention
└─ Insurance coverage (if applicable)

RETURN & DAMAGE ASSESSMENT

Immediate return after event:
├─ Equipment brought back to TO
├─ TO inspection for event-related wear
├─ Damage categorized:
│  ├─ Normal wear → No penalty
│  ├─ Minor damage → Assess value
│  └─ Major damage → Investigation
├─ TO documentation in photos
└─ Status: RETURNED

PENALTIES FOR EXTRACURRICULAR:

Damage penalties apply (see Section 3.2)
└─ Event-related damage: 10-50 points (severity-based)

Late Return:
├─ Strict: 10 points per day
├─ Events should return immediately
└─ No flexibility (event fixed end date)
```

---

### 2.5 Personal Request Workflow

**Diagram:**

```
┌─────────────────────────────────────────────────────────────┐
│                PERSONAL REQUEST WORKFLOW                    │
│                  (PRIORITY 1 - LOWEST)                      │
└─────────────────────────────────────────────────────────────┘

STUDENT SUBMITS REQUEST

Student provides:
├─ Project description
├─ Educational value justification
├─ Equipment needs (borrowable only)
├─ From date-time (max 72 hours total)
├─ To date-time (max 3 days)
└─ Personal use agreement

Validation:
├─ Borrowable equipment only
├─ Duration ≤ 3 days (strict)
├─ Student has clean record (no pending penalties)
├─ No Lab Session conflicts
└─ Educational merit assessment

INSTRUCTOR APPROVAL (MANDATORY)

Instructor verifies:
├─ Personal project has educational value
├─ Student is competent to use equipment
├─ Safety considerations addressed
├─ Approves or rejects

Instructor can reject if:
├─ Not educational (recreational only)
├─ Student safety risk
├─ Equipment not appropriate for personal use
└─ Student has prior damage incidents

HOD APPROVAL (FINAL - HIGHEST SCRUTINY)

HOD reviews for:
├─ Institutional risk assessment
├─ Insurance/liability concerns
├─ Resource allocation justification
├─ Student record history
└─ Approves or rejects

HOD can reject if:
├─ Student has history of damage
├─ Equipment high-value/sensitive
├─ Risk deemed unacceptable
└─ No educational purpose identified

TO ISSUANCE (CAUTIOUS)

TO prepares with extra safeguards:
├─ Equipment condition documented (photos)
├─ Special usage instructions provided
├─ Damage expectations clearly stated
├─ Deposit or insurance information
├─ Student signs liability waiver
└─ Status: ISSUED_PERSONAL_USE

USAGE PERIOD (3 DAYS MAX)

Strict deadline enforcement:
├─ No extensions allowed
├─ Automatic return tracking
├─ Reminder at 12 hours before deadline
└─ Late return triggers immediate penalties

RETURN & ASSESSMENT

Immediate return required:
├─ TO inspection (very thorough)
├─ Any damage or wear noted
├─ Assessment prioritizes personal use context
├─ Damage penalty calculated
└─ Status: RETURNED

PENALTIES FOR PERSONAL:

Late Return: 20 points per day
├─ Highest penalty rate (personal category)
├─ After 48h late: Escalate to HOD (potential suspension)
└─ 72h late: System auto-deactivates personal requests

Damage Penalties: DOUBLED (see Section 3.2)
├─ Personal use damage: 2× severity penalty
├─ Reasoning: Less supervision, personal liability
└─ Example: Minor damage = 10 → 20 points

Student Suspension:
├─ 3 damage incidents in personal category
└─ Personal requests BLOCKED for 1 semester
```

---

## 🔧 SECTION 3: PENALTY SYSTEM (REDESIGNED)

### 3.1 Penalty Point System Overview

**Calculation Model:**

```
Total Penalty Points = Late Return Penalty + Damage Penalty + Override Penalty

Where:
├─ Late Return Penalty = (days_late × rate_per_day)
├─ Damage Penalty = (damage_level × item_value × multiplier)
└─ Override Penalty = Priority override incidents
```

---

### 3.2 Late Return Penalty (Per Day Overdue)

**Formula:**

```
Late Return Points = CEILING(hours_late / 24) × rate_per_day

Where:
  hours_late = (actual_return_datetime - to_datetime) × 3600 seconds
  CEILING = rounds up (2.1 hours late = 1 day)
  rate_per_day = varies by request type
```

**Rate by Request Type:**

| Request Type | Rate/Day | Rationale |
|--------------|----------|-----------|
| Lab Session | 30 | Highest priority - disrupts scheduled labs |
| Research | 5 | Flexible, but deadline still matters |
| Coursework | 5 | Standard educational use |
| Extracurricular | 10 | Event-bound, strict timing |
| Personal | 20 | Least priority, strict discipline |

**Calculation Examples:**

```
Example 1: Coursework, 2 days 6 hours late
├─ hours_late = 54 hours
├─ CEILING(54 / 24) = 3 days
├─ Points = 3 × 5 = 15 points

Example 2: Personal, 18 hours late
├─ hours_late = 18 hours
├─ CEILING(18 / 24) = 1 day
├─ Points = 1 × 20 = 20 points

Example 3: Lab Session, 0.5 hours late
├─ hours_late = 0.5 hours
├─ CEILING(0.5 / 24) = 1 day
├─ Points = 1 × 30 = 30 points (even 30 min = full day penalty)
```

---

### 3.3 Damage Penalty (Assessed by TO)

**Damage Assessment Levels:**

| Level | Category | Description | Item Value Impact | Points Formula |
|-------|----------|-------------|------------------|-----------------|
| 0 | None | No damage, perfect condition | 0% | 0 |
| 1 | Minimal | Cosmetic scratches, minor wear | <5% | value × 0.10 |
| 2 | Minor | Small dents, minor functional impact | 5-15% | value × 0.25 |
| 3 | Moderate | Significant damage, affects function | 15-30% | value × 0.50 |
| 4 | Severe | Major damage, partially functional | 30-60% | value × 0.75 |
| 5 | Critical | Non-functional, requires major repair | 60-100% | value × 1.00 |

**Calculation:**

```
Damage Points = damage_level × (item_value_points / 100) × multiplier

Where:
  item_value_points = Equipment value converted to penalty scale (1-100)
  multiplier = varies by request type

Example Equipment Values:
├─ Multimeter ($50) = 5 points
├─ Oscilloscope ($500) = 50 points
├─ Power Supply ($200) = 20 points
└─ Network Analyzer ($2000) = 100 points (max)
```

**Multipliers by Request Type:**

```
Lab Session: 1.5× (lab setting, more users, harder to assess)
Research: 1.0× (controlled research environment)
Coursework: 1.0× (educational use, expected)
Extracurricular: 1.2× (event risk, less supervised)
Personal: 2.0× (personal use, higher liability)
```

**Calculation Examples:**

```
Example 1: Multimeter (value=5) Moderate damage (level 3) in Coursework
├─ Points = 3 × 5 × 1.0 = 15 points

Example 2: Oscilloscope (value=50) Minor damage (level 2) in Personal
├─ Points = 2 × 50 × 2.0 = 200 points
├─ Note: Personal use multiplier doubles damage points

Example 3: Power Supply (value=20) Critical damage (level 5) in Lab Session
├─ Points = 5 × 20 × 1.5 = 150 points
├─ Note: Lab setting, multiple users make damage assessment harder
```

---

### 3.4 Lab Session Override Penalty

**When Applied:**

If Lab Session request needs equipment currently borrowed/used:
- Equipment MUST be returned immediately
- Overriding request type (Late Coursework/Research/Personal)

**Penalty If NOT Returned Immediately:**

```
Override Penalty = 50 + (hours_delay × 5)

Where:
  hours_delay = time between override notification and actual return
```

**Examples:**

```
Example 1: Coursework holder receives "Return immediately" at 10:00 AM
├─ Returns at 10:15 AM (15 minutes delay)
├─ Penalty = 50 + (0.25 × 5) = 51.25 ≈ 51 points

Example 2: Research holder receives notification at 2:00 PM
├─ Returns at 4:30 PM (2.5 hours delay)
├─ Penalty = 50 + (2.5 × 5) = 62.5 ≈ 63 points
```

---

### 3.5 Penalty Point Consequences

**Student Penalty Account Management:**

```
0-10 points    → Warning (system notification)
11-30 points   → Caution (email to student + advisor)
31-50 points   → Serious Warning (email to HOD)
51-100 points  → Restricted (Personal requests blocked)
101-200 points → Suspended (All non-coursework requests blocked)
>200 points    → Escalated (HOD + Director review, possible expulsion)
```

**Actions by Penalty Level:**

| Points | Action | Duration | Recovery |
|--------|--------|----------|----------|
| 0-10 | Warning notification | Until next semester | Automatic |
| 11-30 | Caution, advisor notified | Current semester + 1 | Good behavior reset |
| 31-50 | HOD notified, monitoring | Current + 2 semesters | Good behavior |
| 51-100 | Personal requests blocked | Current + 2 semesters | Good behavior |
| 101-200 | All requests blocked except coursework | Current + 3 semesters | HOD approval |
| >200 | Case review, possible expulsion | Until resolved | Formal appeal process |

**Penalty Reset Policy:**

```
Good Behavior Reset:
├─ If student has <10 points current semester
├─ AND no violations in previous semester
└─ Then: 10% of accumulated points cleared per semester

Examples:
├─ 50 points (semester 1) → No violations → 45 points (semester 2) → 40 points (semester 3)
└─ 100 points → 3 semesters good behavior → ~73 points remaining
```

---

### 3.6 Penalty Appeal Process

**Student Appeal:**

```
Student can appeal penalty within 7 days of assessment:

1. Submit appeal form with justification
2. Appeal goes to Department Coordinator
3. Coordinator reviews:
   ├─ Damage assessment validity
   ├─ Circumstances (force majeure, equipment malfunction)
   ├─ Student record history
   └─ Makes decision
4. Decision: UPHELD, REDUCED, or WAIVED

Examples of Appeal Success:
├─ Equipment malfunction caused damage → Potential waive
├─ TO handling caused damage → Reduced/waived
├─ Force majeure (fire, theft) → Waived
├─ Miscalculation in to_datetime → Reduced
└─ Student negligence → Upheld
```

---

## 📊 SECTION 4: INSTRUCTOR REQUIREMENTS (NEW)

### 4.1 Mandatory Instructor Sign-off

**All Request Types Require Instructor Approval:**

| Request Type | Instructor Role | Authority | Timeline |
|-------------|-----------------|-----------|----------|
| Lab Session | Lab Instructor + Course Lecturer | Both must co-sign | Before request |
| Coursework | Course Lecturer | Approves quantity & dates | Within 24h of submission |
| Research | Academic Advisor/Supervisor | Mandates approval | Within 48h of submission |
| Extracurricular | Faculty Advisor | Safety verification | Within 24h of submission |
| Personal | Any Department Instructor | Educational merit assessment | Within 48h of submission |

**Instructor Approval Workflow:**

```
1. Instructor receives notification of pending request
2. Instructor logs into dashboard
3. Reviews request details:
   ├─ Equipment list
   ├─ From/To dates-times
   ├─ Student justification
   └─ Purpose
4. Instructor can:
   ├─ APPROVE → Proceeds to TO
   ├─ REQUEST MODIFICATION → Specify changes
   ├─ REJECT → Provide feedback
   └─ ADD NOTES → Additional context for TO
5. System auto-escalates based on type
```

**Auto-Approval Criteria (By Request Type):**

Lab Session:
├─ NO auto-approval (always requires explicit approval)
└─ Both instructors must actively approve

Coursework:
├─ Can auto-approve if:
│  ├─ Equipment available
│  ├─ Student grade ≥ C (in course)
│  ├─ No pending penalties
│  ├─ Retention within limits
│  └─ Instructor pre-authorized auto-approval in settings
└─ Default: Manual review (conservative)

Research:
├─ NO auto-approval (supervisor must verify)
└─ Always manual review

Extracurricular:
├─ NO auto-approval (HOD must verify)
└─ Always manual review

Personal:
├─ NO auto-approval (requires explicit approvals)
└─ Both instructor + HOD must approve
```

---

### 4.2 Instructor Dashboard (NEW)

**Instructor can see:**

```
┌─────────────────────────────────────┐
│   INSTRUCTOR EQUIPMENT DASHBOARD    │
├─────────────────────────────────────┤
│                                     │
│ PENDING APPROVALS: 5                │
│ ├─ Lab Sessions: 2 (URGENT)        │
│ ├─ Coursework: 2                    │
│ └─ Personal: 1                      │
│                                     │
│ MY LAB SESSIONS (next 2 weeks)      │
│ ├─ CS301-Lab-W3 (Jan 22) → Equipment Reserved ✓
│ ├─ CS301-Lab-W4 (Jan 29) → Pending Submission
│ └─ EEE302-Lab-W3 (Jan 25) → Equipment Available
│                                     │
│ COURSE REQUESTS (this semester)     │
│ ├─ CS301: 8 coursework requests     │
│ ├─ CS401: 3 coursework requests     │
│ └─ EEE101: 5 coursework requests    │
│                                     │
│ STUDENTS WITH PENALTIES             │
│ ├─ John Doe: 45 points → Caution    │
│ ├─ Jane Smith: 18 points → Warning  │
│ └─ Alex Kumar: 2 points → OK        │
│                                     │
└─────────────────────────────────────┘
```

---

## 💾 SECTION 5: DATABASE SCHEMA (UPDATED)

### 5.1 Request Type Enhancements

**New/Updated Tables:**

```sql
-- UPDATED: requests table
CREATE TABLE requests (
  id VARCHAR(50) PRIMARY KEY,  -- REQ-2026-001
  request_type VARCHAR(30) NOT NULL,  
    -- NEW ENUM: 'LAB_SESSION', 'COURSEWORK', 'RESEARCH', 'EXTRACURRICULAR', 'PERSONAL'
  equipment_type VARCHAR(30) NOT NULL,  
    -- ENUM: 'LAB_DEDICATED', 'BORROWABLE'
  
  -- TEMPORAL MODEL (NEW)
  from_date DATE NOT NULL,
  from_time TIME NOT NULL,
  from_datetime TIMESTAMP NOT NULL,
  
  to_date DATE NOT NULL,
  to_time TIME NOT NULL,
  to_datetime TIMESTAMP NOT NULL,
  
  duration_hours DECIMAL(10,2),  -- Calculated
  duration_days DECIMAL(10,2),   -- Calculated
  
  max_retention_days INTEGER,  -- Configurable per type
  
  -- INSTRUCTOR APPROVALS (NEW)
  instructor_id INTEGER REFERENCES users(id),  -- Primary instructor
  co_instructor_id INTEGER REFERENCES users(id),  -- For lab sessions
  instructor_approved_at TIMESTAMP,
  instructor_approval_status VARCHAR(20),  -- 'PENDING', 'APPROVED', 'REJECTED', 'MODIFIED'
  instructor_notes TEXT,
  
  -- STANDARD FIELDS
  student_id INTEGER REFERENCES users(id) NOT NULL,
  course_id VARCHAR(20),
  status VARCHAR(30) DEFAULT 'SUBMITTED',
  priority_level INTEGER,  -- 5=Lab Session (highest), 1=Personal (lowest)
  
  submitted_at TIMESTAMP DEFAULT NOW(),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  
  INDEX idx_request_type (request_type),
  INDEX idx_from_to_dates (from_datetime, to_datetime),
  INDEX idx_instructor (instructor_id),
  UNIQUE(id)
);

-- NEW: request_approvals table (tracks all approval decisions)
CREATE TABLE request_approvals (
  id SERIAL PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  approver_role VARCHAR(30) NOT NULL,  -- 'INSTRUCTOR', 'HOD', 'TO', 'SUPERVISOR'
  approver_id INTEGER REFERENCES users(id) NOT NULL,
  decision VARCHAR(20) NOT NULL,  -- 'APPROVE', 'REJECT', 'MODIFY_REQUEST', 'RECOMMEND'
  decision_comments TEXT,
  decided_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_request_id (request_id),
  INDEX idx_approver (approver_id, approver_role)
);

-- NEW: penalty_assessments table
CREATE TABLE penalty_assessments (
  id SERIAL PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  student_id INTEGER REFERENCES users(id) NOT NULL,
  penalty_type VARCHAR(30) NOT NULL,  
    -- 'LATE_RETURN', 'DAMAGE', 'OVERRIDE', 'CANCELLATION'
  
  penalty_points INTEGER NOT NULL,
  
  -- Damage-specific fields
  equipment_id INTEGER REFERENCES equipment(id),
  damage_level INTEGER (0-5),  -- 0=None, 5=Critical
  equipment_value DECIMAL(10,2),
  damage_description TEXT,
  damage_photo_url VARCHAR(255),
  
  -- Late return-specific fields
  hours_late DECIMAL(10,2),
  rate_per_day INTEGER,
  
  -- Assessment details
  assessed_by INTEGER REFERENCES users(id),  -- TO staff
  assessed_at TIMESTAMP DEFAULT NOW(),
  appeal_status VARCHAR(20) DEFAULT 'NONE',  -- 'NONE', 'PENDING', 'UPHELD', 'REDUCED', 'WAIVED'
  appeal_comments TEXT,
  
  created_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_student (student_id),
  INDEX idx_request (request_id),
  INDEX idx_penalty_type (penalty_type)
);

-- NEW: equipment_availability table
CREATE TABLE equipment_availability (
  id SERIAL PRIMARY KEY,
  equipment_id INTEGER REFERENCES equipment(id) NOT NULL,
  from_datetime TIMESTAMP NOT NULL,
  to_datetime TIMESTAMP NOT NULL,
  request_id VARCHAR(50) REFERENCES requests(id),
  status VARCHAR(20),  -- 'AVAILABLE', 'RESERVED', 'IN_USE', 'MAINTENANCE'
  INDEX idx_equipment (equipment_id),
  INDEX idx_dates (from_datetime, to_datetime)
);

-- NEW: lab_sessions table (structured lab session management)
CREATE TABLE lab_sessions (
  id VARCHAR(50) PRIMARY KEY,  -- CS301-Lab-W3
  course_id VARCHAR(20) NOT NULL,
  lab_instructor_id INTEGER REFERENCES users(id) NOT NULL,
  course_lecturer_id INTEGER REFERENCES users(id) NOT NULL,
  
  session_date DATE NOT NULL,
  time_slot VARCHAR(10) NOT NULL,  -- '08-12' or '13-16' (1-4 PM)
  duration_hours INTEGER,
  student_count INTEGER,
  
  request_id VARCHAR(50) REFERENCES requests(id),  -- Links to LAB_SESSION request
  status VARCHAR(20) DEFAULT 'SCHEDULED',
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  
  INDEX idx_course (course_id),
  INDEX idx_session_date (session_date)
);

-- NEW: instructor_approvals table (track approvals by instructors)
CREATE TABLE instructor_approvals (
  id SERIAL PRIMARY KEY,
  request_id VARCHAR(50) REFERENCES requests(id) NOT NULL,
  instructor_id INTEGER REFERENCES users(id) NOT NULL,
  instructor_role VARCHAR(30),  -- 'LAB_INSTRUCTOR', 'COURSE_LECTURER', 'ADVISOR'
  decision VARCHAR(20) NOT NULL,  -- 'APPROVED', 'REJECTED', 'MODIFICATION_REQUESTED'
  notes TEXT,
  decided_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_request_id (request_id),
  INDEX idx_instructor (instructor_id)
);

-- NEW: student_penalties table (cumulative penalty tracking)
CREATE TABLE student_penalties (
  id SERIAL PRIMARY KEY,
  student_id INTEGER REFERENCES users(id) NOT NULL UNIQUE,
  total_points INTEGER DEFAULT 0,
  current_status VARCHAR(20) DEFAULT 'GOOD',  
    -- 'GOOD', 'WARNING', 'CAUTION', 'SERIOUS', 'RESTRICTED', 'SUSPENDED'
  last_violation_date TIMESTAMP,
  last_reset_date TIMESTAMP,
  active_restrictions VARCHAR(255),  -- JSON: which request types blocked
  updated_at TIMESTAMP DEFAULT NOW(),
  INDEX idx_student (student_id),
  INDEX idx_status (current_status)
);
```

---

## 🔄 SECTION 6: WORKFLOW DIAGRAMS & RULES

### 6.1 Lab Session Override Rule (Critical)

**Immediate Return Rule for Lab Sessions:**

```
┌──────────────────────────────────────────────────┐
│  LAB SESSION OVERRIDE - IMMEDIATE RETURN         │
│  (All Other Requests Must Yield)                 │
└──────────────────────────────────────────────────┘

TRIGGER: Lab Session request approved + needs equipment

SCENARIO:
├─ Coursework request: Student borrowed multimeter (due Jan 25)
├─ Lab Session created: Lab on Jan 23, needs multimeter
└─ Lab Session approved: Equipment prioritized immediately

ACTION:
1. System sends notification to Coursework student:
   "Equipment needed for lab session on Jan 23.
    Please return by Jan 23 @ 8:00 AM"

2. Coursework student has until 8:00 AM Jan 23 to return

3. If returned ON TIME:
   ├─ Equipment transferred to lab
   ├─ Lab session proceeds
   └─ No penalty to coursework student

4. If NOT returned by 8:00 AM:
   ├─ Equipment retrieved by TO (forced return)
   ├─ Lab session proceeds
   ├─ Coursework student receives override penalty
   │  └─ 50 + (delay_hours × 5)
   └─ Coursework request marked CANCELLED

5. If equipment not available by LAB TIME:
   ├─ Lab session CANCELLED
   ├─ Lab instructor notified immediately
   ├─ HOD escalation
   └─ Alternative equipment sourced OR reschedule lab

IMPORTANT:
├─ Lab Session ALWAYS takes priority
├─ Coursework/Research/Extracurricular/Personal all yield
├─ NO exceptions or appeals during lab session override
└─ Student penalized for late return during override
```

---

### 6.2 Conflict Detection Rules

**System Prevents Overlapping Requests:**

```
RULE 1: Equipment Availability Conflict
├─ New request: from_datetime to to_datetime
├─ Check existing approved/in-use requests for same equipment
├─ If overlap detected:
│  ├─ Error: "Equipment unavailable during period"
│  ├─ Show available time slots
│  └─ Suggest alternative dates/equipment
└─ Validation FAILS

RULE 2: Lab Session Absolute Priority
├─ Lab Session request always reserves equipment
├─ Even partially overlapping Coursework requests FAIL
├─ Existing Coursework request during Lab time:
│  ├─ Auto-cancelled (override rule)
│  ├─ Student notified
│  └─ No penalty to student (system override, not their fault)
└─ Lab Session gets equipment

RULE 3: Lab-Dedicated Equipment Cannot Be Borrowed
├─ Borrowable request submitted for Lab-Dedicated item:
│  ├─ Error: "This equipment is lab-dedicated only"
│  ├─ Suggestion: "Use equipment in lab during scheduled sessions"
│  └─ Validation FAILS
└─ Enforces equipment type integrity

RULE 4: Max Retention Enforcement
├─ Coursework request: to_datetime - from_datetime > 7 days
├─ Error: "Retention period exceeds 7-day limit"
├─ Student adjusts dates to fit within max
└─ Validation FAILS until corrected

RULE 5: Lab Session 48-Hour Advance Notice
├─ Lab Session request submitted < 48 hours before lab date
├─ Error: "Lab session requests require 48-hour advance notice"
├─ Suggestion: "Contact HOD for emergency approval"
└─ Validation FAILS (unless HOD override)
```

---

## 📱 SECTION 7: UI/UX REQUIREMENTS (UPDATED)

### 7.1 Student Request Submission Form (NEW)

**Multi-Step Request Creation Wizard:**

```
┌─────────────────────────────────────────────────────┐
│         EQUIPMENT REQUEST WIZARD (NEW)              │
├─────────────────────────────────────────────────────┤
│                                                     │
│ STEP 1: SELECT REQUEST TYPE                        │
│                                                     │
│ ( ) Lab Session                                     │
│     For mandatory course lab sessions              │
│     Highest priority, auto-reserves equipment     │
│     [Learn more ▼]                                 │
│                                                     │
│ (•) Coursework                                     │
│     For course assignments & practicals           │
│     Standard processing, instructor approval     │
│     [Learn more ▼]                                │
│                                                     │
│ ( ) Research                                       │
│     For thesis/research projects                   │
│     Supervisor approval required                  │
│     [Learn more ▼]                                 │
│                                                     │
│ ( ) Extracurricular                               │
│     For club events & competitions                │
│     HOD + Instructor approval required            │
│     [Learn more ▼]                                 │
│                                                     │
│ ( ) Personal                                       │
│     For personal learning (lowest priority)       │
│     HOD + Instructor approval, max 3 days        │
│     [Learn more ▼]                                │
│                                                     │
│                  [Next] [Cancel]                  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**STEP 2: SELECT EQUIPMENT & DATES**

```
┌─────────────────────────────────────────────────────┐
│     EQUIPMENT & DATES (Step 2 of 4)                │
├─────────────────────────────────────────────────────┤
│                                                     │
│ EQUIPMENT SELECTION:                               │
│ [Search: _______________] [Browse Categories ▼]  │
│                                                     │
│ Selected Equipment:                                │
│ ┌─────────────────────────────────────────────┐  │
│ │ ☑ Multimeter (1 unit)                       │  │
│ │   Available: 3 units                        │  │
│ │   Type: Borrowable                          │  │
│ │   Max Retention: 7 days                     │  │
│ │ [Remove]                                    │  │
│ └─────────────────────────────────────────────┘  │
│ [Add More Equipment]                              │
│                                                     │
│ DATE & TIME SELECTION:                            │
│                                                     │
│ From Date: [Jan 20, 2026 ▼]                      │
│ From Time: [14:00 (2:00 PM) ▼]                   │
│                                                     │
│ To Date: [Jan 22, 2026 ▼]                        │
│ To Time: [17:00 (5:00 PM) ▼]                     │
│                                                     │
│ Duration: 2 days 3 hours                         │
│ ✓ Within max retention (7 days)                  │
│ ✓ Equipment available during period              │
│                                                     │
│               [← Back] [Next]                     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**STEP 3: PURPOSE & APPROVAL**

```
┌─────────────────────────────────────────────────────┐
│        PURPOSE & APPROVAL (Step 3 of 4)            │
├─────────────────────────────────────────────────────┤
│                                                     │
│ PURPOSE DESCRIPTION:                               │
│ ┌─────────────────────────────────────────────┐  │
│ │ Analyze power consumption in database       │  │
│ │ server for CS301 Assignment 3               │  │
│ │                                             │  │
│ │ [Character count: 65/500]                   │  │
│ └─────────────────────────────────────────────┘  │
│                                                     │
│ INSTRUCTOR ASSIGNMENT:                            │
│ [Select Instructor: Dr. Smith ▼]                 │
│   Your primary course instructor will            │
│   review & approve this request                  │
│                                                     │
│ ⚠ Instructor Must Approve:                       │
│   Requests cannot proceed without                │
│   instructor authorization                       │
│                                                     │
│ ESTIMATED TIMELINE:                               │
│ ├─ Submitted: Today                              │
│ ├─ Instructor Review: 24 hours                   │
│ ├─ TO Processing: 2-4 hours                      │
│ └─ Ready by: Tomorrow afternoon                  │
│                                                     │
│               [← Back] [Next]                     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**STEP 4: REVIEW & SUBMIT**

```
┌─────────────────────────────────────────────────────┐
│      REVIEW & SUBMIT (Step 4 of 4)                │
├─────────────────────────────────────────────────────┤
│                                                     │
│ REQUEST SUMMARY:                                   │
│                                                     │
│ Request Type: COURSEWORK                          │
│ Course: CS301 (Database Systems)                  │
│ Equipment: Multimeter (1 unit)                    │
│ From: Jan 20, 2026 @ 2:00 PM                     │
│ To: Jan 22, 2026 @ 5:00 PM                       │
│ Duration: 2 days 3 hours                         │
│ Instructor: Dr. Smith (pending approval)         │
│ Purpose: Analyze power consumption...            │
│                                                     │
│ SUBMISSION POLICY AGREEMENT:                      │
│ ☑ I understand equipment must be                 │
│   returned by the specified date-time            │
│ ☑ Late returns will incur penalty points        │
│ ☑ I agree to handle equipment carefully         │
│ ☑ I will report any damage immediately          │
│                                                     │
│ ⚠ PENALTIES:                                      │
│ Late return: 5 points per day                    │
│ Damage: Assessed upon return                     │
│ Lab session override: May cancel request         │
│                                                     │
│               [← Back] [Submit Request]          │
│                                                     │
│ * Your instructor will receive notification     │
│ * DO NOT take equipment until instructor approves │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

### 7.2 Lab Session Request Form (For Instructors)

```
┌─────────────────────────────────────────────────────┐
│      LAB SESSION REQUEST (Instructor Only)         │
├─────────────────────────────────────────────────────┤
│                                                     │
│ ⚠ MANDATORY REQUEST                               │
│ All lab sessions must pre-book equipment          │
│ Submit ≥48 hours before lab date                  │
│                                                     │
│ LAB SESSION DETAILS:                              │
│                                                     │
│ Course: [CS301 - Database Systems ▼]             │
│ Lab Instructor: Dr. Kumar (auto-filled)          │
│ Course Lecturer: [Dr. Smith ▼]  (must co-sign)   │
│ Lab Session ID: CS301-Lab-W3                      │
│                                                     │
│ Session Date: [Jan 22, 2026 ▼]  (must be future)│
│ Day: Wednesday ✓                                  │
│ Time Slot: [10:00-13:00 (3 hours) ▼]             │
│   Fixed options only:                            │
│   ├─ 08:00-12:00 (4 hours)                       │
│   ├─ 10:00-13:00 (3 hours) ← selected           │
│   ├─ 13:15-17:00 (3.75 hours)                    │
│   └─ 14:00-17:00 (3 hours)                       │
│                                                     │
│ Student Count: [30]                              │
│                                                     │
│ EQUIPMENT REQUIREMENTS:                           │
│                                                     │
│ Equipment: [Digital Oscilloscope ▼]              │
│   Type: Lab-Dedicated ✓ (only type allowed)      │
│   Available: 2 units                             │
│ Quantity: [2 units]                              │
│ [+ Add Equipment] [Remove]                        │
│                                                     │
│ AVAILABILITY CHECK:                               │
│ ✓ Equipment available Jan 22, 10:00-13:00       │
│ ✓ No lab session conflicts                        │
│ ✓ No borrowable conflicts                         │
│                                                     │
│ SUBMISSION REQUIREMENTS:                          │
│ ☑ Both instructors will sign-off                 │
│ ☑ Understand: Equipment must be                  │
│   returned immediately after session             │
│ ☑ Lab session is highest priority                │
│ ☑ Coursework/Research requests may be cancelled  │
│                                                     │
│         [← Cancel] [Submit Request]              │
│                                                     │
│ After submission:                                 │
│ 1. Course Lecturer (Dr. Smith) will receive      │
│    notification & must approve                   │
│ 2. HOD auto-confirms (mandatory type)            │
│ 3. Equipment automatically reserved              │
│ 4. TO receives highest priority flag             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 SECTION 8: SYSTEM PARAMETERS & CONFIGURATION

### 8.1 Department-Configurable Parameters

```
╔════════════════════════════════════════════════════════╗
║  CONFIGURATION PARAMETERS (By Department)             ║
╚════════════════════════════════════════════════════════╝

COURSEWORK RETENTION:
├─ CSE Default: 7 days (configurable: 3-14 days)
├─ EEE Default: 10 days (configurable: 3-14 days)
└─ Coordinator can adjust per course

RESEARCH RETENTION:
├─ Default: 30 days (configurable: 14-60 days)
├─ Extensions: 2 max per project
└─ Supervisor can authorize extension

EXTRACURRICULAR MAX DURATION:
├─ Default: 7 days
├─ HOD can reduce to 3 days (strict)
└─ No extensions

PERSONAL MAX DURATION:
├─ Default: 3 days (fixed, no variation)
├─ HOD can reduce to 1 day (strict)
└─ No extensions

LAB SESSION ADVANCE NOTICE:
├─ Default: 48 hours (strict)
├─ Cannot reduce (enforced minimum)
└─ HOD can set emergency exception process

AUTO-APPROVAL THRESHOLDS:
├─ Coursework: Equipment ≤ $500 value
├─ Research: Equipment ≤ $1000 value
├─ Extracurricular: NO auto-approval
├─ Personal: NO auto-approval
└─ Lab Session: NO auto-approval

PENALTY RATES:
├─ Late return rates (per type): Configurable
├─ Damage multipliers: Configurable
├─ Penalty point thresholds: Configurable
└─ Suspension levels: Configurable

MAX RETENTION ENFORCEMENT:
├─ Hard limit: Cannot submit > max_days
├─ Soft warning: Alert if > (max_days * 0.8)
├─ Instructor override: HOD must authorize
└─ Override logged & tracked for audits
```

---

## ✅ SECTION 9: MIGRATION PLAN (v3.6 → v3.7)

### 9.1 Data Migration Steps

```
PHASE 1: DATABASE UPDATES (Week 1)
├─ Add new columns to requests table
│  ├─ from_date, from_time, from_datetime
│  ├─ to_date, to_time, to_datetime
│  ├─ equipment_type
│  ├─ instructor_id, instructor_approval_status
│  └─ max_retention_days
├─ Create new tables
│  ├─ request_approvals
│  ├─ penalty_assessments
│  ├─ equipment_availability
│  ├─ lab_sessions
│  ├─ instructor_approvals
│  └─ student_penalties
├─ Add foreign keys & indexes
└─ Migrate existing requests → new schema
   ├─ Set from_datetime = submitted_at
   ├─ Set to_datetime = submitted_at + 7 days (default)
   ├─ Map existing request types → 5 new types
   └─ Backfill equipment_type (LAB or BORROWABLE)

PHASE 2: BUSINESS LOGIC IMPLEMENTATION (Weeks 2-3)
├─ Lab Session request type & workflow
├─ Coursework deadline policy (From/To dates)
├─ Research retention limits
├─ Penalty calculation system
├─ Lab session override rule
├─ Conflict detection logic
└─ Instructor approval workflow

PHASE 3: UI/UX UPDATES (Weeks 2-4)
├─ New request submission wizard (5-step)
├─ Date/time picker component
├─ Equipment availability calendar
├─ Instructor approval dashboard
├─ Penalty tracking display
├─ Lab session management UI
└─ Student penalty account view

PHASE 4: TESTING & QA (Weeks 4-5)
├─ Unit tests (business logic)
├─ Integration tests (database + API)
├─ User acceptance testing (by instructors & students)
├─ Performance testing (large datasets)
└─ Security & authorization testing

PHASE 5: DEPLOYMENT & TRAINING (Week 6)
├─ Production deployment
├─ User training (students, instructors, HOD, TO)
├─ Support documentation
├─ 2-week monitoring & bug fixes
└─ Production stabilization
```

---

## 📚 APPENDIX: CONFIGURATION EXAMPLES

### A.1 CSE Department Configuration

```yaml
Department: Computer Science & Engineering
Timezone: Asia/Colombo

Equipment_Types:
  Lab_Dedicated:
    - Digital_Oscilloscope (location: CSE_LAB_3)
    - Function_Generator (location: CSE_LAB_3)
    - Logic_Analyzer (location: CSE_LAB_5)
  
  Borrowable:
    - Multimeter (count: 5)
    - USB_Measurement_Device (count: 3)
    - Network_Analyzer (count: 1)

Request_Type_Limits:
  Coursework:
    max_retention_days: 7
    auto_approval_enabled: true
    equipment_value_limit: 500
  
  Research:
    max_retention_days: 30
    max_extensions: 2
    auto_approval_enabled: false
  
  Extracurricular:
    max_retention_days: 7
    auto_approval_enabled: false
  
  Personal:
    max_retention_days: 3
    auto_approval_enabled: false

Lab_Sessions:
  advance_notice_hours: 48
  time_slots: ['08:00-12:00', '10:00-13:00', '13:15-17:00', '14:00-17:00']
  max_sessions_per_day: 4
  allowed_days: [1, 2, 3, 4, 5]  # Mon-Fri
  blocked_dates: [holidays]

Penalties:
  late_return_rates:
    lab_session: 30
    research: 5
    coursework: 5
    extracurricular: 10
    personal: 20
  
  damage_multipliers:
    lab_session: 1.5
    research: 1.0
    coursework: 1.0
    extracurricular: 1.2
    personal: 2.0
  
  thresholds:
    warning: 10
    caution: 30
    serious: 50
    restricted: 100
    suspended: 200
```

---

## 🔍 FINAL VALIDATION CHECKLIST

- ✅ Equipment types clearly differentiated (Lab-Dedicated vs Borrowable)
- ✅ 5 request types with distinct workflows
- ✅ Lab Session as highest priority (Level 5)
- ✅ Mandatory instructor sign-off for all types
- ✅ Temporal model (From/To dates-times) integrated
- ✅ Max retention policy per request type
- ✅ Penalty system (Late + Damage + Override)
- ✅ Lab session immediate return override rule
- ✅ Advance notice requirement (48h) for lab sessions
- ✅ Coursework deadline policy updated
- ✅ Conflict detection rules specified
- ✅ Auto-approval criteria per type
- ✅ Penalty appeal process included
- ✅ Department-configurable parameters
- ✅ Database schema updated
- ✅ UI/UX forms redesigned
- ✅ Migration plan provided
- ✅ Configuration examples included

---

**EqipHub v3.7 - FINAL SPECIFICATION**  
**Document Status:** ✅ COMPLETE & READY FOR DEVELOPMENT  
**Prepared By:** Software Engineering Agent  
**Date:** January 10, 2026  
**Next Steps:** Technical Implementation (Backend API, Database, Frontend UI)