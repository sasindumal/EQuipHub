package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "labsessiondetails",
       indexes = {
           @Index(name = "idx_labsessions_request", columnList = "requestid"),
           @Index(name = "idx_labsessions_course", columnList = "courseid"),
           @Index(name = "idx_labsessions_date", columnList = "sessiondate"),
           @Index(name = "idx_labsessions_instructor", columnList = "labinstructorid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabSessionDetails extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "labsessionid")
    private Integer labSessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestid", nullable = false, unique = true)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseid", nullable = false)
    private Course course;

    @Column(name = "labinstructorid", nullable = false)
    private UUID labInstructorId;

    @Column(name = "courselecturerid", nullable = false)
    private UUID courseLecturerId;

    @Column(name = "sessiondate", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "timeslot", nullable = false, length = 10)
    private String timeSlot; // "08-12" or "13-16"

    @Column(name = "studentcount", nullable = false)
    private Integer studentCount;

    @Column(name = "advancenoticehours", nullable = false)
    private Integer advanceNoticeHours;

    @Column(name = "isemergency")
    private Boolean emergency = Boolean.FALSE;

    @Column(name = "emergencyjustification")
    private String emergencyJustification;

    @Column(name = "hodapprovalid")
    private Integer hodApprovalId;

    @Column(name = "equipmentprepared")
    private Boolean equipmentPrepared = Boolean.FALSE;

    @Column(name = "preparedbyid")
    private UUID preparedById;

    @Column(name = "preparedat")
    private LocalDateTime preparedAt;
}
