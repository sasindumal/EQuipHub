-- Seed 1000 equipments for CSE and EEE departments
-- V2___Seed_1000_Equipment.sql

-- First, get the department IDs
-- The departments use string IDs (CSE, EEE) in the equipment table based on init.sql

-- Equipment Categories (already seeded in init.sql):
-- 1: MEASUREMENT
-- 2: POWER_SUPPLY
-- 3: DEVELOPMENT_TOOLS
-- 4: COMPONENTS
-- 5: SPECIALIZED
-- 6: PORTABLE

-- CSE Equipment (500 items)
-- ========================

-- Oscilloscopes (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-OSC-' || LPAD(i::TEXT, 4, '0'),
    'Digital Oscilloscope ' || CASE 
        WHEN i <= 50 THEN 'Keysight 1000X '
        WHEN i <= 100 THEN 'Tektronix TDS2000 '
        WHEN i <= 150 THEN 'Rigol DS1000Z '
        WHEN i <= 200 THEN 'Agilent DSOX2000 '
        ELSE 'GW Instek GDS-3000 '
    END || (i % 100 + 1) || 'MHz',
    1, -- MEASUREMENT
    CASE WHEN i % 3 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'CSE',
    'Digital oscilloscope ' || (i % 100 + 1) || 'MHz, ' || CASE 
        WHEN i % 4 = 0 THEN '4 channel'
        WHEN i % 4 = 1 THEN '2 channel'
        WHEN i % 4 = 2 THEN '4 channel with MSO'
        ELSE '2 channel with MSO'
    END,
    'OSC-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 30 + 70)::int, -- condition 70-100
    'AVAILABLE',
    (i % 5) + 1, -- quantity 1-5
    'CSE Lab ' || CHR(65 + (i % 10)), -- Lab A-J
    DATE '2023-01-01' + (random() * 730)::int, -- random date in 2023-2024
    (random() * 50000 + 50000)::decimal(10,2), -- 50000-100000
    NOW(), NOW()
FROM generate_series(1, 250) i;

-- Multimeters (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-MUL-' || LPAD(i::TEXT, 4, '0'),
    'Digital Multimeter ' || CASE 
        WHEN i <= 50 THEN 'Fluke 289 '
        WHEN i <= 100 THEN 'Keysight U1253B '
        WHEN i <= 150 THEN 'Agilent U34461A '
        WHEN i <= 200 THEN 'Rigol DM3068 '
        ELSE 'GW Instek GDM-8261 '
    END,
    1, -- MEASUREMENT
    'BORROWABLE',
    'CSE',
    'True RMS Digital Multimeter with ' || CASE 
        WHEN i % 3 = 0 THEN 'Bluetooth'
        WHEN i % 3 = 1 THEN 'Data Logging'
        ELSE 'Basic'
    END,
    'MUL-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 40 + 60)::int,
    'AVAILABLE',
    (i % 10) + 1,
    'CSE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 20000 + 10000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 150) i;

-- Power Supplies (POWER_SUPPLY - category_id: 2)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-PWR-' || LPAD(i::TEXT, 4, '0'),
    'Power Supply ' || CASE 
        WHEN i <= 40 THEN 'Regulated DC 30V/2A '
        WHEN i <= 80 THEN 'Triple Output 30V/3A '
        WHEN i <= 120 THEN 'Programmable 60V/5A '
        WHEN i <= 160 THEN 'High Voltage 100V/1A '
        ELSE 'Bench Power 120W '
    END,
    2, -- POWER_SUPPLY
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'CSE',
    'Regulated power supply ' || (i % 30 + 1) || 'V ' || (i % 5 + 1) || 'A',
    'PWR-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 35 + 65)::int,
    'AVAILABLE',
    (i % 4) + 1,
    'CSE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 15000 + 8000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 200) i;

-- FPGA Boards (DEVELOPMENT_TOOLS - category_id: 3)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-FPG-' || LPAD(i::TEXT, 4, '0'),
    'FPGA Board ' || CASE 
        WHEN i <= 30 THEN 'Xilinx Nexys A7 '
        WHEN i <= 60 THEN 'Xilinx Basys 3 '
        WHEN i <= 90 THEN 'Intel Cyclone IV '
        WHEN i <= 120 THEN 'Lattice ICE40 '
        WHEN i <= 150 THEN 'Altera DE10-Lite '
        ELSE 'Xilinx Spartan-7 '
    END,
    3, -- DEVELOPMENT_TOOLS
    'BORROWABLE',
    'CSE',
    'FPGA Development Board with ' || CASE 
        WHEN i % 4 = 0 THEN 'Artix-7 100T'
        WHEN i % 4 = 1 THEN 'Artix-7 35T'
        WHEN i % 4 = 2 THEN 'Cyclone IV E'
        ELSE 'ICE40HX8K'
    END,
    'FPG-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 50 + 50)::int,
    'AVAILABLE',
    (i % 8) + 1,
    'CSE Lab ' || CHR(67 + (i % 8)), -- Lab C-J
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 40000 + 20000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 180) i;

-- Microcontroller Boards (DEVELOPMENT_TOOLS - category_id: 3)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-MCU-' || LPAD(i::TEXT, 4, '0'),
    'MCU Board ' || CASE 
        WHEN i <= 40 THEN 'STM32F4 Discovery '
        WHEN i <= 80 THEN 'Arduino Due '
        WHEN i <= 120 THEN 'ESP32 DevKit '
        WHEN i <= 160 THEN 'Raspberry Pi Pico '
        WHEN i <= 200 THEN 'Nordic nRF52 '
        ELSE 'TI MSP430 LaunchPad '
    END,
    3, -- DEVELOPMENT_TOOLS
    'BORROWABLE',
    'CSE',
    'Microcontroller Development Board',
    'MCU-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 40 + 60)::int,
    'AVAILABLE',
    (i % 10) + 1,
    'CSE Lab ' || CHR(67 + (i % 8)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 8000 + 3000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 240) i;

-- Component Kits (COMPONENTS - category_id: 4)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-CMP-' || LPAD(i::TEXT, 4, '0'),
    'Component Kit ' || CASE 
        WHEN i <= 50 THEN 'Basic Resistor/Capacitor Kit '
        WHEN i <= 100 THEN 'IC Starter Pack '
        WHEN i <= 150 THEN 'Sensor Module Set '
        WHEN i <= 200 THEN 'Power Electronics Kit '
        ELSE 'Analog Circuit Kit '
    END,
    4, -- COMPONENTS
    'BORROWABLE',
    'CSE',
    'Electronic components assortment kit with ' || (i % 50 + 50) || '+ components',
    'CMP-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 60 + 40)::int,
    'AVAILABLE',
    (i % 15) + 1,
    'CSE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 5000 + 2000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 250) i;

-- Specialized Equipment (SPECIALIZED - category_id: 5)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-SPE-' || LPAD(i::TEXT, 4, '0'),
    'Specialized Equipment ' || CASE 
        WHEN i <= 20 THEN 'Network Analyzer '
        WHEN i <= 40 THEN 'Spectrum Analyzer '
        WHEN i <= 60 THEN 'Logic Analyzer '
        WHEN i <= 80 THEN 'EMC Tester '
        ELSE 'Signal Generator '
    END,
    5, -- SPECIALIZED
    'LABDEDICATED',
    'CSE',
    'Professional grade ' || CASE 
        WHEN i % 5 = 0 THEN 'Network Analyzer'
        WHEN i % 5 = 1 THEN 'Spectrum Analyzer'
        WHEN i % 5 = 2 THEN 'Logic Analyzer'
        WHEN i % 5 = 3 THEN 'EMC Tester'
        ELSE 'Signal Generator'
    END,
    'SPE-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 30 + 70)::int,
    'AVAILABLE',
    1,
    'CSE Lab ' || CHR(67 + (i % 6)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 150000 + 50000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 100) i;

-- Portable Equipment (PORTABLE - category_id: 6)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'CSE-PRT-' || LPAD(i::TEXT, 4, '0'),
    'Portable Device ' || CASE 
        WHEN i <= 30 THEN 'Laptop Dell XPS '
        WHEN i <= 60 THEN 'Laptop HP ProBook '
        WHEN i <= 90 THEN 'Tablet iPad '
        WHEN i <= 120 THEN 'Laptop Lenovo ThinkPad '
        ELSE 'Chromebook '
    END,
    6, -- PORTABLE
    'BORROWABLE',
    'CSE',
    'Portable computing device for lab use',
    'PRT-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 50 + 50)::int,
    'AVAILABLE',
    1,
    'CSE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 80000 + 40000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 150) i;

-- EEE Equipment (500 items)
-- ========================

-- Oscilloscopes (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-OSC-' || LPAD(i::TEXT, 4, '0'),
    'Digital Oscilloscope ' || CASE 
        WHEN i <= 50 THEN 'Tektronix TDS2024 '
        WHEN i <= 100 THEN 'Keysight DSOX2000 '
        WHEN i <= 150 THEN 'Rigol DS1104Z '
        WHEN i <= 200 THEN 'Agilent DSO5014A '
        ELSE 'GW Instek GDS-3352 '
    END || (i % 100 + 1) || 'MHz',
    1, -- MEASUREMENT
    CASE WHEN i % 3 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Digital oscilloscope ' || (i % 100 + 1) || 'MHz, ' || CASE 
        WHEN i % 4 = 0 THEN '4 channel'
        WHEN i % 4 = 1 THEN '2 channel'
        WHEN i % 4 = 2 THEN '4 channel with MSO'
        ELSE '2 channel with MSO'
    END,
    'OSC-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 30 + 70)::int,
    'AVAILABLE',
    (i % 5) + 1,
    'EEE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 50000 + 50000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 250) i;

-- Function Generators (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-FUN-' || LPAD(i::TEXT, 4, '0'),
    'Function Generator ' || CASE 
        WHEN i <= 40 THEN 'Agilent 33220A '
        WHEN i <= 80 THEN 'Tektronix AFG1022 '
        WHEN i <= 120 THEN 'Rigol DG1022 '
        WHEN i <= 160 THEN 'Keysight 33500B '
        ELSE 'GW Instek AFG-2125 '
    END,
    1, -- MEASUREMENT
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Function/Waveform Generator ' || (i % 20 + 1) || 'MHz',
    'FUN-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 35 + 65)::int,
    'AVAILABLE',
    (i % 4) + 1,
    'EEE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 30000 + 15000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 200) i;

-- Power Supplies (POWER_SUPPLY - category_id: 2)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-PWR-' || LPAD(i::TEXT, 4, '0'),
    'Power Supply ' || CASE 
        WHEN i <= 40 THEN 'Regulated DC 30V/3A '
        WHEN i <= 80 THEN 'Triple Output 30V/5A '
        WHEN i <= 120 THEN 'High Voltage 60V/2A '
        WHEN i <= 160 THEN 'Programmable 100V/3A '
        ELSE 'Bench Power 300W '
    END,
    2, -- POWER_SUPPLY
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Regulated power supply ' || (i % 60 + 1) || 'V ' || (i % 5 + 1) || 'A',
    'PWR-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 35 + 65)::int,
    'AVAILABLE',
    (i % 4) + 1,
    'EEE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 20000 + 10000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 200) i;

-- Development Tools (DEVELOPMENT_TOOLS - category_id: 3)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-DEV-' || LPAD(i::TEXT, 4, '0'),
    'Development Board ' || CASE 
        WHEN i <= 30 THEN 'Arduino Uno R3 '
        WHEN i <= 60 THEN 'Arduino Mega 2560 '
        WHEN i <= 90 THEN 'ESP32-WROOM '
        WHEN i <= 120 THEN 'Raspberry Pi 4 '
        WHEN i <= 150 THEN 'BeagleBone Black '
        ELSE 'NVIDIA Jetson Nano '
    END,
    3, -- DEVELOPMENT_TOOLS
    'BORROWABLE',
    'EEE',
    'Development board for embedded systems',
    'DEV-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 40 + 60)::int,
    'AVAILABLE',
    (i % 8) + 1,
    'EEE Lab ' || CHR(67 + (i % 8)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 25000 + 5000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 180) i;

-- Component Kits (COMPONENTS - category_id: 4)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-CMP-' || LPAD(i::TEXT, 4, '0'),
    'Component Kit ' || CASE 
        WHEN i <= 50 THEN 'Resistor/Capacitor Kit '
        WHEN i <= 100 THEN 'Semiconductor Kit '
        WHEN i <= 150 THEN 'Passive Components Set '
        WHEN i <= 200 THEN 'IC Collection '
        ELSE 'Power Components Kit '
    END,
    4, -- COMPONENTS
    'BORROWABLE',
    'EEE',
    'Electronic components kit with ' || (i % 100 + 50) || '+ components',
    'CMP-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 60 + 40)::int,
    'AVAILABLE',
    (i % 15) + 1,
    'EEE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 6000 + 2000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 250) i;

-- Specialized Equipment (SPECIALIZED - category_id: 5)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-SPE-' || LPAD(i::TEXT, 4, '0'),
    'Specialized Equipment ' || CASE 
        WHEN i <= 20 THEN 'Signal Analyzer '
        WHEN i <= 40 THEN 'Power Analyzer '
        WHEN i <= 60 THEN 'LCR Meter '
        WHEN i <= 80 THEN 'Insulation Tester '
        ELSE 'Ground Resistance Tester '
    END,
    5, -- SPECIALIZED
    'LABDEDICATED',
    'EEE',
    'Professional grade electrical testing equipment',
    'SPE-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 30 + 70)::int,
    'AVAILABLE',
    1,
    'EEE Lab ' || CHR(67 + (i % 6)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 200000 + 80000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 100) i;

-- Portable Equipment (PORTABLE - category_id: 6)
INSERT INTO equipment (equipment_id, name, category_id, type, department_id, description, serial_number, current_condition, status, total_quantity, current_location, purchase_date, purchase_value, created_at, updated_at)
SELECT 
    'EEE-PRT-' || LPAD(i::TEXT, 4, '0'),
    'Portable Device ' || CASE 
        WHEN i <= 30 THEN 'Laptop Dell Latitude '
        WHEN i <= 60 THEN 'Laptop HP EliteBook '
        WHEN i <= 90 THEN 'Oscilloscope Handheld '
        WHEN i <= 120 THEN 'Multimeter Handheld '
        ELSE 'Thermal Camera '
    END,
    6, -- PORTABLE
    'BORROWABLE',
    'EEE',
    'Portable device for field work and lab use',
    'PRT-EEE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 50 + 50)::int,
    'AVAILABLE',
    1,
    'EEE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 100000 + 30000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 150) i;

-- Verify counts
SELECT department_id, COUNT(*) as equipment_count FROM equipment GROUP BY department_id ORDER BY department_id;
