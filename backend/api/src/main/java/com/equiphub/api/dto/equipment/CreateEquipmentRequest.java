package com.equiphub.api.dto.equipment;

import com.equiphub.api.model.Equipment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateEquipmentRequest {

    @NotBlank(message = "Equipment ID is required (e.g. MULTI-001)")
    @Pattern(regexp = "^[A-Z0-9\\-]{3,20}$",
             message = "Equipment ID must be 3-20 chars: uppercase letters, digits, hyphens only")
    private UUID equipmentId;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Category ID is required")
    private Integer categoryId;    // maps to EquipmentCategory

    @NotNull(message = "Equipment type is required")
    private Equipment.EquipmentType type; // LABDEDICATED or BORROWABLE

    @NotBlank(message = "Department ID is required")
    private String departmentId;

    private String description;

    private String specificationsJson; // free-form JSON text

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", message = "Purchase value cannot be negative")
    private BigDecimal purchaseValue;

    @Size(max = 100, message = "Serial number cannot exceed 100 characters")
    private String serialNumber;

    @Min(value = 1,   message = "Total quantity must be at least 1")
    @Max(value = 1000, message = "Total quantity cannot exceed 1000")
    private Integer totalQuantity = 1;

    @NotBlank(message = "Current location is required")
    @Size(max = 100)
    private String currentLocation;

    private String assignedLabs;  // comma-separated lab IDs

    @Min(value = 1,   message = "Maintenance interval must be at least 1 day")
    @Max(value = 3650, message = "Maintenance interval cannot exceed 10 years")
    private Integer maintenanceIntervalDays;

    @DecimalMin(value = "0.0")
    private BigDecimal replacementCost;

    @Min(0) @Max(100)
    private Integer depreciationRate;
}
