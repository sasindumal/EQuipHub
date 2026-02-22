package com.equiphub.api.dto.department;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {

    @Size(min = 5, max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    private Boolean isActive;

    private String hodId;     // UUID of HOD user to assign
    private String adminId;   // UUID of Dept Admin to assign
}
