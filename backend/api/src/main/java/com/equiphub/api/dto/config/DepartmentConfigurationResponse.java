package com.equiphub.api.dto.config;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DepartmentConfigurationResponse {

    private Integer configId;
    private UUID    departmentId;
    private String  departmentName;
    private String  departmentCode;

    // Retention policies (days)
    private Integer maxRetentionCoursework;
    private Integer maxRetentionResearch;
    private Integer maxRetentionExtracurricular;
    private Integer maxRetentionPersonal;

    // Penalty rates (points per day)
    private Integer penaltyRateLatePtsDay;
    private Integer penaltyRateOverridePtsDay;

    // Auto-approval settings
    private Boolean autoApprovalEnabled;
    private Double  autoApprovalValueLimit;
    private String  autoApprovalGradeMinimum;

    // Metadata
    private UUID          updatedById;
    private LocalDateTime updatedAt;
    private boolean       isDefault; // true = not yet initialized, showing system defaults
}
