package com.equiphub.api.dto.request;

import com.equiphub.api.model.Request;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RequestResponseDTO {

    private String requestId;
    private Request.RequestType requestType;
    private Request.RequestStatus status;

    // Student info
    private UUID studentId;
    private String studentName;
    private String studentEmail;

    // Submitter info
    private UUID submitterId;
    private String submitterName;

    // Department info
    private UUID departmentId;
    private String departmentName;
    private String departmentCode;

    // Schedule
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;

    // Description & details
    private String description;
    private String courseId;
    private String courseName;
    private UUID supervisorId;
    private String supervisorName;
    private UUID instructorId;
    private String instructorName;

    // Priority & SLA
    private Integer priorityLevel;
    private Integer slaHours;
    private LocalDateTime slaDeadline;
    private boolean slaBreached;

    // Emergency
    private boolean emergency;
    private String emergencyJustification;

    // Extensions
    private Integer extensionCount;
    private Integer maxExtensions;

    // Rejection
    private String rejectionReason;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime returnedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Items
    private List<RequestItemResponseDTO> items;

    // Approvals
    private List<RequestApprovalResponseDTO> approvals;
}
