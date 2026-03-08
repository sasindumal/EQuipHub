package com.equiphub.api.dto.approval;

import com.equiphub.api.model.RequestApproval;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDecisionDTO {

    @NotNull(message = "Action is required")
    private RequestApproval.ApprovalAction action;

    private String reason;

    private String comments;

    // Optional: if approver wants to modify quantities
    private java.util.List<ItemQuantityModification> itemModifications;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemQuantityModification {
        private Integer requestItemId;
        private Integer approvedQuantity;
    }
}
