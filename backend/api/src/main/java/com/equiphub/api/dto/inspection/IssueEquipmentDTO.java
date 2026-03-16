package com.equiphub.api.dto.inspection;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueEquipmentDTO {

    @NotNull(message = "Request ID is required")
    private String requestId;

    @NotEmpty(message = "At least one item to issue is required")
    private List<ItemIssue> items;

    private String issuanceNotes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemIssue {

        @NotNull(message = "Request item ID is required")
        private Integer requestItemId;

        @NotNull(message = "Quantity to issue is required")
        @Min(value = 1, message = "Must issue at least 1")
        private Integer quantityToIssue;

        @NotNull(message = "Condition before (pre-issue) is required")
        @Min(0) @Max(100)
        private Integer conditionBefore;

        private String notes;
    }
}
