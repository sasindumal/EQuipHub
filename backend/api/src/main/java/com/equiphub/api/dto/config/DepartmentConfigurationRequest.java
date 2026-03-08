package com.equiphub.api.dto.config;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DepartmentConfigurationRequest {

    // ── Retention Policies (days) ──────────────────────────────
    @Min(value = 1,  message = "Coursework retention must be at least 1 day")
    @Max(value = 90, message = "Coursework retention cannot exceed 90 days")
    private Integer maxRetentionCoursework;

    @Min(value = 1,   message = "Research retention must be at least 1 day")
    @Max(value = 365, message = "Research retention cannot exceed 365 days")
    private Integer maxRetentionResearch;

    @Min(value = 1,  message = "Extracurricular retention must be at least 1 day")
    @Max(value = 90, message = "Extracurricular retention cannot exceed 90 days")
    private Integer maxRetentionExtracurricular;

    @Min(value = 1,  message = "Personal retention must be at least 1 day")
    @Max(value = 30, message = "Personal retention cannot exceed 30 days")
    private Integer maxRetentionPersonal;

    // ── Penalty Rates (points/day) ─────────────────────────────
    @Min(value = 1,   message = "Late penalty must be at least 1 point per day")
    @Max(value = 100, message = "Late penalty cannot exceed 100 points per day")
    private Integer penaltyRateLatePtsDay;

    @Min(value = 1,   message = "Override penalty must be at least 1 point per day")
    @Max(value = 200, message = "Override penalty cannot exceed 200 points per day")
    private Integer penaltyRateOverridePtsDay;

    // ── Auto-Approval Settings ─────────────────────────────────
    private Boolean autoApprovalEnabled;

    @DecimalMin(value = "0.0",       message = "Auto-approval value limit cannot be negative")
    @DecimalMax(value = "1000000.0", message = "Auto-approval value limit is too large")
    private Double autoApprovalValueLimit;

    @Pattern(
        regexp = "^[A-F][+-]?$",
        message = "Grade minimum must be a valid grade (e.g. A, B+, C-, F)"
    )
    private String autoApprovalGradeMinimum;
}
