package com.equiphub.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_dept_code", columnList = "code", unique = true)
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "departmentid", nullable = false, updatable = false)
    private UUID departmentId;

    @NotBlank
    @Size(max = 10)
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code; // e.g., "CSE", "EEE"

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name; // e.g., "Department of Computer Engineering"

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    // HOD linked to user
    @Column(name = "hodid")
    private UUID hodId;

    // Department Admin linked to user
    @Column(name = "adminid")
    private UUID adminId;

    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @Column(name = "createdby")
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
