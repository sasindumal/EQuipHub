package com.equiphub.api.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {

    @NotBlank(message = "Department code is required")
    @Size(min = 2, max = 10, message = "Code must be 2–10 characters")
    @Pattern(regexp = "^[A-Z]+$", message = "Code must be uppercase letters only (e.g., CSE, EEE)")
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(min = 5, max = 200, message = "Name must be 5–200 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
