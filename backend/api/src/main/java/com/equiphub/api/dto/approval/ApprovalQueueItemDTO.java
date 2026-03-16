package com.equiphub.api.dto.approval;

import com.equiphub.api.model.Request;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalQueueItemDTO {

    private String requestId;
    private Request.RequestType requestType;
    private String studentName;
    private String studentIndexNumber;
    private String departmentName;
    private String courseName;
    private String description;
    private Integer priorityLevel;
    private Boolean emergency;
    private String emergencyJustification;
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
    private LocalDateTime submittedAt;
    private Integer slaHours;
    private LocalDateTime slaDeadline;
    private Boolean slaBreached;
    private Request.RequestStatus currentStatus;
    private String pendingStage;
    private Integer totalItems;
    private Integer totalQuantity;
    private List<String> equipmentNames;
    private List<ApprovalResponseDTO> approvalHistory;
}
