package com.equiphub.api.dto.inspection;

import com.equiphub.api.model.Inspection;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInspectionDTO {

    @NotNull(message = "Request item ID is required")
    private Integer requestItemId;

    @NotNull(message = "Inspection type is required")
    private Inspection.InspectionType inspectionType;

    @NotNull(message = "Condition before rating is required")
    @Min(value = 0, message = "Condition must be between 0 and 100")
    @Max(value = 100, message = "Condition must be between 0 and 100")
    private Integer conditionBefore;

    @Min(value = 0, message = "Condition must be between 0 and 100")
    @Max(value = 100, message = "Condition must be between 0 and 100")
    private Integer conditionAfter;

    @Min(value = 0, message = "Damage level must be between 0 and 5")
    @Max(value = 5, message = "Damage level must be between 0 and 5")
    private Integer damageLevel;

    private String damageDescription;

    private String damagePhotos;

    private String preDamageEvidence;

    private String notes;
}
