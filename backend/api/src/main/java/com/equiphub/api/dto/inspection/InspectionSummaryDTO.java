package com.equiphub.api.dto.inspection;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionSummaryDTO {

    private long totalInspections;
    private long preIssuanceCount;
    private long postReturnCount;
    private long damageDetectedCount;
    private long penaltiesTriggered;
    private double averageConditionBefore;
    private double averageConditionAfter;
    private double averageConditionDelta;
    private Map<String, Long> inspectionsByDamageLevel;
}
