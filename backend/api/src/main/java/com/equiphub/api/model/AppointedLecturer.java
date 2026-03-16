package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "appointedlecturers",
       indexes = {
           @Index(name = "idx_appointedlecturers_lecturer", columnList = "lecturerid"),
           @Index(name = "idx_appointedlecturers_activity", columnList = "activityid"),
           @Index(name = "idx_appointedlecturers_active", columnList = "isactive")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointedLecturer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointedlecturerid")
    private Integer appointedLecturerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturerid", nullable = false)
    private User lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activityid", nullable = false)
    private Activity activity;

    @Column(name = "appointmentdate", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "expirydate")
    private LocalDate expiryDate;

    @Column(name = "isactive")
    private Boolean active = Boolean.TRUE;
}
