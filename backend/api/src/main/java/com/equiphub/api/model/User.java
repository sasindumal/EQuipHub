package com.equiphub.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_users_email", columnList = "email"),
           @Index(name = "idx_users_department", columnList = "departmentid"),
           @Index(name = "idx_users_role", columnList = "role"),
           @Index(name = "idx_users_status", columnList = "status"),
           @Index(name = "idx_users_semester", columnList = "semesteryear")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "userid", nullable = false, updatable = false)
    private UUID userId;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "passwordhash", nullable = false, length = 255)
    private String passwordHash;

    @NotBlank
    @Size(max = 100)
    @Column(name = "firstname", nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "lastname", nullable = false)
    private String lastName;

    @Size(max = 20)
    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid")
    private Department department;

    @Column(name = "semesteryear")
    private Integer semesterYear;

    @Size(max = 50)
    @Column(name = "indexnumber", unique = true)
    private String indexNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.ACTIVE;

    @Column(name = "emailverified", nullable = false)
    private Boolean emailVerified = Boolean.FALSE;

    @Column(name = "createdby")
    private UUID createdBy;

    @Column(name = "lastlogin")
    private LocalDateTime lastLogin;

    public enum Role {
        SYSTEMADMIN,
        DEPARTMENTADMIN,
        HEADOFDEPARTMENT,
        LECTURER,
        INSTRUCTOR,
        APPOINTEDLECTURER,
        TECHNICALOFFICER,
        STUDENT
    }

    public enum Status {
        ACTIVE,
        SUSPENDED,
        INACTIVE
    }
}
