package com.equiphub.api.dto.equipment;

import com.equiphub.api.model.Equipment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateEquipmentRequest {

    @Size(max = 255)
    private String name;

    private Integer categoryId;

    private Equipment.EquipmentType type;

    private String description;

    private String specificationsJson;

    private LocalDate purchaseDate;

    @DecimalMin("0.0")
    private BigDecimal purchaseValue;

    @Size(max = 100)
    private String serialNumber;

    @Min(0) @Max(100)
    private Integer currentCondition;

    private String conditionNotes;

    @Size(max = 100)
    private String currentLocation;

    private String assignedLabs;

    private LocalDate lastMaintenanceDate;

    private LocalDate nextMaintenanceDate;

    @Min(1) @Max(3650)
    private Integer maintenanceIntervalDays;

    @DecimalMin("0.0")
    private BigDecimal replacementCost;

    @Min(0) @Max(100)
    private Integer depreciationRate;
}
