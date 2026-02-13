package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments",
       indexes = {
           @Index(name = "idx_departments_hod", columnList = "hodid"),
           @Index(name = "idx_departments_admin", columnList = "adminid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @Column(name = "departmentid", length = 10)
    private String departmentId; // e.g. CSE, EEE

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hodid")
    private User hod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminid")
    private User admin;

    @Column(name = "maxretentiondayscoursework", nullable = false)
    private Integer maxRetentionDaysCoursework = 7;

    @Column(name = "maxretentiondaysresearch", nullable = false)
    private Integer maxRetentionDaysResearch = 30;

    @Column(name = "maxretentiondaysextracurricular", nullable = false)
    private Integer maxRetentionDaysExtracurricular = 7;

    @Column(name = "maxretentiondayspersonal", nullable = false)
    private Integer maxRetentionDaysPersonal = 3;

    @Column(name = "penaltyratelatecoursework", nullable = false)
    private Integer penaltyRateLateCoursework = 10;

    @Column(name = "penaltyratelateresearch", nullable = false)
    private Integer penaltyRateLateResearch = 20;

    @Column(name = "penaltyratelatepersonal", nullable = false)
    private Integer penaltyRateLatePersonal = 5;

    @Column(name = "penaltyrateoverride", nullable = false)
    private Integer penaltyRateOverride = 50;

    @Column(name = "damagemultiplierpersonal", nullable = false)
    private Double damageMultiplierPersonal = 2.0;
}
