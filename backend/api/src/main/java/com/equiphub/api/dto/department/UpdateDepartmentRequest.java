package com.equiphub.api.dto.department;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating a department.
 * @JsonIgnoreProperties(ignoreUnknown = true) ensures fields like "code" sent
 * by the frontend are silently ignored instead of throwing a 400/500.
 * Department codes are immutable identifiers and must not be updated via this endpoint.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDepartmentRequest {

    @Size(min = 5, max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean isActive;

    private String hodId;     // UUID of HOD user to assign
    private String adminId;   // UUID of Dept Admin to assign
}
