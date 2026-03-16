package com.equiphub.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "departmentconfiguration",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_config_department", columnNames = "departmentid")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "configid")
    private Integer configId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentid", nullable = false, unique = true)
    private Department department;

    @Column(name = "maxretentioncoursework")
    private Integer maxRetentionCoursework = 7;

    @Column(name = "maxretentionresearch")
    private Integer maxRetentionResearch = 30;

    @Column(name = "maxretentionextracurricular")
    private Integer maxRetentionExtracurricular = 7;

    @Column(name = "maxretentionpersonal")
    private Integer maxRetentionPersonal = 3;

    @Column(name = "penaltyratelateptsday")
    private Integer penaltyRateLatePtsDay = 10;

    @Column(name = "penaltyrateoverrideptsday")
    private Integer penaltyRateOverridePtsDay = 50;

    @Column(name = "autoapprovalenabled")
    private Boolean autoApprovalEnabled = Boolean.TRUE;

    @Column(name = "autoapprovalvaluelimit")
    private Double autoApprovalValueLimit = 5000.0;

    @Column(name = "autoapprovalgrademinimum", length = 2)
    private String autoApprovalGradeMinimum = "C";

    @Column(name = "updatedbyid")
    private UUID updatedById;

    @Column(name = "updatedat", nullable = false)
    private java.time.LocalDateTime updatedAt;
}
