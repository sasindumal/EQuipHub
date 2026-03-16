package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipmentconditionhistory",
       indexes = {
           @Index(name = "idx_conditionhistory_equipment", columnList = "equipmentid"),
           @Index(name = "idx_conditionhistory_date", columnList = "recordedat")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentConditionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historyid")
    private Integer historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipmentid", nullable = false)
    private Equipment equipment;

    @Column(name = "oldcondition", nullable = false)
    private Integer oldCondition;

    @Column(name = "newcondition", nullable = false)
    private Integer newCondition;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "recordedbyid", nullable = false)
    private UUID recordedById;

    @Column(name = "recordedat", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "notes")
    private String notes;
}