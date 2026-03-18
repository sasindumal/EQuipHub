package com.equiphub.api.dto.user;

import com.equiphub.api.model.enums.UserRole;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code PUT /users/{userId}}.
 *
 * <p>All fields are optional — only non-null values are applied by the service.
 * {@code role} uses {@link UserRole} (a standalone enum in
 * {@code model.enums}) which Jackson can deserialize reliably and which
 * mirrors {@code User.Role} value-for-value.</p>
 */
@Data
public class UpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    /** Must be one of: ACTIVE, SUSPENDED, INACTIVE */
    @Pattern(regexp = "ACTIVE|SUSPENDED|INACTIVE")
    private String status;

    /** UUID string — reassign user to a different department. */
    private String departmentId;

    /**
     * New role for the user. Deserialized via {@link UserRole} and converted
     * to {@link com.equiphub.api.model.User.Role} in the service layer.
     * Leave null to keep the existing role.
     */
    private UserRole role;
}
