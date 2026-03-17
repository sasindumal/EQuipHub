package com.equiphub.api.dto.equipment;

import com.equiphub.api.model.Equipment;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEquipmentRequest {

    @NotNull(message = "Equipment ID is required (e.g. MULTI-001)")
    private UUID equipmentId;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    // Accept numeric categoryId OR a string categoryName from the frontend.
    // @JsonAlias ensures legacy payloads sending "category" as an int still work.
    @JsonAlias("category")
    private Integer categoryId;    // e.g. 3  (numeric FK)

    private String categoryName;   // e.g. "instrument" (frontend sends this)

    @NotNull(message = "Equipment type is required")
    private Equipment.EquipmentType type; // LABDEDICATED or BORROWABLE

    @NotBlank(message = "Department ID is required")
    private String departmentId;

    private String description;

    private String specificationsJson;

    private LocalDate purchaseDate;

    @DecimalMin(value = "0.0", message = "Purchase value cannot be negative")
    private BigDecimal purchaseValue;

    @Size(max = 100, message = "Serial number cannot exceed 100 characters")
    private String serialNumber;

    @Min(value = 1,    message = "Total quantity must be at least 1")
    @Max(value = 1000, message = "Total quantity cannot exceed 1000")
    private Integer totalQuantity = 1;

    @Size(max = 100)
    private String currentLocation;

    private String assignedLabs;

    @Min(value = 1,    message = "Maintenance interval must be at least 1 day")
    @Max(value = 3650, message = "Maintenance interval cannot exceed 10 years")
    private Integer maintenanceIntervalDays;

    @DecimalMin(value = "0.0")
    private BigDecimal replacementCost;

    @Min(0) @Max(100)
    private Integer depreciationRate;
}
