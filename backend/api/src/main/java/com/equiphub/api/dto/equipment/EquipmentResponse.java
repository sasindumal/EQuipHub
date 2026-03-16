package com.equiphub.api.dto.equipment;

import com.equiphub.api.model.Equipment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EquipmentResponse {

    private String              equipmentId;
    private String              name;
    private Integer             categoryId;
    private String              categoryName;
    private Equipment.EquipmentType    type;
    private UUID                departmentId;
    private String              departmentName;
    private String              departmentCode;
    private String              description;
    private String              specificationsJson;
    private LocalDate           purchaseDate;
    private BigDecimal          purchaseValue;
    private String              serialNumber;
    private Integer             currentCondition;   // 0–100
    private String              conditionLabel;     // EXCELLENT / GOOD / FAIR / POOR / CRITICAL
    private String              conditionNotes;
    private Equipment.EquipmentStatus  status;
    private Integer             totalQuantity;
    private String              currentLocation;
    private String              assignedLabs;
    private LocalDate           lastMaintenanceDate;
    private LocalDate           nextMaintenanceDate;
    private Integer             maintenanceIntervalDays;
    private Integer             depreciationRate;
    private BigDecimal          replacementCost;
    private boolean             retired;
    private boolean             maintenanceDue;     // computed: nextMaintenanceDate <= today
    private LocalDateTime       createdAt;
    private LocalDateTime       updatedAt;
}
