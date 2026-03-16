package com.equiphub.api.dto.user;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String role;
    private String status;
    private UUID departmentId;
    private String departmentName;
    private String departmentCode;
    private Boolean emailVerified;
    private String indexNumber;
    private Integer semesterYear;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
