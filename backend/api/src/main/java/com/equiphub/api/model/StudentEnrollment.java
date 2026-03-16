package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "studentenrollments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_studentcoursesemester",
                             columnNames = {"studentid", "courseid", "semesteryear", "semesternumber"})
       },
       indexes = {
           @Index(name = "idx_enrollments_student", columnList = "studentid"),
           @Index(name = "idx_enrollments_course", columnList = "courseid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentEnrollment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollmentid")
    private Integer enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentid", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseid", nullable = false)
    private Course course;

    @Column(name = "semesteryear", nullable = false)
    private Integer semesterYear; // Academic year

    @Column(name = "semesternumber", nullable = false)
    private Integer semesterNumber; // 1 or 2

    @Column(name = "enrollmentdate", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE"; // ACTIVE, DROPPED, COMPLETED
}
