package com.equiphub.api.dto.penalty;

import com.equiphub.api.model.Penalty.PenaltyStatus;
import com.equiphub.api.model.Penalty.PenaltyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyResponseDTO {

    private Integer penaltyId;
    private String requestId;
    private UUID studentId;
    private String studentName;
    private String studentIndexNumber;
    private PenaltyType penaltyType;
    private Integer points;
    private String reason;
    private String calculationDetailsJson;
    private PenaltyStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private Integer totalPointsAfter;
    private String statusLevel;
    private Boolean appealed;
    private LocalDateTime createdAt;
}
