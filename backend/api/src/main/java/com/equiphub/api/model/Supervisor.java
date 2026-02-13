package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "supervisors",
       indexes = {
           @Index(name = "idx_supervisors_student", columnList = "studentid"),
           @Index(name = "idx_supervisors_supervisor", columnList = "supervisoruserid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supervisor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supervisorid")
    private Integer supervisorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentid", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisoruserid", nullable = false)
    private User supervisorUser;

    @Column(name = "assignmentdate", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "researchtopic", length = 255)
    private String researchTopic;

    @Column(name = "semesterassigned", nullable = false)
    private Integer semesterAssigned;

    @Column(name = "status", length = 50)
    private String status = "ACTIVE";
}
