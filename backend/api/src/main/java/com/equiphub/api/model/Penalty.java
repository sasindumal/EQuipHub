package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "penalties",
       indexes = {
           @Index(name = "idx_penalties_student", columnList = "studentid"),
           @Index(name = "idx_penalties_request", columnList = "requestid"),
           @Index(name = "idx_penalties_status", columnList = "status"),
           @Index(name = "idx_penalties_level", columnList = "statuslevel"),
           @Index(name = "idx_penalties_studentstatus", columnList = "studentid,status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Penalty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penaltyid")
    private Integer penaltyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestid", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentid", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "penaltytype", nullable = false, length = 50)
    private PenaltyType penaltyType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "calculationdetails", columnDefinition = "TEXT")
    private String calculationDetailsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PenaltyStatus status = PenaltyStatus.PENDING;

    @Column(name = "approvedbyid")
    private UUID approvedById;

    @Column(name = "approvedat")
    private LocalDateTime approvedAt;

    @Column(name = "totalpointsafter")
    private Integer totalPointsAfter;

    @Column(name = "statuslevel", length = 50)
    private String statusLevel;

    @Column(name = "isappealed")
    private Boolean appealed = Boolean.FALSE;

    @OneToOne(mappedBy = "penalty", fetch = FetchType.LAZY)
    private PenaltyAppeal appeal;

    public enum PenaltyType {
        LATERETURN,
        DAMAGE,
        LABOVERRIDE
    }

    public enum PenaltyStatus {
        PENDING,
        APPROVED,
        APPEALED,
        WAIVED,
        REDUCED
    }
}