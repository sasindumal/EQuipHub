package com.equiphub.api.dto.penalty;

import com.equiphub.api.model.Penalty.PenaltyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePenaltyDTO {

    @NotNull(message = "Request ID is required")
    private String requestId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Penalty type is required")
    private PenaltyType penaltyType;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String calculationDetailsJson;
}
