package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requestitems",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_requestitemequipment",
                             columnNames = {"requestid", "equipmentid"})
       },
       indexes = {
           @Index(name = "idx_requestitems_request", columnList = "requestid"),
           @Index(name = "idx_requestitems_equipment", columnList = "equipmentid"),
           @Index(name = "idx_requestitems_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestitemid")
    private Integer requestItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestid", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipmentid", nullable = false)
    private Equipment equipment;

    @Column(name = "quantityrequested", nullable = false)
    private Integer quantityRequested;

    @Column(name = "quantityapproved")
    private Integer quantityApproved;

    @Column(name = "quantityissued")
    private Integer quantityIssued;

    @Column(name = "quantityreturned")
    private Integer quantityReturned;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "notes")
    private String notes;

    public enum ItemStatus {
        PENDING,
        APPROVED,
        ISSUED,
        RETURNED,
        DAMAGED,
        LOST,
        CANCELLED
    }
}
