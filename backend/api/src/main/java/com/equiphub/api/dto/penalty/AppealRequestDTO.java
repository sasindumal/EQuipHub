package com.equiphub.api.dto.penalty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppealRequestDTO {

    @NotNull(message = "Penalty ID is required")
    private Integer penaltyId;

    @NotBlank(message = "Appeal reason is required")
    private String appealReason;

    private String evidenceDocuments;
}
