package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "equipment",
       indexes = {
           @Index(name = "idx_equipment_department", columnList = "departmentid"),
           @Index(name = "idx_equipment_status", columnList = "status"),
           @Index(name = "idx_equipment_type", columnList = "type"),
           @Index(name = "idx_equipment_category", columnList = "categoryid"),
           @Index(name = "idx_equipment_location", columnList = "currentlocation"),
           @Index(name = "idx_equipment_serial", columnList = "serialnumber")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "equipmentid", nullable = false, updatable = false)      
    private UUID equipmentId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryid", nullable = false)
    private EquipmentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private EquipmentType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid", nullable = false)
    private Department department;

    @Column(name = "description")
    private String description;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specificationsJson;

    @Column(name = "purchasedate")
    private LocalDate purchaseDate;

    @Column(name = "purchasevalue")
    private BigDecimal purchaseValue;

    @Column(name = "serialnumber", unique = true, length = 100)
    private String serialNumber;

    @Column(name = "currentcondition", nullable = false)
    private Integer currentCondition = 0;

    @Column(name = "conditionnotes")
    private String conditionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(name = "totalquantity", nullable = false)
    private Integer totalQuantity = 1;

    @Column(name = "currentlocation", nullable = false, length = 100)
    private String currentLocation;

    @Column(name = "assignedlabs")
    private String assignedLabs; // text/array

    @Column(name = "lastmaintenancedate")
    private LocalDate lastMaintenanceDate;

    @Column(name = "nextmaintenancedate")
    private LocalDate nextMaintenanceDate;

    @Column(name = "maintenanceintervaldays")
    private Integer maintenanceIntervalDays;

    @Column(name = "depreciationrate")
    private Integer depreciationRate;

    @Column(name = "replacementcost")
    private BigDecimal replacementCost;

    @Column(name = "isretired")
    private Boolean retired = Boolean.FALSE;

    public enum EquipmentType {
        LABDEDICATED,
        BORROWABLE
    }

    public enum EquipmentStatus {
        AVAILABLE,
        RESERVED,
        INUSE,
        MAINTENANCE,
        DAMAGED,
        ARCHIVED
    }
}
