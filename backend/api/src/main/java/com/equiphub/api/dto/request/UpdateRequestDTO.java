package com.equiphub.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateRequestDTO {

    @Future(message = "Start date must be in the future")
    private LocalDateTime fromDateTime;

    @Future(message = "End date must be in the future")
    private LocalDateTime toDateTime;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private Integer courseId;

    private UUID supervisorId;

    private UUID instructorId;

    @Min(value = 1, message = "Priority level must be between 1 and 5")
    @Max(value = 5, message = "Priority level must be between 1 and 5")
    private Integer priorityLevel;

    private Boolean isEmergency;

    @Size(max = 500, message = "Emergency justification cannot exceed 500 characters")
    private String emergencyJustification;

    @Valid
    private List<RequestItemDTO> items;
}
