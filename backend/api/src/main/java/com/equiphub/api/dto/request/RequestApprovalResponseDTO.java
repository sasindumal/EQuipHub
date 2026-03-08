package com.equiphub.api.dto.request;

import com.equiphub.api.model.RequestApproval;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RequestApprovalResponseDTO {

    private Integer approvalId;
    private RequestApproval.ApprovalStage approvalStage;
    private UUID actorId;
    private String actorName;
    private String actorRole;
    private RequestApproval.ApprovalAction action;
    private RequestApproval.ApprovalDecision decision;
    private String reason;
    private String comments;
    private LocalDateTime decidedAt;
}
