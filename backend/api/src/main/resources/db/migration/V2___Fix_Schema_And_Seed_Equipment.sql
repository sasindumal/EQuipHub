-- Fix schema to match Java model and seed 1000 equipment
-- V2___Fix_Schema_And_Seed_Equipment.sql

-- Drop existing tables if they exist (fresh start)
DROP TABLE IF EXISTS equipment CASCADE;
DROP TABLE IF EXISTS equipment_categories CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS approvals CASCADE;

-- Create equipment_categories table (matches Java model)
CREATE TABLE equipment_categories (
    categoryid SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    damagemultiplierbase DOUBLE PRECISION DEFAULT 1.0,
    typicalreplacementcost DECIMAL(15, 2)
);

-- Insert equipment categories
INSERT INTO equipment_categories (name, description, damagemultiplierbase, typicalreplacementcost) VALUES
('MEASUREMENT', 'Measurement instruments (oscilloscopes, multimeters, analyzers)', 1.0, 50000.00),
('POWER_SUPPLY', 'Power supplies and regulators', 1.0, 15000.00),
('DEVELOPMENT_TOOLS', 'FPGA boards, microcontrollers, compilers', 1.2, 30000.00),
('COMPONENTS', 'Electronic components (resistors, capacitors, ICs)', 0.8, 5000.00),
('SPECIALIZED', 'Specialized lab equipment', 1.5, 100000.00),
('PORTABLE', 'Portable tools and laptops', 1.1, 80000.00);

-- Create equipment table (matches Java model)
-- Note: departmentid is a string code (CSE/EEE) not UUID reference
CREATE TABLE equipment (
    equipmentid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    categoryid INTEGER NOT NULL REFERENCES equipment_categories(categoryid) ON DELETE RESTRICT,
    type VARCHAR(50) NOT NULL CHECK (type IN ('LABDEDICATED', 'BORROWABLE')),
    departmentid VARCHAR(10) NOT NULL,
    description TEXT,
    specifications TEXT,
    purchasedate DATE,
    purchasevalue DECIMAL(15, 2),
    serialnumber VARCHAR(100) UNIQUE,
    currentcondition INTEGER NOT NULL DEFAULT 0,
    conditionnotes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'INUSE', 'MAINTENANCE', 'DAMAGED', 'ARCHIVED')),
    totalquantity INTEGER NOT NULL DEFAULT 1,
    currentlocation VARCHAR(100) NOT NULL,
    assignedlabs TEXT,
    lastmaintenancedate DATE,
    nextmaintenancedate DATE,
    maintenanceintervaldays INTEGER,
    depreciationrate INTEGER,
    replacementcost DECIMAL(15, 2),
    isretired BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_equipment_department ON equipment(departmentid);
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_type ON equipment(type);
CREATE INDEX idx_equipment_category ON equipment(categoryid);
CREATE INDEX idx_equipment_location ON equipment(currentlocation);
CREATE INDEX idx_equipment_serial ON equipment(serialnumber);

-- CSE Equipment (500 items)
-- ========================

-- Oscilloscopes (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Digital Oscilloscope ' || CASE 
        WHEN i <= 50 THEN 'Keysight 1000X '
        WHEN i <= 100 THEN 'Tektronix TDS2000 '
        WHEN i <= 150 THEN 'Rigol DS1000Z '
        WHEN i <= 200 THEN 'Agilent DSOX2000 '
        ELSE 'GW Instek GDS-3000 '
    END || ((i % 100) + 1) || 'MHz',
    1,
    CASE WHEN i % 3 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'CSE',
    'Digital oscilloscope ' || ((i % 100) + 1) || 'MHz, ' || CASE 
        WHEN i % 4 = 0 THEN '4 channel'
        WHEN i % 4 = 1 THEN '2 channel'
        WHEN i % 4 = 2 THEN '4 channel with MSO'
        ELSE '2 channel with MSO'
    END,
    'OSC-CSE-' || LPAD(i::TEXT, 5, '0'),
    (random() * 30 + 70)::int,
    'AVAILABLE',
    (i % 5) + 1,
    'CSE Lab ' || CHR(65 + (i % 10)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 50000 + 50000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 250) i;

-- Multimeters (MEASUREMENT - category_id: 1)
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Digital Multimeter ' || CASE 
        WHEN i <= 50 THEN 'Fluke 289 '
        WHEN i <= 100 THEN 'Keysight U1253B '
        WHEN i <= 150 THEN 'Agilent U34461A '
        WHEN i <= 200 THEN 'Rigol DM3068 '
        ELSE 'GW Instek GDM-8261 '
    END,
    1,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Power Supply ' || CASE 
        WHEN i <= 40 THEN 'Regulated DC 30V/2A '
        WHEN i <= 80 THEN 'Triple Output 30V/3A '
        WHEN i <= 120 THEN 'Programmable 60V/5A '
        WHEN i <= 160 THEN 'High Voltage 100V/1A '
        ELSE 'Bench Power 120W '
    END,
    2,
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'CSE',
    'Regulated power supply ' || ((i % 30) + 1) || 'V ' || ((i % 5) + 1) || 'A',
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'FPGA Board ' || CASE 
        WHEN i <= 30 THEN 'Xilinx Nexys A7 '
        WHEN i <= 60 THEN 'Xilinx Basys 3 '
        WHEN i <= 90 THEN 'Intel Cyclone IV '
        WHEN i <= 120 THEN 'Lattice ICE40 '
        WHEN i <= 150 THEN 'Altera DE10-Lite '
        ELSE 'Xilinx Spartan-7 '
    END,
    3,
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
    'CSE Lab ' || CHR(67 + (i % 8)),
    DATE '2023-01-01' + (random() * 730)::int,
    (random() * 40000 + 20000)::decimal(10,2),
    NOW(), NOW()
FROM generate_series(1, 180) i;

-- Microcontroller Boards (DEVELOPMENT_TOOLS - category_id: 3)
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'MCU Board ' || CASE 
        WHEN i <= 40 THEN 'STM32F4 Discovery '
        WHEN i <= 80 THEN 'Arduino Due '
        WHEN i <= 120 THEN 'ESP32 DevKit '
        WHEN i <= 160 THEN 'Raspberry Pi Pico '
        WHEN i <= 200 THEN 'Nordic nRF52 '
        ELSE 'TI MSP430 LaunchPad '
    END,
    3,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Component Kit ' || CASE 
        WHEN i <= 50 THEN 'Basic Resistor/Capacitor Kit '
        WHEN i <= 100 THEN 'IC Starter Pack '
        WHEN i <= 150 THEN 'Sensor Module Set '
        WHEN i <= 200 THEN 'Power Electronics Kit '
        ELSE 'Analog Circuit Kit '
    END,
    4,
    'BORROWABLE',
    'CSE',
    'Electronic components assortment kit with ' || ((i % 50) + 50) || '+ components',
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Specialized Equipment ' || CASE 
        WHEN i <= 20 THEN 'Network Analyzer '
        WHEN i <= 40 THEN 'Spectrum Analyzer '
        WHEN i <= 60 THEN 'Logic Analyzer '
        WHEN i <= 80 THEN 'EMC Tester '
        ELSE 'Signal Generator '
    END,
    5,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Portable Device ' || CASE 
        WHEN i <= 30 THEN 'Laptop Dell XPS '
        WHEN i <= 60 THEN 'Laptop HP ProBook '
        WHEN i <= 90 THEN 'Tablet iPad '
        WHEN i <= 120 THEN 'Laptop Lenovo ThinkPad '
        ELSE 'Chromebook '
    END,
    6,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Digital Oscilloscope ' || CASE 
        WHEN i <= 50 THEN 'Tektronix TDS2024 '
        WHEN i <= 100 THEN 'Keysight DSOX2000 '
        WHEN i <= 150 THEN 'Rigol DS1104Z '
        WHEN i <= 200 THEN 'Agilent DSO5014A '
        ELSE 'GW Instek GDS-3352 '
    END || ((i % 100) + 1) || 'MHz',
    1,
    CASE WHEN i % 3 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Digital oscilloscope ' || ((i % 100) + 1) || 'MHz, ' || CASE 
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Function Generator ' || CASE 
        WHEN i <= 40 THEN 'Agilent 33220A '
        WHEN i <= 80 THEN 'Tektronix AFG1022 '
        WHEN i <= 120 THEN 'Rigol DG1022 '
        WHEN i <= 160 THEN 'Keysight 33500B '
        ELSE 'GW Instek AFG-2125 '
    END,
    1,
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Function/Waveform Generator ' || ((i % 20) + 1) || 'MHz',
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Power Supply ' || CASE 
        WHEN i <= 40 THEN 'Regulated DC 30V/3A '
        WHEN i <= 80 THEN 'Triple Output 30V/5A '
        WHEN i <= 120 THEN 'High Voltage 60V/2A '
        WHEN i <= 160 THEN 'Programmable 100V/3A '
        ELSE 'Bench Power 300W '
    END,
    2,
    CASE WHEN i % 4 = 0 THEN 'LABDEDICATED' ELSE 'BORROWABLE' END,
    'EEE',
    'Regulated power supply ' || ((i % 60) + 1) || 'V ' || ((i % 5) + 1) || 'A',
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Development Board ' || CASE 
        WHEN i <= 30 THEN 'Arduino Uno R3 '
        WHEN i <= 60 THEN 'Arduino Mega 2560 '
        WHEN i <= 90 THEN 'ESP32-WROOM '
        WHEN i <= 120 THEN 'Raspberry Pi 4 '
        WHEN i <= 150 THEN 'BeagleBone Black '
        ELSE 'NVIDIA Jetson Nano '
    END,
    3,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Component Kit ' || CASE 
        WHEN i <= 50 THEN 'Resistor/Capacitor Kit '
        WHEN i <= 100 THEN 'Semiconductor Kit '
        WHEN i <= 150 THEN 'Passive Components Set '
        WHEN i <= 200 THEN 'IC Collection '
        ELSE 'Power Components Kit '
    END,
    4,
    'BORROWABLE',
    'EEE',
    'Electronic components kit with ' || ((i % 100) + 50) || '+ components',
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Specialized Equipment ' || CASE 
        WHEN i <= 20 THEN 'Signal Analyzer '
        WHEN i <= 40 THEN 'Power Analyzer '
        WHEN i <= 60 THEN 'LCR Meter '
        WHEN i <= 80 THEN 'Insulation Tester '
        ELSE 'Ground Resistance Tester '
    END,
    5,
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
INSERT INTO equipment (equipmentid, name, categoryid, type, departmentid, description, serialnumber, currentcondition, status, totalquantity, currentlocation, purchasedate, purchasevalue, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'Portable Device ' || CASE 
        WHEN i <= 30 THEN 'Laptop Dell Latitude '
        WHEN i <= 60 THEN 'Laptop HP EliteBook '
        WHEN i <= 90 THEN 'Oscilloscope Handheld '
        WHEN i <= 120 THEN 'Multimeter Handheld '
        ELSE 'Thermal Camera '
    END,
    6,
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
SELECT departmentid as department, COUNT(*) as equipment_count 
FROM equipment 
WHERE departmentid IN ('CSE', 'EEE')
GROUP BY departmentid 
ORDER BY departmentid;
