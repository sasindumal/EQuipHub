package com.equiphub.api.dto.user;

import com.equiphub.api.model.User;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @Pattern(regexp = "ACTIVE|SUSPENDED|INACTIVE")
    private String status;

    private String departmentId; // reassign to different department

    private User.Role role; // allow role change by admin
}
