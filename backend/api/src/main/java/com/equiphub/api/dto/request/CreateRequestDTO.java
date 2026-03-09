package com.equiphub.api.dto.request;

import com.equiphub.api.model.Request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateRequestDTO {

    @NotNull(message = "Request type is required")
    private Request.RequestType requestType;

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Start date/time is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime fromDateTime;

    @NotNull(message = "End date/time is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime toDateTime;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Integer courseId;

    private UUID supervisorId;

    private UUID instructorId;

    @NotNull(message = "Priority level is required")
    @Min(value = 1, message = "Priority level must be between 1 and 5")
    @Max(value = 5, message = "Priority level must be between 1 and 5")
    private Integer priorityLevel;

    private Boolean isEmergency = false;

    @Size(max = 500, message = "Emergency justification cannot exceed 500 characters")
    private String emergencyJustification;

    @NotNull(message = "SLA hours is required")
    @Min(value = 1, message = "SLA hours must be at least 1")
    @Max(value = 720, message = "SLA hours cannot exceed 720 (30 days)")
    private Integer slaHours;

    @NotEmpty(message = "At least one request item is required")
    @Valid
    private List<RequestItemDTO> items;
}
