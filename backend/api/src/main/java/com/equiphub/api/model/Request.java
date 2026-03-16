package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requests",
       indexes = {
           @Index(name = "idx_requests_student", columnList = "studentid"),
           @Index(name = "idx_requests_status", columnList = "status"),
           @Index(name = "idx_requests_department", columnList = "departmentid"),
           @Index(name = "idx_requests_dates", columnList = "fromdatetime,todatetime"),
           @Index(name = "idx_requests_type", columnList = "requesttype"),
           @Index(name = "idx_requests_priority", columnList = "prioritylevel")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request extends BaseEntity {

    @Id
    @Column(name = "requestid", length = 50)
    private String requestId; // REQ-YYYY-NNNNN

    @Enumerated(EnumType.STRING)
    @Column(name = "requesttype", nullable = false, length = 50)
    private RequestType requestType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentid", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitterid", nullable = false)
    private User submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid", nullable = false)
    private Department department;

    @Column(name = "fromdatetime", nullable = false)
    private LocalDateTime fromDateTime;

    @Column(name = "todatetime", nullable = false)
    private LocalDateTime toDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RequestStatus status = RequestStatus.PENDINGAPPROVAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseid")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisorid")
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructorid")
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activityid")
    private Activity activity;

    @Column(name = "description")
    private String description;

    @Column(name = "rejectionreason")
    private String rejectionReason;

    @Column(name = "submittedat")
    private LocalDateTime submittedAt;

    @Column(name = "approvedat")
    private LocalDateTime approvedAt;

    @Column(name = "returnedat")
    private LocalDateTime returnedAt;

    @Column(name = "completedat")
    private LocalDateTime completedAt;

    @Column(name = "prioritylevel", nullable = false)
    private Integer priorityLevel;

    @Column(name = "slahours", nullable = false)
    private Integer slaHours;

    @Column(name = "extensioncount")
    private Integer extensionCount = 0;

    @Column(name = "maxextensions")
    private Integer maxExtensions = 0;

    @Column(name = "isemergency")
    private Boolean emergency = Boolean.FALSE;

    @Column(name = "emergencyjustification")
    private String emergencyJustification;

    public enum RequestType {
        LABSESSION,
        COURSEWORK,
        RESEARCH,
        EXTRACURRICULAR,
        PERSONAL
    }

    public enum RequestStatus {
        DRAFT,
        PENDINGRECOMMENDATION,
        PENDINGAPPROVAL,
        APPROVED,
        REJECTED,
        MODIFICATIONPROPOSED,
        MODIFICATIONPENDING,
        CANCELLED,
        INUSE,
        RETURNED,
        COMPLETED,
        OVERDUE,
        PENALTYASSESSED
    }
}
