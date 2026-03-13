package com.equiphub.api.controller;

import com.equiphub.api.dto.config.DepartmentConfigurationRequest;
import com.equiphub.api.dto.config.DepartmentConfigurationResponse;
import com.equiphub.api.dto.department.CreateDepartmentRequest;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.UserRepository;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.DepartmentConfigurationService;
import com.equiphub.api.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('SYSTEMADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "SYSTEMADMIN-only endpoints — department management, configuration, system dashboard")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final DepartmentService               departmentService;
    private final DepartmentConfigurationService  configService;
    private final DepartmentRepository            departmentRepository;
    private final UserRepository                  userRepository;

    // ─────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> ok(Object data, String message) {
        return build(HttpStatus.OK, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> created(Object data, String message) {
        return build(HttpStatus.CREATED, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return build(HttpStatus.BAD_REQUEST, null, message, false);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, Object data,
                                                       String message, boolean success) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        if (data != null) body.put("data", data);
        return ResponseEntity.status(status).body(body);
    }

    // ═════════════════════════════════════════════════════════════
    //  SYSTEM DASHBOARD
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    @Operation(
        summary = "System dashboard stats",
        description = "Returns system-wide counts: departments, users by role, equipment, pending requests."
    )
    public ResponseEntity<Map<String, Object>> getDashboard() {
        long totalDepartments  = departmentRepository.count();
        long activeDepartments = departmentRepository.findAllByIsActiveTrue().size();
        long totalUsers        = userRepository.count();

        // User counts by role
        Map<String, Long> usersByRole = new HashMap<>();
        for (var role : com.equiphub.api.model.User.Role.values()) {
            long count = userRepository.findByRole(role).stream()
                    .filter(u -> u.getDeletedAt() == null).count();
            if (count > 0) usersByRole.put(role.name(), count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDepartments",  totalDepartments);
        stats.put("activeDepartments", activeDepartments);
        stats.put("totalUsers",        totalUsers);
        stats.put("usersByRole",       usersByRole);
        stats.put("generatedAt",       LocalDateTime.now());

        return ok(stats, "Dashboard data retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  DEPARTMENT MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    @PostMapping("/departments")
    @Operation(
        summary = "Create a department",
        description = "Creates a new department. Optionally auto-initializes a default configuration."
    )
    public ResponseEntity<Map<String, Object>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            @RequestParam(defaultValue = "true") boolean initConfig,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DepartmentResponse dept = departmentService.createDepartment(request, currentUser.getUserId());
        log.info("[DEPT_CREATE] '{}' created by {}", dept.getCode(), currentUser.getEmail());

        DepartmentConfigurationResponse config = null;
        if (initConfig) {
            config = configService.initializeConfiguration(dept.getDepartmentId(), currentUser.getUserId());
            log.info("[CONFIG_AUTO_INIT] Default config created for '{}'", dept.getCode());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("department",     dept);
        result.put("configuration",  config);
        result.put("configCreated",  config != null);

        return created(result, "Department created successfully" +
                (initConfig ? " with default configuration" : " (no configuration initialized)"));
    }

    @GetMapping("/departments")
    @Operation(
        summary = "Get all departments",
        description = "Returns all departments (active and inactive) with user counts."
    )
    public ResponseEntity<Map<String, Object>> getAllDepartments(
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        List<DepartmentResponse> departments = activeOnly
                ? departmentService.getActiveDepartments()
                : departmentService.getAllDepartments();

        return ok(
            Map.of("departments", departments, "count", departments.size()),
            "Departments retrieved successfully"
        );
    }

    // Bug fix #5: Wrapped in try/catch to return a proper 404 response when the
    // department ID is not found, instead of propagating an unhandled RuntimeException
    // that Spring converts to a 500 Internal Server Error.
    @GetMapping("/departments/{departmentId}")
    @Operation(summary = "Get a department by ID")
    public ResponseEntity<Map<String, Object>> getDepartmentById(@PathVariable UUID departmentId) {
        try {
            DepartmentResponse dept = departmentService.getDepartmentById(departmentId);
            return ok(dept, "Department retrieved successfully");
        } catch (RuntimeException e) {
            return build(HttpStatus.NOT_FOUND, null, e.getMessage(), false);
        }
    }

    @PutMapping("/departments/{departmentId}")
    @Operation(
        summary = "Update a department",
        description = "Update name, description, active status, HOD assignment, or admin assignment."
    )
    public ResponseEntity<Map<String, Object>> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DepartmentResponse updated = departmentService.updateDepartment(
                departmentId, request, currentUser.getUserId()
        );
        log.info("[DEPT_UPDATE] '{}' updated by {}", updated.getCode(), currentUser.getEmail());
        return ok(updated, "Department updated successfully");
    }

    @DeleteMapping("/departments/{departmentId}")
    @Operation(
        summary = "Deactivate a department",
        description = "Soft-deactivates a department. Fails if there are still active users assigned."
    )
    public ResponseEntity<Map<String, Object>> deactivateDepartment(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        departmentService.deactivateDepartment(departmentId, currentUser.getUserId());
        log.warn("[DEPT_DEACTIVATE] departmentId={} deactivated by {}", departmentId, currentUser.getEmail());
        return ok(null, "Department deactivated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  DEPARTMENT CONFIGURATION  (SYSTEMADMIN section)
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/configurations")
    @Operation(
        summary = "Get all department configurations",
        description = "Returns configurations for all departments in a single call — useful for bulk review."
    )
    public ResponseEntity<Map<String, Object>> getAllConfigurations() {
        List<DepartmentConfigurationResponse> configs = configService.getAllConfigurations();
        return ok(
            Map.of("configurations", configs, "count", configs.size()),
            "All configurations retrieved successfully"
        );
    }

    @PostMapping("/departments/{departmentId}/config")
    @Operation(
        summary = "Initialize department configuration",
        description = "Creates a default configuration record for a department. " +
                      "Fails if configuration already exists — use PUT to update."
    )
    public ResponseEntity<Map<String, Object>> initializeConfig(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DepartmentConfigurationResponse config =
                configService.initializeConfiguration(departmentId, currentUser.getUserId());

        log.info("[CONFIG_INIT] departmentId={} by {}", departmentId, currentUser.getEmail());
        return created(config, "Department configuration initialized with system defaults");
    }

    @GetMapping("/departments/{departmentId}/config")
    @Operation(
        summary = "Get configuration for a specific department",
        description = "Returns configuration if initialized, otherwise returns system defaults with isDefault=true."
    )
    public ResponseEntity<Map<String, Object>> getConfig(@PathVariable UUID departmentId) {
        DepartmentConfigurationResponse config = configService.getByDepartmentId(departmentId);
        String message = config.isDefault()
                ? "No custom configuration found — returning system defaults"
                : "Department configuration retrieved successfully";
        return ok(config, message);
    }

    @PutMapping("/departments/{departmentId}/config")
    @Operation(
        summary = "Update department configuration",
        description = "Partially updates configuration. Only provided (non-null) fields are updated."
    )
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable UUID departmentId,
            @Valid @RequestBody DepartmentConfigurationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DepartmentConfigurationResponse config =
                configService.updateConfiguration(departmentId, request, currentUser.getUserId());

        log.info("[CONFIG_UPDATE] departmentId={} by {}", departmentId, currentUser.getEmail());
        return ok(config, "Department configuration updated successfully");
    }

    @PostMapping("/departments/{departmentId}/config/reset")
    @Operation(
        summary = "Reset department configuration to system defaults",
        description = "Resets all configuration fields back to the v3.8 system default values."
    )
    public ResponseEntity<Map<String, Object>> resetConfig(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        DepartmentConfigurationResponse config =
                configService.resetToDefaults(departmentId, currentUser.getUserId());

        log.info("[CONFIG_RESET] departmentId={} reset by {}", departmentId, currentUser.getEmail());
        return ok(config, "Department configuration reset to system defaults");
    }

    // ═════════════════════════════════════════════════════════════
    //  SYSTEM DEFAULT VALUES  (read-only reference endpoint)
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/configurations/defaults")
    @Operation(
        summary = "Get system default configuration values",
        description = "Returns the v3.8 hardcoded default values used when no custom config exists."
    )
    public ResponseEntity<Map<String, Object>> getSystemDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("maxRetentionCoursework",      DepartmentConfigurationService.DEFAULT_MAX_RETENTION_COURSEWORK);
        defaults.put("maxRetentionResearch",         DepartmentConfigurationService.DEFAULT_MAX_RETENTION_RESEARCH);
        defaults.put("maxRetentionExtracurricular",  DepartmentConfigurationService.DEFAULT_MAX_RETENTION_EXTRACURRICULAR);
        defaults.put("maxRetentionPersonal",         DepartmentConfigurationService.DEFAULT_MAX_RETENTION_PERSONAL);
        defaults.put("penaltyRateLatePtsDay",        DepartmentConfigurationService.DEFAULT_PENALTY_LATE_PTS_DAY);
        defaults.put("penaltyRateOverridePtsDay",    DepartmentConfigurationService.DEFAULT_PENALTY_OVERRIDE_PTS_DAY);
        defaults.put("autoApprovalEnabled",          DepartmentConfigurationService.DEFAULT_AUTO_APPROVAL_ENABLED);
        defaults.put("autoApprovalValueLimit",       DepartmentConfigurationService.DEFAULT_AUTO_APPROVAL_VALUE_LIMIT);
        defaults.put("autoApprovalGradeMinimum",     DepartmentConfigurationService.DEFAULT_AUTO_APPROVAL_GRADE_MINIMUM);
        return ok(defaults, "System default configuration values");
    }
}
