package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "requestmodifications",
       indexes = {
           @Index(name = "idx_modifications_request", columnList = "requestid"),
           @Index(name = "idx_modifications_proposer", columnList = "proposedbyid"),
           @Index(name = "idx_modifications_status", columnList = "responsestatus")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestModification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modificationid")
    private Integer modificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestid", nullable = false)
    private Request request;

    @Column(name = "proposedbyid", nullable = false)
    private UUID proposedById;

    @Column(name = "proposedbyrole", nullable = false, length = 50)
    private String proposedByRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "modificationtype", nullable = false, length = 50)
    private ModificationType modificationType;

    @Column(name = "originalvalue")
    private String originalValueJson;

    @Column(name = "proposedvalue")
    private String proposedValueJson;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsestatus", length = 50)
    private ResponseStatus responseStatus = ResponseStatus.PENDING;

    @Column(name = "studentresponse", length = 50)
    private String studentResponse; // ACCEPTED/REJECTED

    @Column(name = "studentresponseat")
    private LocalDateTime studentResponseAt;

    @Column(name = "expiresat", nullable = false)
    private LocalDateTime expiresAt;

    public enum ModificationType {
        EQUIPMENTQUANTITY,
        EQUIPMENTSUBSTITUTE,
        DATESPROPOSED,
        EQUIPMENTREMOVAL
    }

    public enum ResponseStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }
}
