package com.equiphub.api.dto.equipment;

import com.equiphub.api.model.Equipment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentStatusUpdateRequest {

    @NotNull(message = "New status is required")
    private Equipment.EquipmentStatus status;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason; // required when setting MAINTENANCE or DAMAGED

    private LocalDate nextMaintenanceDate; // optional, used when sending to MAINTENANCE

    private Integer conditionScore; // 0-100, optional update alongside status change
}
