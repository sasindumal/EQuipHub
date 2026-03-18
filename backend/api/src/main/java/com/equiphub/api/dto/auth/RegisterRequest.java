package com.equiphub.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.equiphub.api.model.User.Role;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Role is required")
    private Role role;

    // Student-specific fields
    @NotNull(message = "Semester year is required")
    @Min(value = 1, message = "Semester year must be at least 1")
    @Max(value = 8, message = "Semester year cannot exceed 8")
    private Integer semesterYear;

    /**
     * University registration number format: YYYYEXXX
     * Examples: 2022E001, 2021E123
     * YYYY = 4-digit enrollment year, E = faculty code letter, XXX = 3-digit sequence
     */
    @NotBlank(message = "Index number is required")
    @Pattern(
        regexp = "^\\d{4}[A-Za-z]\\d{3}$",
        message = "Please enter the full registration number (e.g., 2022E001)"
    )
    private String indexNumber;
}
