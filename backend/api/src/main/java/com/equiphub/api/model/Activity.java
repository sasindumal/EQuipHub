package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activities",
       indexes = {
           @Index(name = "idx_activities_department", columnList = "departmentid"),
           @Index(name = "idx_activities_active", columnList = "isactive")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activityid")
    private Integer activityId;

    @Column(name = "activitycode", nullable = false, unique = true, length = 50)
    private String activityCode;

    @Column(name = "activityname", nullable = false, length = 255)
    private String activityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid")
    private Department department;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "isactive")
    private Boolean active = Boolean.TRUE;
}