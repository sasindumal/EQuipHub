package com.equiphub.api.dto.approval;

import com.equiphub.api.model.RequestApproval;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponseDTO {

    private Integer approvalId;
    private String requestId;
    private RequestApproval.ApprovalStage approvalStage;
    private String approvalStageName;
    private UUID actorId;
    private String actorName;
    private String actorEmail;
    private String actorRole;
    private RequestApproval.ApprovalAction action;
    private com.equiphub.api.model.ApprovalDecision decision;
    private String reason;
    private String comments;
    private LocalDateTime decidedAt;
}
