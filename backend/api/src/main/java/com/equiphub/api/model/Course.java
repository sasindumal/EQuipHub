package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses",
       indexes = {
           @Index(name = "idx_courses_department", columnList = "departmentid"),
           @Index(name = "idx_courses_semester", columnList = "semesteroffered"),
           @Index(name = "idx_courses_code", columnList = "coursecode")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Id
    @Column(name = "courseid", length = 20)
    private String courseId; // CS301

    @Column(name = "coursecode", nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(name = "coursename", nullable = false, length = 255)
    private String courseName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid", nullable = false)
    private Department department;

    @Column(name = "semesteroffered", nullable = false)
    private Integer semesterOffered;

    @Column(name = "credits", nullable = false)
    private Double credits;

    @Column(name = "labrequired")
    private Boolean labRequired = Boolean.FALSE;
}