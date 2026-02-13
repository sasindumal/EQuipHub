package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "equipmentcategories",
       indexes = {
           @Index(name = "idx_equipmentcategories_name", columnList = "name")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryid")
    private Integer categoryId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "damagemultiplierbase")
    private Double damageMultiplierBase = 1.0;

    @Column(name = "typicalreplacementcost")
    private BigDecimal typicalReplacementCost;
}
