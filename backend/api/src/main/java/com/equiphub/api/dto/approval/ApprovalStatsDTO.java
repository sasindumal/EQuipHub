package com.equiphub.api.dto.approval;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStatsDTO {

    private long totalPending;
    private long totalApproved;
    private long totalRejected;
    private long slaBreached;
    private long emergencyPending;
    private Map<String, Long> pendingByType;
    private Map<String, Long> pendingByStage;
    private double averageApprovalTimeHours;
}
