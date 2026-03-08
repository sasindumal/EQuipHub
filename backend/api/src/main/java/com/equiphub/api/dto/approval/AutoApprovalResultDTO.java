package com.equiphub.api.dto.approval;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoApprovalResultDTO {

    private boolean autoApproved;
    private String requestId;
    private List<ConditionCheck> conditionChecks;
    private String failureReason;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConditionCheck {
        private String condition;
        private boolean passed;
        private String detail;
    }
}
