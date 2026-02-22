package com.equiphub.api.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.equiphub.api.model.User;

@Data
public class CreateStaffRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "DEPARTMENTADMIN|HEADOFDEPARTMENT|LECTURER|INSTRUCTOR|APPOINTEDLECTURER|TECHNICALOFFICER",
        message = "Role must be one of: DEPARTMENTADMIN, HEADOFDEPARTMENT, LECTURER, INSTRUCTOR, APPOINTEDLECTURER, TECHNICALOFFICER"
    )
    private User.Role role;

    // Required for non-SYSTEMADMIN roles
    private String departmentId;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String temporaryPassword;

    // Optional: send welcome email with credentials
    private boolean sendWelcomeEmail = true;
}
