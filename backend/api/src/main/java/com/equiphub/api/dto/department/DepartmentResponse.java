package com.equiphub.api.dto.department;  // ← must match directory path

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DepartmentResponse {
    private UUID departmentId;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private UUID hodId;
    private String hodName;
    private UUID adminId;
    private String adminName;
    private long totalStaff;
    private long totalStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
