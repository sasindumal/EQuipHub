package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "equipmentunavailability",
       indexes = {
           @Index(name = "idx_unavailability_equipment", columnList = "equipmentid"),
           @Index(name = "idx_unavailability_dates", columnList = "fromdate,todate")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unavailabilityid")
    private Integer unavailabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipmentid", nullable = false)
    private Equipment equipment;

    @Column(name = "fromdate", nullable = false)
    private LocalDate fromDate;

    @Column(name = "todate", nullable = false)
    private LocalDate toDate;

    @Column(name = "reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "scheduledbyid", nullable = false)
    private UUID scheduledById;

    @Column(name = "notes")
    private String notes;
}