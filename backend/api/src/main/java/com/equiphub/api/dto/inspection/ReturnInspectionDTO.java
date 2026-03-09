package com.equiphub.api.dto.inspection;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnInspectionDTO {

    @NotNull(message = "Request ID is required")
    private String requestId;

    @NotEmpty(message = "At least one item inspection is required")
    private List<ItemReturnInspection> items;

    private String overallNotes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemReturnInspection {

        @NotNull(message = "Request item ID is required")
        private Integer requestItemId;

        @NotNull(message = "Quantity returned is required")
        @Min(value = 0, message = "Quantity must be ≥ 0")
        private Integer quantityReturned;

        @NotNull(message = "Condition after is required")
        @Min(0) @Max(100)
        private Integer conditionAfter;

        @Min(0) @Max(5)
        private Integer damageLevel;

        private String damageDescription;

        private String damagePhotos;

        private String notes;
    }
}
