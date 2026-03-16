-- V3: Fix equipment table to use UUID departmentid (matching Java model)
-- This migration drops and recreates the equipment table with proper foreign keys

-- Drop existing equipment table if exists
DROP TABLE IF EXISTS equipment CASCADE;

-- Create equipment table with UUID departmentid (matching Java model)
CREATE TABLE equipment (
    equipmentid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    categoryid INTEGER NOT NULL REFERENCES equipment_categories(categoryid),
    type VARCHAR(50) NOT NULL CHECK (type IN ('LABDEDICATED', 'BORROWABLE')),
    departmentid UUID NOT NULL REFERENCES departments(id),
    description TEXT,
    specifications TEXT,
    purchaseDate DATE,
    purchaseValue NUMERIC,
    serialNumber VARCHAR(100) UNIQUE,
    currentCondition INTEGER NOT NULL DEFAULT 0,
    conditionNotes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'INUSE', 'MAINTENANCE', 'DAMAGED', 'ARCHIVED')),
    totalQuantity INTEGER NOT NULL DEFAULT 1,
    currentLocation VARCHAR(100) NOT NULL,
    assignedLabs TEXT,
    lastMaintenanceDate DATE,
    nextMaintenanceDate DATE,
    maintenanceIntervalDays INTEGER,
    depreciationRate INTEGER,
    replacementCost NUMERIC,
    isRetired BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_equipment_department ON equipment(departmentid);
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_type ON equipment(type);
CREATE INDEX idx_equipment_category ON equipment(categoryid);
CREATE INDEX idx_equipment_location ON equipment(currentLocation);
CREATE INDEX idx_equipment_serial ON equipment(serialNumber);
