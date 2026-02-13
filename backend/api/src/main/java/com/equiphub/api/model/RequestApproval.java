package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requestapprovals",
       indexes = {
           @Index(name = "idx_approvals_request", columnList = "requestid"),
           @Index(name = "idx_approvals_actor", columnList = "actorid"),
           @Index(name = "idx_approvals_stage", columnList = "approvalstage"),
           @Index(name = "idx_approvals_date", columnList = "decidedat")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approvalid")
    private Integer approvalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestid", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(name = "approvalstage", nullable = false, length = 50)
    private ApprovalStage approvalStage;

    @Column(name = "actorid", nullable = false)
    private UUID actorId;

    @Column(name = "actorrole", nullable = false, length = 50)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ApprovalAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 50)
    private ApprovalDecision decision;

    @Column(name = "reason")
    private String reason;

    @Column(name = "comments")
    private String comments;

    @Column(name = "decidedat", nullable = false)
    private LocalDateTime decidedAt;

    public enum ApprovalStage {
        INSTRUCTORRECOMMENDATION,
        LECTURERAPPROVAL,
        SUPERVISORRECOMMENDATION,
        APPOINTEDLECTURERAPPROVAL,
        HODEMERGENCYAPPROVAL,
        TOAVAILABILITYCHECK,
        DEPARTMENTADMINREVIEW
    }

    public enum ApprovalAction {
        RECOMMEND,
        APPROVE,
        REJECT,
        MODIFY,
        REVERSE
    }

    public enum ApprovalDecision {
        PENDING,
        APPROVED,
        REJECTED,
        RECOMMENDED,
        MODIFIED
    }
}
