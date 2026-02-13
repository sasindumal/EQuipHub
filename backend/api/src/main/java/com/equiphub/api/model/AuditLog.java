package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditlog",
       indexes = {
           @Index(name = "idx_audit_user", columnList = "userid"),
           @Index(name = "idx_audit_entity", columnList = "entitytype,entityid"),
           @Index(name = "idx_audit_timestamp", columnList = "timestamp")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditid")
    private Long auditId;

    @Column(name = "userid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entitytype", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entityid", nullable = false, length = 50)
    private String entityId;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changesJson;

    @Column(name = "ipaddress")
    private String ipAddress;

    @Column(name = "useragent", length = 500)
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "retentiondays")
    private Integer retentionDays = 2555; // 7 years approx

    public enum AuditAction {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        APPROVE,
        REJECT,
        EXPORT
    }
}
