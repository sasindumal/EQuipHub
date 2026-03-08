package com.equiphub.api.service;

import com.equiphub.api.dto.config.DepartmentConfigurationRequest;
import com.equiphub.api.dto.config.DepartmentConfigurationResponse;
import com.equiphub.api.model.Department;
import com.equiphub.api.model.DepartmentConfiguration;
import com.equiphub.api.repository.DepartmentConfigurationRepository;
import com.equiphub.api.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentConfigurationService {

    private final DepartmentConfigurationRepository configRepository;
    private final DepartmentRepository              departmentRepository;

    // ── System-wide defaults (used when no config is initialized) ──
    public static final int    DEFAULT_MAX_RETENTION_COURSEWORK      = 7;
    public static final int    DEFAULT_MAX_RETENTION_RESEARCH        = 30;
    public static final int    DEFAULT_MAX_RETENTION_EXTRACURRICULAR = 7;
    public static final int    DEFAULT_MAX_RETENTION_PERSONAL        = 3;
    public static final int    DEFAULT_PENALTY_LATE_PTS_DAY          = 10;
    public static final int    DEFAULT_PENALTY_OVERRIDE_PTS_DAY      = 50;
    public static final boolean DEFAULT_AUTO_APPROVAL_ENABLED        = true;
    public static final double  DEFAULT_AUTO_APPROVAL_VALUE_LIMIT    = 5000.0;
    public static final String  DEFAULT_AUTO_APPROVAL_GRADE_MINIMUM  = "C";

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE — creates default config for a new department
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public DepartmentConfigurationResponse initializeConfiguration(UUID departmentId, UUID createdBy) {
        Department dept = findDepartment(departmentId);

        if (configRepository.existsByDepartmentDepartmentId(departmentId)) {
            throw new RuntimeException(
                "Configuration already exists for department '" + dept.getName() +
                "'. Use PUT to update it."
            );
        }

        DepartmentConfiguration config = DepartmentConfiguration.builder()
                .department(dept)
                .maxRetentionCoursework(DEFAULT_MAX_RETENTION_COURSEWORK)
                .maxRetentionResearch(DEFAULT_MAX_RETENTION_RESEARCH)
                .maxRetentionExtracurricular(DEFAULT_MAX_RETENTION_EXTRACURRICULAR)
                .maxRetentionPersonal(DEFAULT_MAX_RETENTION_PERSONAL)
                .penaltyRateLatePtsDay(DEFAULT_PENALTY_LATE_PTS_DAY)
                .penaltyRateOverridePtsDay(DEFAULT_PENALTY_OVERRIDE_PTS_DAY)
                .autoApprovalEnabled(DEFAULT_AUTO_APPROVAL_ENABLED)
                .autoApprovalValueLimit(DEFAULT_AUTO_APPROVAL_VALUE_LIMIT)
                .autoApprovalGradeMinimum(DEFAULT_AUTO_APPROVAL_GRADE_MINIMUM)
                .updatedById(createdBy)
                .updatedAt(LocalDateTime.now())
                .build();

        DepartmentConfiguration saved = configRepository.save(config);
        log.info("[CONFIG_INIT] Department '{}' initialized by {}", dept.getName(), createdBy);
        return mapToResponse(saved, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  GET by department ID (returns defaults if not initialized)
    // ─────────────────────────────────────────────────────────────
    public DepartmentConfigurationResponse getByDepartmentId(UUID departmentId) {
        Department dept = findDepartment(departmentId);

        return configRepository.findByDepartmentDepartmentId(departmentId)
                .map(config -> mapToResponse(config, false))
                .orElse(buildDefaultResponse(dept));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET ALL — for system admin overview
    // ─────────────────────────────────────────────────────────────
    public List<DepartmentConfigurationResponse> getAllConfigurations() {
        return configRepository.findAll()
                .stream()
                .map(config -> mapToResponse(config, false))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE — partial update (null fields are skipped)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public DepartmentConfigurationResponse updateConfiguration(
            UUID departmentId,
            DepartmentConfigurationRequest request,
            UUID updatedBy) {

        Department dept = findDepartment(departmentId);

        DepartmentConfiguration config = configRepository
                .findByDepartmentDepartmentId(departmentId)
                .orElseThrow(() -> new RuntimeException(
                    "No configuration found for department '" + dept.getName() +
                    "'. Initialize it first with POST."
                ));

        if (request.getMaxRetentionCoursework() != null)
            config.setMaxRetentionCoursework(request.getMaxRetentionCoursework());
        if (request.getMaxRetentionResearch() != null)
            config.setMaxRetentionResearch(request.getMaxRetentionResearch());
        if (request.getMaxRetentionExtracurricular() != null)
            config.setMaxRetentionExtracurricular(request.getMaxRetentionExtracurricular());
        if (request.getMaxRetentionPersonal() != null)
            config.setMaxRetentionPersonal(request.getMaxRetentionPersonal());
        if (request.getPenaltyRateLatePtsDay() != null)
            config.setPenaltyRateLatePtsDay(request.getPenaltyRateLatePtsDay());
        if (request.getPenaltyRateOverridePtsDay() != null)
            config.setPenaltyRateOverridePtsDay(request.getPenaltyRateOverridePtsDay());
        if (request.getAutoApprovalEnabled() != null)
            config.setAutoApprovalEnabled(request.getAutoApprovalEnabled());
        if (request.getAutoApprovalValueLimit() != null)
            config.setAutoApprovalValueLimit(request.getAutoApprovalValueLimit());
        if (request.getAutoApprovalGradeMinimum() != null)
            config.setAutoApprovalGradeMinimum(request.getAutoApprovalGradeMinimum());

        config.setUpdatedById(updatedBy);
        config.setUpdatedAt(LocalDateTime.now());

        DepartmentConfiguration updated = configRepository.save(config);
        log.info("[CONFIG_UPDATE] Department '{}' config updated by {}", dept.getName(), updatedBy);
        return mapToResponse(updated, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  RESET TO SYSTEM DEFAULTS
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public DepartmentConfigurationResponse resetToDefaults(UUID departmentId, UUID resetBy) {
        Department dept = findDepartment(departmentId);

        DepartmentConfiguration config = configRepository
                .findByDepartmentDepartmentId(departmentId)
                .orElseThrow(() -> new RuntimeException(
                    "No configuration found for department '" + dept.getName() + "'."
                ));

        config.setMaxRetentionCoursework(DEFAULT_MAX_RETENTION_COURSEWORK);
        config.setMaxRetentionResearch(DEFAULT_MAX_RETENTION_RESEARCH);
        config.setMaxRetentionExtracurricular(DEFAULT_MAX_RETENTION_EXTRACURRICULAR);
        config.setMaxRetentionPersonal(DEFAULT_MAX_RETENTION_PERSONAL);
        config.setPenaltyRateLatePtsDay(DEFAULT_PENALTY_LATE_PTS_DAY);
        config.setPenaltyRateOverridePtsDay(DEFAULT_PENALTY_OVERRIDE_PTS_DAY);
        config.setAutoApprovalEnabled(DEFAULT_AUTO_APPROVAL_ENABLED);
        config.setAutoApprovalValueLimit(DEFAULT_AUTO_APPROVAL_VALUE_LIMIT);
        config.setAutoApprovalGradeMinimum(DEFAULT_AUTO_APPROVAL_GRADE_MINIMUM);
        config.setUpdatedById(resetBy);
        config.setUpdatedAt(LocalDateTime.now());

        DepartmentConfiguration saved = configRepository.save(config);
        log.info("[CONFIG_RESET] Department '{}' config reset to defaults by {}", dept.getName(), resetBy);
        return mapToResponse(saved, false);
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private Department findDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
    }

    private DepartmentConfigurationResponse mapToResponse(DepartmentConfiguration config, boolean isDefault) {
        return DepartmentConfigurationResponse.builder()
                .configId(config.getConfigId())
                .departmentId(config.getDepartment().getDepartmentId())
                .departmentName(config.getDepartment().getName())
                .departmentCode(config.getDepartment().getCode())
                .maxRetentionCoursework(config.getMaxRetentionCoursework())
                .maxRetentionResearch(config.getMaxRetentionResearch())
                .maxRetentionExtracurricular(config.getMaxRetentionExtracurricular())
                .maxRetentionPersonal(config.getMaxRetentionPersonal())
                .penaltyRateLatePtsDay(config.getPenaltyRateLatePtsDay())
                .penaltyRateOverridePtsDay(config.getPenaltyRateOverridePtsDay())
                .autoApprovalEnabled(config.getAutoApprovalEnabled())
                .autoApprovalValueLimit(config.getAutoApprovalValueLimit())
                .autoApprovalGradeMinimum(config.getAutoApprovalGradeMinimum())
                .updatedById(config.getUpdatedById())
                .updatedAt(config.getUpdatedAt())
                .isDefault(isDefault)
                .build();
    }

    private DepartmentConfigurationResponse buildDefaultResponse(Department dept) {
        return DepartmentConfigurationResponse.builder()
                .configId(null)
                .departmentId(dept.getDepartmentId())
                .departmentName(dept.getName())
                .departmentCode(dept.getCode())
                .maxRetentionCoursework(DEFAULT_MAX_RETENTION_COURSEWORK)
                .maxRetentionResearch(DEFAULT_MAX_RETENTION_RESEARCH)
                .maxRetentionExtracurricular(DEFAULT_MAX_RETENTION_EXTRACURRICULAR)
                .maxRetentionPersonal(DEFAULT_MAX_RETENTION_PERSONAL)
                .penaltyRateLatePtsDay(DEFAULT_PENALTY_LATE_PTS_DAY)
                .penaltyRateOverridePtsDay(DEFAULT_PENALTY_OVERRIDE_PTS_DAY)
                .autoApprovalEnabled(DEFAULT_AUTO_APPROVAL_ENABLED)
                .autoApprovalValueLimit(DEFAULT_AUTO_APPROVAL_VALUE_LIMIT)
                .autoApprovalGradeMinimum(DEFAULT_AUTO_APPROVAL_GRADE_MINIMUM)
                .isDefault(true)
                .build();
    }
}
