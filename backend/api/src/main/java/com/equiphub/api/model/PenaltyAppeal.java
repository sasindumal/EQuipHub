package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "penaltyappeals",
       indexes = {
           @Index(name = "idx_appeals_penalty", columnList = "penaltyid"),
           @Index(name = "idx_appeals_student", columnList = "studentid"),
           @Index(name = "idx_appeals_status", columnList = "appealstatus")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyAppeal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appealid")
    private Integer appealId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penaltyid", nullable = false, unique = true)
    private Penalty penalty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentid", nullable = false)
    private User student;

    @Column(name = "appealreason", nullable = false)
    private String appealReason;

    @Column(name = "evidencedocuments")
    private String evidenceDocuments;

    @Enumerated(EnumType.STRING)
    @Column(name = "appealstatus", nullable = false, length = 50)
    private AppealStatus appealStatus = AppealStatus.PENDING;

    @Column(name = "decidedbyid")
    private UUID decidedById;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 50)
    private AppealDecision decision;

    @Column(name = "decisionreason")
    private String decisionReason;

    @Column(name = "pointswaived")
    private Integer pointsWaived;

    @Column(name = "newtotalpoints")
    private Integer newTotalPoints;

    @Column(name = "appealedat", nullable = false)
    private LocalDateTime appealedAt;

    @Column(name = "decidedat")
    private LocalDateTime decidedAt;

    @Column(name = "appealdeadline", nullable = false)
    private LocalDateTime appealDeadline;

    public enum AppealStatus {
        PENDING,
        UNDERREVIEW,
        APPROVED,
        PARTIALLYAPPROVED,
        REJECTED
    }

    public enum AppealDecision {
        APPROVED,
        REJECTED,
        PARTIALLYWAIVED
    }
}