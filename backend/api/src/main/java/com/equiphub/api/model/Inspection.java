package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inspections",
       indexes = {
           @Index(name = "idx_inspections_requestitem", columnList = "requestitemid"),
           @Index(name = "idx_inspections_inspector", columnList = "inspectorid"),
           @Index(name = "idx_inspections_type", columnList = "inspectiontype"),
           @Index(name = "idx_inspections_date", columnList = "inspectedat")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspectionid")
    private Integer inspectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestitemid", nullable = false)
    private RequestItem requestItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspectiontype", nullable = false, length = 50)
    private InspectionType inspectionType;

    @Column(name = "inspectorid", nullable = false)
    private UUID inspectorId;

    @Column(name = "conditionbefore", nullable = false)
    private Integer conditionBefore;

    @Column(name = "conditionafter")
    private Integer conditionAfter;

    @Column(name = "damagelevel")
    private Integer damageLevel;

    @Column(name = "damagedescription")
    private String damageDescription;

    @Column(name = "damagephotos")
    private String damagePhotos; // e.g. JSON/text array

    @Column(name = "studentacknowledged")
    private Boolean studentAcknowledged = Boolean.FALSE;

    @Column(name = "studentacknowledgementat")
    private LocalDateTime studentAcknowledgementAt;

    @Column(name = "predamageevidence")
    private String preDamageEvidence;

    @Column(name = "penaltyapplicable")
    private Boolean penaltyApplicable = Boolean.FALSE;

    @Column(name = "notes")
    private String notes;

    @Column(name = "inspectedat", nullable = false)
    private LocalDateTime inspectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspectionstatus", nullable = false, length = 50)
    private com.equiphub.api.model.InspectionType inspectionSType;

}
