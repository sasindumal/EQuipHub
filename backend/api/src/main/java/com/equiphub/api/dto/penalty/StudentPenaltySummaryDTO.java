package com.equiphub.api.dto.penalty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPenaltySummaryDTO {

    private UUID studentId;
    private String studentName;
    private String studentIndexNumber;
    private int totalActivePoints;
    private String currentLevel;       // GREEN / YELLOW / ORANGE / RED
    private long totalPenalties;
    private long pendingPenalties;
    private long appealedPenalties;
    private long lateReturnCount;
    private long damageCount;
    private boolean borrowingRestricted;
}
