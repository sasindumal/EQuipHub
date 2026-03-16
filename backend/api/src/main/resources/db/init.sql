-- ============================================
-- EQuipHub v1.0 Database Initialization Script
-- PostgreSQL 15+
-- Location: backend/api/src/main/resources/db/init.sql
-- ============================================
-- Purpose: Initialize sample data after database schema creation
-- Usage: This script runs automatically when Docker Compose starts PostgreSQL
-- Note: Schema must exist (created by Flyway migration V1__Create_Base_Schema.sql)

-- ============================================
-- ENABLE EXTENSIONS
-- ============================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- INSERT: DEPARTMENTS (2 core departments)
-- ============================================

INSERT INTO departments (
    department_id, name, description, 
    max_retention_days_coursework, max_retention_days_research,
    max_retention_days_extracurricular, max_retention_days_personal,
    penalty_rate_late_coursework, penalty_rate_late_research,
    penalty_rate_late_personal, penalty_rate_override,
    damage_multiplier_personal, auto_approval_enabled,
    auto_approval_value_limit, auto_approval_grade_minimum,
    lab_time_slot_1_start, lab_time_slot_1_end,
    lab_time_slot_2_start, lab_time_slot_2_end
) VALUES 
(
    'CSE', 
    'Department of Computer Science & Engineering',
    'Computer Science & Engineering Department',
    7, 30, 7, 3,
    10, 20, 5, 50,
    2.0, TRUE, 5000.00, 'C',
    '08:00', '12:00', '13:00', '16:00'
),
(
    'EEE',
    'Department of Electrical & Electronics Engineering', 
    'Electrical & Electronics Engineering Department',
    7, 30, 7, 3,
    10, 20, 5, 50,
    2.0, TRUE, 5000.00, 'C',
    '08:00', '12:00', '13:00', '16:00'
);

-- ============================================
-- INSERT: EQUIPMENT_CATEGORIES (6 predefined)
-- ============================================

INSERT INTO equipment_categories (name, description, damage_multiplier_base, typical_replacement_cost) VALUES
('MEASUREMENT', 'Measurement instruments (oscilloscopes, multimeters, analyzers)', 1.0, 50000.00),
('POWER_SUPPLY', 'Power supplies and regulators', 1.0, 15000.00),
('DEVELOPMENT_TOOLS', 'FPGA boards, microcontrollers, compilers', 1.2, 30000.00),
('COMPONENTS', 'Electronic components (resistors, capacitors, ICs)', 0.8, 5000.00),
('SPECIALIZED', 'Specialized lab equipment', 1.5, 100000.00),
('PORTABLE', 'Portable tools and laptops', 1.1, 80000.00);

-- ============================================
-- INSERT: SAMPLE EQUIPMENT FOR CSE (Computer Labs)
-- ============================================

INSERT INTO equipment (
    equipment_id, name, category_id, type, department_id,
    description, serial_number, current_condition, 
    status, total_quantity, current_location,
    purchase_date, purchase_value
) VALUES
-- Oscilloscopes (MEASUREMENT)
('EQU-OSC-001', 'Oscilloscope Digital Keysight 1000X', 1, 'LAB_DEDICATED', 'CSE',
 'Digital oscilloscope 100MHz', 'KEY-OSC-12345', 0, 'AVAILABLE', 2, 'CSE Lab A', '2023-01-15', 85000.00),

-- Multimeters (MEASUREMENT)
('EQU-MUL-001', 'Digital Multimeter Fluke 289', 1, 'BORROWABLE', 'CSE',
 'True RMS digital multimeter', 'FLU-MUL-54321', 1, 'AVAILABLE', 5, 'CSE Lab A', '2022-06-10', 25000.00),

-- Power Supplies (POWER_SUPPLY)
('EQU-PWR-001', 'Dual Power Supply 30V 2A', 2, 'LAB_DEDICATED', 'CSE',
 'Regulated dual power supply', 'PSU-PWR-11111', 0, 'AVAILABLE', 3, 'CSE Lab B', '2023-03-20', 18000.00),

-- FPGA Boards (DEVELOPMENT_TOOLS)
('EQU-FPG-001', 'FPGA Development Board Xilinx Nexys', 3, 'BORROWABLE', 'CSE',
 'Xilinx Artix-7 FPGA board', 'XIL-FPG-99999', 2, 'AVAILABLE', 10, 'CSE Lab C', '2023-08-05', 32000.00),

-- Microcontroller Boards (DEVELOPMENT_TOOLS)
('EQU-ARM-001', 'ARM Cortex-M4 Development Kit STM32', 3, 'BORROWABLE', 'CSE',
 'STMicroelectronics development board', 'STM-ARM-77777', 1, 'AVAILABLE', 15, 'CSE Lab C', '2023-05-12', 12000.00),

-- Component Kit (COMPONENTS)
('EQU-COM-001', 'Electronic Components Assortment Kit', 4, 'BORROWABLE', 'CSE',
 'Resistors, capacitors, ICs, connectors', 'COM-KIT-33333', 0, 'AVAILABLE', 20, 'CSE Lab A', '2023-02-01', 8000.00);

-- ============================================
-- INSERT: SAMPLE EQUIPMENT FOR EEE (Electronics Labs)
-- ============================================

INSERT INTO equipment (
    equipment_id, name, category_id, type, department_id,
    description, serial_number, current_condition,
    status, total_quantity, current_location,
    purchase_date, purchase_value
) VALUES
-- Oscilloscope (MEASUREMENT)
('EQU-TDS-001', 'Oscilloscope Tektronix TDS2024', 1, 'LAB_DEDICATED', 'EEE',
 'Tektronix digital oscilloscope 200MHz', 'TEK-TDS-66666', 0, 'AVAILABLE', 2, 'EEE Lab A', '2023-01-10', 95000.00),

-- Function Generator (MEASUREMENT)
('EQU-FUN-001', 'Function Generator Agilent 33220A', 1, 'LAB_DEDICATED', 'EEE',
 'Waveform generator 20MHz', 'AGL-FUN-88888', 1, 'AVAILABLE', 2, 'EEE Lab B', '2023-02-15', 45000.00),

-- Regulated Power Supply (POWER_SUPPLY)
('EQU-REG-001', 'Regulated Power Supply 0-30V 0-3A', 2, 'LAB_DEDICATED', 'EEE',
 'Variable regulated PSU', 'REG-PSU-44444', 0, 'AVAILABLE', 4, 'EEE Lab B', '2023-04-20', 22000.00),

-- Specialized Equipment (SPECIALIZED)
('EQU-SIG-001', 'Signal Analyzer Rohde & Schwarz', 5, 'LAB_DEDICATED', 'EEE',
 'RF signal analyzer', 'RND-SIG-55555', 1, 'AVAILABLE', 1, 'EEE Lab C', '2022-11-10', 250000.00);

-- ============================================
-- INSERT: COURSES (Sample courses CSE & EEE)
-- ============================================

INSERT INTO courses (course_id, course_code, course_name, department_id, semester_offered, credits, lab_required) VALUES
-- CSE Courses
('CS101', 'CS101', 'Programming Fundamentals', 'CSE', 1, 3.0, FALSE),
('CS201', 'CS201', 'Digital Logic Design', 'CSE', 2, 3.0, TRUE),
('CS301', 'CS301', 'Microprocessors & Assembly Language', 'CSE', 3, 3.0, TRUE),
('CS401', 'CS401', 'Embedded Systems', 'CSE', 4, 3.0, TRUE),
('CS501', 'CS501', 'VLSI Design', 'CSE', 5, 3.0, TRUE),
('CS601', 'CS601', 'Advanced FPGA Design', 'CSE', 6, 3.0, TRUE),
('CS701', 'CS701', 'Research Project I', 'CSE', 7, 4.0, TRUE),
('CS801', 'CS801', 'Research Project II', 'CSE', 8, 4.0, TRUE),

-- EEE Courses
('EE101', 'EE101', 'Basic Electrical Engineering', 'EEE', 1, 3.0, FALSE),
('EE201', 'EE201', 'Circuit Analysis', 'EEE', 2, 3.0, TRUE),
('EE301', 'EE301', 'Electronics I', 'EEE', 3, 3.0, TRUE),
('EE401', 'EE401', 'Electronics II', 'EEE', 4, 3.0, TRUE),
('EE501', 'EE501', 'Power Electronics', 'EEE', 5, 3.0, TRUE),
('EE601', 'EE601', 'Signal Processing', 'EEE', 6, 3.0, TRUE),
('EE701', 'EE701', 'Research Project I', 'EEE', 7, 4.0, TRUE),
('EE801', 'EE801', 'Research Project II', 'EEE', 8, 4.0, TRUE);

-- ============================================
-- INSERT: ACTIVITIES (Co-curricular)
-- ============================================

INSERT INTO activities (activity_code, activity_name, description, department_id, category, is_active) VALUES
('ROBO-CLUB', 'Robotics Club', 'Robotics and autonomous systems club', 'CSE', 'TECHNICAL', TRUE),
('IEEE-STUDENT', 'IEEE Student Branch', 'IEEE student chapter activities', 'EEE', 'TECHNICAL', TRUE),
('HACKATHON', 'Annual Hackathon', 'Coding competition event', 'CSE', 'TECHNICAL', TRUE),
('PAPER-CLUB', 'Paper Presentation Club', 'Presentation skills development', 'CSE', 'ACADEMIC', TRUE),
('CIRCUIT-LAB', 'Circuit Design Lab', 'Circuit design and testing activities', 'EEE', 'TECHNICAL', TRUE);

-- ============================================
-- Sample Data Complete
-- ============================================
-- Additional User, Supervisor, Enrollment data should be created
-- via application initialization or separate script
-- 
-- Key Notes:
-- 1. Users are NOT included in this script (created via app or admin panel)
-- 2. Enrollments auto-created when students register for courses
-- 3. Supervisors assigned during semester start
-- 4. Appointed lecturers assigned for activities
-- 5. All timestamps use server timezone
-- 6. Default constraints and check constraints enforced at DB level

-- ============================================
-- VERIFY DATA
-- ============================================

-- Verify departments created
SELECT COUNT(*) as dept_count FROM departments;
-- Expected: 2

-- Verify equipment created
SELECT COUNT(*) as equipment_count FROM equipment;
-- Expected: 10

-- Verify courses created
SELECT COUNT(*) as course_count FROM courses;
-- Expected: 16

-- Verify activities created
SELECT COUNT(*) as activity_count FROM activities;
-- Expected: 5

-- Show equipment per department
SELECT d.department_id, COUNT(e.equipment_id) as equipment_count
FROM departments d
LEFT JOIN equipment e ON d.department_id = e.department_id
GROUP BY d.department_id;

-- Show courses per department
SELECT d.department_id, COUNT(c.course_id) as course_count
FROM departments d
LEFT JOIN courses c ON d.department_id = c.department_id
GROUP BY d.department_id;
