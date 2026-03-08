package com.equiphub.api.dto.inspection;

import com.equiphub.api.model.Inspection;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionResponseDTO {

    private Integer inspectionId;
    private Integer requestItemId;
    private String requestId;
    private Inspection.InspectionType inspectionType;
    private String inspectionTypeName;

    // Inspector info
    private UUID inspectorId;
    private String inspectorName;
    private String inspectorEmail;

    // Equipment info
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentSerialNumber;

    // Condition ratings
    private Integer conditionBefore;
    private String conditionBeforeLabel;
    private Integer conditionAfter;
    private String conditionAfterLabel;
    private Integer conditionDelta;

    // Damage assessment
    private Integer damageLevel;
    private String damageLevelLabel;
    private String damageDescription;
    private String damagePhotos;
    private String preDamageEvidence;

    // Penalty info
    private Boolean penaltyApplicable;
    private String penaltyReason;

    // Student acknowledgement
    private Boolean studentAcknowledged;
    private LocalDateTime studentAcknowledgementAt;

    // Metadata
    private String notes;
    private LocalDateTime inspectedAt;
    private LocalDateTime createdAt;
}
