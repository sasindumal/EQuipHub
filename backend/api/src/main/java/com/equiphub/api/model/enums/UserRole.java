package com.equiphub.api.model.enums;

/**
 * Standalone enum mirroring {@link com.equiphub.api.model.User.Role}.
 *
 * <p>Used in DTOs (e.g. {@code UpdateUserRequest}) so that Jackson can
 * deserialize the {@code role} field independently of the inner-class enum,
 * which some Jackson versions struggle to resolve correctly.</p>
 *
 * <p>Values are kept in exact sync with {@code User.Role}. When a new role
 * is added to {@code User.Role} it MUST be added here too.</p>
 */
public enum UserRole {
    SYSTEMADMIN,
    DEPARTMENTADMIN,
    HEADOFDEPARTMENT,
    LECTURER,
    INSTRUCTOR,
    APPOINTEDLECTURER,
    TECHNICALOFFICER,
    STUDENT;

    /**
     * Converts this {@code UserRole} to the equivalent {@link com.equiphub.api.model.User.Role}.
     * Relies on identical name matching so a compile-time mismatch will surface
     * as an {@link IllegalArgumentException} at first use.
     */
    public com.equiphub.api.model.User.Role toUserRole() {
        return com.equiphub.api.model.User.Role.valueOf(this.name());
    }
}
