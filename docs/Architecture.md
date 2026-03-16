# Architecture – Data Modeling

## Purpose
Define the core data entities and attributes required for the EqipHub system.

---

## Users

**Fields:**
- id
- full_name
- email
- password_hash
- role
- department_id
- is_active
- is_banned
- created_at
- last_login_at

---

## Departments

**Fields:**
- id
- name
- code
- description

---

## Labs

**Fields:**
- id
- name
- department_id
- location
- lab_type
- assigned_to_id

---

## Equipment

**Fields:**
- id
- name
- equipment_code
- category
- lab_id
- total_qty
- available_qty
- status
- is_maintenance
- created_at

---

## Requests

**Fields:**
- id
- requester_id
- request_type
- justification
- status
- priority_level
- request_date
- expected_return_date
- current_approval_level

---

## Request_Items

**Fields:**
- id
- request_id
- equipment_id
- quantity_requested

---

## Request_Approvals

**Fields:**
- id
- request_id
- approver_id
- approver_role
- action
- comments
- action_date

---

## Transactions

**Fields:**
- id
- request_id
- equipment_id
- issued_by_to_id
- issue_time
- return_time
- issue_condition
- return_condition
- condition_note

---

## Equipment_Maintenance

**Fields:**
- id
- equipment_id
- reported_by_id
- issue_description
- maintenance_status
- reported_date
- resolved_date

---

## Audit_Logs

**Fields:**
- id
- user_id
- action
- entity_type
- entity_id
- timestamp
