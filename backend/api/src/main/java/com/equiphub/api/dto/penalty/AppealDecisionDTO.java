package com.equiphub.api.dto.penalty;

import com.equiphub.api.model.PenaltyAppeal.AppealDecision;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealDecisionDTO {

    @NotNull(message = "Decision is required")
    private AppealDecision decision;

    private String decisionReason;
    private Integer pointsWaived;
}
