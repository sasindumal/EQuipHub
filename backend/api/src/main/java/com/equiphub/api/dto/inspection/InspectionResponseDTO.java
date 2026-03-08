package com.equiphub.api.dto.inspection;

import com.equiphub.api.model.InspectionType;
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
    private String  requestId;            // matches item.getRequest().getRequestId()

    private InspectionType inspectionType;  // ← enum, NOT String
    private String         inspectionTypeName;

    private UUID   inspectorId;
    private String inspectorName;
    private String inspectorEmail;

    private UUID equipmentId;
    private String  equipmentName;
    private String  equipmentSerialNumber;

    private Integer conditionBefore;
    private String  conditionBeforeLabel;
    private Integer conditionAfter;
    private String  conditionAfterLabel;
    private Integer conditionDelta;

    private Integer damageLevel;
    private String  damageLevelLabel;
    private String  damageDescription;
    private String  damagePhotos;
    private String  preDamageEvidence;

    private Boolean       penaltyApplicable;
    private Boolean       studentAcknowledged;
    private LocalDateTime studentAcknowledgementAt;

    private String        notes;
    private LocalDateTime inspectedAt;
}