package com.equiphub.api.controller;

import com.equiphub.api.dto.config.DepartmentConfigurationRequest;
import com.equiphub.api.dto.config.DepartmentConfigurationResponse;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.dto.user.UserResponse;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.DepartmentConfigurationService;
import com.equiphub.api.service.DepartmentService;
import com.equiphub.api.service.UserManagementService;
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
@RequestMapping("/department-admin")
@PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Department Admin", description = "Endpoints for DEPARTMENTADMIN and HOD to manage their own department")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentAdminController {

    private final DepartmentService              departmentService;
    private final DepartmentConfigurationService configService;
    private final UserManagementService          userManagementService;

    // ─────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> ok(Object data, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helper: resolve and validate caller's department
    // ─────────────────────────────────────────────────────────────
    private UUID getCallerDepartmentId(CustomUserDetails currentUser) {
        if (currentUser.getRole() == User.Role.SYSTEMADMIN) {
            throw new IllegalStateException("SYSTEMADMIN should use /api/v1/admin endpoints");
        }
        if (currentUser.getDepartmentId() == null) {
            throw new RuntimeException("Your account has no department assigned. Contact SYSTEMADMIN.");
        }
        return UUID.fromString(currentUser.getDepartmentId());
    }

    // ═════════════════════════════════════════════════════════════
    //  MY DEPARTMENT — info & management
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/my-department")
    @Operation(summary = "Get my department details", description = "Returns full details of the caller's department.")
    public ResponseEntity<Map<String, Object>> getMyDepartment(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        DepartmentResponse dept = departmentService.getDepartmentById(deptId);
        return ok(dept, "Department retrieved successfully");
    }

    @PutMapping("/my-department")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Update my department",
        description = "DEPARTMENTADMIN can update name, description. SYSTEMADMIN uses /admin instead."
    )
    public ResponseEntity<Map<String, Object>> updateMyDepartment(
            @Valid @RequestBody UpdateDepartmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        DepartmentResponse updated = departmentService.updateDepartment(
                deptId, request, currentUser.getUserId()
        );
        log.info("[DEPT_SELF_UPDATE] {} updated their department", currentUser.getEmail());
        return ok(updated, "Department updated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  DEPARTMENT USERS
    //  Canonical paths:  /my-department/users
    //                    /my-department/users/staff
    //                    /my-department/users/students
    //  Short aliases  :  /my-department/staff
    //                    /my-department/students
    //                    /my-department/members  (all users alias)
    // ═════════════════════════════════════════════════════════════

    /** Canonical — all users */
    @GetMapping("/my-department/users")
    @Operation(summary = "Get all users in my department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentUsers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> users = userManagementService.getUsersByDepartment(deptId);
        return ok(Map.of("users", users, "count", users.size()),
                  "Department users retrieved successfully");
    }

    /** Short alias — all users */
    @GetMapping("/my-department/members")
    @Operation(summary = "Get all users in my department (alias for /users)")
    public ResponseEntity<Map<String, Object>> getMyDepartmentMembers(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> users = userManagementService.getUsersByDepartment(deptId);
        return ok(Map.of("users", users, "count", users.size()),
                  "Department users retrieved successfully");
    }

    /** Canonical — staff only */
    @GetMapping("/my-department/users/staff")
    @Operation(summary = "Get staff members in my department (non-students)")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStaff(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> staff = userManagementService.getStaffByDepartment(deptId);
        return ok(Map.of("staff", staff, "count", staff.size()),
                  "Department staff retrieved successfully");
    }

    /** Short alias — staff only */
    @GetMapping("/my-department/staff")
    @Operation(summary = "Get staff members in my department (alias for /users/staff)")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStaffAlias(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> staff = userManagementService.getStaffByDepartment(deptId);
        return ok(Map.of("staff", staff, "count", staff.size()),
                  "Department staff retrieved successfully");
    }

    /** Canonical — students only */
    @GetMapping("/my-department/users/students")
    @Operation(summary = "Get students in my department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStudents(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> students = userManagementService.getStudentsByDepartment(deptId);
        return ok(Map.of("students", students, "count", students.size()),
                  "Department students retrieved successfully");
    }

    /** Short alias — students only */
    @GetMapping("/my-department/students")
    @Operation(summary = "Get students in my department (alias for /users/students)")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStudentsAlias(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        List<UserResponse> students = userManagementService.getStudentsByDepartment(deptId);
        return ok(Map.of("students", students, "count", students.size()),
                  "Department students retrieved successfully");
    }

    @GetMapping("/my-department/stats")
    @Operation(summary = "Get user statistics for my department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStats(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        Map<String, Object> stats = userManagementService.getDepartmentStats(deptId);
        return ok(stats, "Department statistics retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  DEPARTMENT CONFIGURATION
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/my-department/config")
    @Operation(
        summary = "Get my department's configuration",
        description = "Returns current configuration. If not initialized, returns system defaults with isDefault=true."
    )
    public ResponseEntity<Map<String, Object>> getMyDepartmentConfig(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        DepartmentConfigurationResponse config = configService.getByDepartmentId(deptId);
        String message = config.isDefault()
                ? "No custom configuration set — showing system defaults"
                : "Department configuration retrieved successfully";
        return ok(config, message);
    }

    @PutMapping("/my-department/config")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Update my department's configuration",
        description = "DEPARTMENTADMIN can update retention policies, penalties, and auto-approval settings."
    )
    public ResponseEntity<Map<String, Object>> updateMyDepartmentConfig(
            @Valid @RequestBody DepartmentConfigurationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        DepartmentConfigurationResponse config =
                configService.updateConfiguration(deptId, request, currentUser.getUserId());

        log.info("[CONFIG_DEPT_UPDATE] {} updated config for their department", currentUser.getEmail());
        return ok(config, "Department configuration updated successfully");
    }

    @PostMapping("/my-department/config/reset")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Reset my department's configuration to defaults",
        description = "Resets all configuration values back to v3.8 system defaults."
    )
    public ResponseEntity<Map<String, Object>> resetMyDepartmentConfig(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = getCallerDepartmentId(currentUser);
        DepartmentConfigurationResponse config = configService.resetToDefaults(deptId, currentUser.getUserId());

        log.info("[CONFIG_RESET] {} reset their department config to defaults", currentUser.getEmail());
        return ok(config, "Department configuration reset to system defaults");
    }

    // ═════════════════════════════════════════════════════════════
    //  VIEW OTHER DEPARTMENTS (read-only, all roles)
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/departments")
    @Operation(summary = "List all active departments", description = "Read-only. Available to all authenticated admins.")
    public ResponseEntity<Map<String, Object>> getActiveDepartments() {
        List<DepartmentResponse> departments = departmentService.getActiveDepartments();
        return ok(
            Map.of("departments", departments, "count", departments.size()),
            "Active departments retrieved"
        );
    }

    @GetMapping("/departments/{departmentId}")
    @Operation(summary = "Get any department by ID (read-only)")
    public ResponseEntity<Map<String, Object>> getDepartmentById(@PathVariable UUID departmentId) {
        DepartmentResponse dept = departmentService.getDepartmentById(departmentId);
        return ok(dept, "Department retrieved successfully");
    }
}
