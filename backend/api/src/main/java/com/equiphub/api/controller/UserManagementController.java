package com.equiphub.api.controller;

import com.equiphub.api.dto.user.CreateStaffRequest;
import com.equiphub.api.dto.user.ResetPasswordRequest;
import com.equiphub.api.dto.user.UpdateUserRequest;
import com.equiphub.api.dto.user.UserResponse;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users — staff, students, and admins")
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

    private final UserManagementService userManagementService;

    // ─────────────────────────────────────────────────────────────
    //  HELPER: standard response builder
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> ok(Object data, String message) {
        return buildResponse(data, message, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> created(Object data, String message) {
        return buildResponse(data, message, HttpStatus.CREATED);
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(Object data, String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPER: department access guard
    //  Returns true if the caller is SYSTEMADMIN OR belongs to the
    //  given department — used to restrict DEPARTMENTADMIN / HOD.
    // ─────────────────────────────────────────────────────────────
    private boolean hasAccessToDepartment(CustomUserDetails currentUser, UUID targetDeptId) {
        if (currentUser.getRole() == User.Role.SYSTEMADMIN) return true;
        if (currentUser.getDepartmentId() == null) return false;
        return targetDeptId.toString().equals(currentUser.getDepartmentId());
    }

    // ═════════════════════════════════════════════════════════════
    //  1. CREATE STAFF USER
    //     SYSTEMADMIN  → any role, any department
    //     DEPARTMENTADMIN → own department only
    // ═════════════════════════════════════════════════════════════
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Create a staff user",
        description = "SYSTEMADMIN can create any role in any department. " +
                      "DEPARTMENTADMIN can only create users in their own department."
    )
    public ResponseEntity<Map<String, Object>> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // DEPARTMENTADMIN: force-assign their own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN) {
            String callerDeptId = currentUser.getDepartmentId();

            if (request.getDepartmentId() == null || request.getDepartmentId().isBlank()) {
                // auto-assign to their department
                request.setDepartmentId(callerDeptId);
            } else if (!request.getDepartmentId().equals(callerDeptId)) {
                return forbidden("Department Admin can only create users within their own department");
            }
        }

        UserResponse created = userManagementService.createStaff(request, currentUser.getUserId());
        log.info("[USER_CREATE] {} created by {}", created.getEmail(), currentUser.getEmail());
        return created(created, "Staff user created successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  2. GET ALL USERS  (SYSTEMADMIN only — full system view)
    // ═════════════════════════════════════════════════════════════
    @GetMapping
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Returns every user across all departments. SYSTEMADMIN only."
    )
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserResponse> users = userManagementService.getAllUsers();
        return ok(Map.of("users", users, "count", users.size()), "Users retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  3. GET CURRENT USER PROFILE  (any authenticated user)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the authenticated user's own profile.")
    public ResponseEntity<Map<String, Object>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UserResponse profile = userManagementService.getUserById(currentUser.getUserId());
        return ok(profile, "Profile retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  4. GET ALL STAFF  (non-students)
    //     SYSTEMADMIN    → all departments
    //     DEPARTMENTADMIN / HOD → their department only
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
    @Operation(
        summary = "Get all staff users",
        description = "SYSTEMADMIN sees all staff globally. DEPARTMENTADMIN/HOD see their department only."
    )
    public ResponseEntity<Map<String, Object>> getAllStaff(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<UserResponse> staff;

        if (currentUser.getRole() == User.Role.SYSTEMADMIN) {
            staff = userManagementService.getAllStaff();
        } else {
            UUID deptId = UUID.fromString(currentUser.getDepartmentId());
            staff = userManagementService.getStaffByDepartment(deptId);
        }

        return ok(Map.of("staff", staff, "count", staff.size()), "Staff retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  5. GET ALL STUDENTS
    //     SYSTEMADMIN              → all departments
    //     DEPARTMENTADMIN / HOD / LECTURER / INSTRUCTOR → own dept
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR')")
    @Operation(
        summary = "Get all students",
        description = "SYSTEMADMIN sees all. Others are scoped to their own department."
    )
    public ResponseEntity<Map<String, Object>> getAllStudents(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<UserResponse> students;

        if (currentUser.getRole() == User.Role.SYSTEMADMIN) {
            students = userManagementService.getAllStudents();
        } else {
            UUID deptId = UUID.fromString(currentUser.getDepartmentId());
            students = userManagementService.getStudentsByDepartment(deptId);
        }

        return ok(Map.of("students", students, "count", students.size()), "Students retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  6. SEARCH USERS BY KEYWORD
    //     Searches by firstName, lastName, email
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER')")
    @Operation(summary = "Search users", description = "Searches by name or email. Min 2 characters.")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (keyword == null || keyword.trim().length() < 2) {
            return badRequest("Search keyword must be at least 2 characters");
        }

        List<UserResponse> results = userManagementService.searchUsers(keyword.trim());
        return ok(
            Map.of("results", results, "count", results.size(), "keyword", keyword.trim()),
            "Search completed successfully"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  7. GET USERS BY DEPARTMENT
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
    @Operation(
        summary = "Get users by department",
        description = "DEPARTMENTADMIN and HOD can only access their own department."
    )
    public ResponseEntity<Map<String, Object>> getUsersByDepartment(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasAccessToDepartment(currentUser, departmentId)) {
            return forbidden("You can only view users within your own department");
        }

        List<UserResponse> users = userManagementService.getUsersByDepartment(departmentId);
        return ok(Map.of("users", users, "count", users.size()), "Department users retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  8. GET USERS BY ROLE
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Get users by role",
        description = "Valid roles: SYSTEMADMIN, DEPARTMENTADMIN, HEADOFDEPARTMENT, LECTURER, " +
                      "INSTRUCTOR, APPOINTEDLECTURER, TECHNICALOFFICER, STUDENT"
    )
    public ResponseEntity<Map<String, Object>> getUsersByRole(
            @PathVariable String role,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        try {
            User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid role: '" + role + "'. Valid roles: " +
                              java.util.Arrays.toString(User.Role.values()));
        }

        List<UserResponse> users = userManagementService.getUsersByRole(role.toUpperCase());
        return ok(
            Map.of("users", users, "count", users.size(), "role", role.toUpperCase()),
            "Users retrieved by role"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  9. GET USER BY ID
    //     Any admin can look up any user.
    //     A regular user can look up themselves only.
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','TECHNICALOFFICER') " +
                  "or #userId == authentication.principal.userId")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UserResponse target = userManagementService.getUserById(userId);

        // DEPARTMENTADMIN / HOD: only view their own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN ||
            currentUser.getRole() == User.Role.HEADOFDEPARTMENT) {

            if (target.getDepartmentId() != null &&
                !target.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only view users within your own department");
            }
        }

        return ok(target, "User retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  10. UPDATE USER
    //      SYSTEMADMIN   → any user
    //      DEPARTMENTADMIN → own department only
    // ═════════════════════════════════════════════════════════════
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Update a user",
        description = "Update name, phone, status, or department assignment."
    )
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // DEPARTMENTADMIN: only update users in their own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN) {
            UserResponse target = userManagementService.getUserById(userId);
            if (target.getDepartmentId() != null &&
                !target.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only update users within your own department");
            }
        }

        UserResponse updated = userManagementService.updateUser(userId, request, currentUser.getUserId());
        log.info("[USER_UPDATE] userId={} by {}", userId, currentUser.getEmail());
        return ok(updated, "User updated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  11. SUSPEND USER
    // ═════════════════════════════════════════════════════════════
    @PatchMapping("/{userId}/suspend")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(summary = "Suspend a user", description = "Sets status to SUSPENDED. Blocks login immediately.")
    public ResponseEntity<Map<String, Object>> suspendUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Prevent self-suspension
        if (userId.equals(currentUser.getUserId())) {
            return badRequest("You cannot suspend your own account");
        }

        // DEPARTMENTADMIN: only own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN) {
            UserResponse target = userManagementService.getUserById(userId);
            if (target.getDepartmentId() != null &&
                !target.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only suspend users within your own department");
            }
        }

        UserResponse suspended = userManagementService.suspendUser(userId, currentUser.getUserId());
        log.info("[USER_SUSPEND] userId={} by {}", userId, currentUser.getEmail());
        return ok(suspended, "User suspended successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  12. ACTIVATE USER
    // ═════════════════════════════════════════════════════════════
    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(summary = "Activate a suspended user", description = "Sets status back to ACTIVE.")
    public ResponseEntity<Map<String, Object>> activateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // DEPARTMENTADMIN: only own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN) {
            UserResponse target = userManagementService.getUserById(userId);
            if (target.getDepartmentId() != null &&
                !target.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only activate users within your own department");
            }
        }

        UserResponse activated = userManagementService.activateUser(userId, currentUser.getUserId());
        log.info("[USER_ACTIVATE] userId={} by {}", userId, currentUser.getEmail());
        return ok(activated, "User activated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  13. RESET PASSWORD (Admin)
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN')")
    @Operation(
        summary = "Reset user password",
        description = "Admin sets a new temporary password for a user."
    )
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // DEPARTMENTADMIN: only own department
        if (currentUser.getRole() == User.Role.DEPARTMENTADMIN) {
            UserResponse target = userManagementService.getUserById(userId);
            if (target.getDepartmentId() != null &&
                !target.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only reset passwords for users in your own department");
            }
        }

        userManagementService.resetPassword(userId, request.getNewPassword(), currentUser.getUserId());
        log.info("[PASSWORD_RESET] userId={} by {}", userId, currentUser.getEmail());
        return ok(null, "Password reset successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  14. SOFT DELETE USER  (SYSTEMADMIN only)
    // ═════════════════════════════════════════════════════════════
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(
        summary = "Delete a user (soft delete)",
        description = "Marks the user as deleted. Irreversible through API. SYSTEMADMIN only."
    )
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Prevent self-deletion
        if (userId.equals(currentUser.getUserId())) {
            return badRequest("You cannot delete your own account");
        }

        userManagementService.deleteUser(userId, currentUser.getUserId());
        log.warn("[USER_DELETE] userId={} soft-deleted by {}", userId, currentUser.getEmail());
        return ok(null, "User deleted successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  15. GET DEPARTMENT USER STATISTICS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
    @Operation(
        summary = "Get user statistics for a department",
        description = "Returns counts of staff, students, and role breakdown."
    )
    public ResponseEntity<Map<String, Object>> getDepartmentUserStats(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasAccessToDepartment(currentUser, departmentId)) {
            return forbidden("You do not have access to this department's statistics");
        }

        Map<String, Object> stats = userManagementService.getDepartmentStats(departmentId);
        return ok(stats, "Department statistics retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  16. GET ALL SYSTEM ADMINS  (SYSTEMADMIN only)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/system-admins")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(summary = "Get all system admins", description = "Returns all SYSTEMADMIN accounts.")
    public ResponseEntity<Map<String, Object>> getSystemAdmins() {
        List<UserResponse> admins = userManagementService.getUsersByRole("SYSTEMADMIN");
        return ok(Map.of("admins", admins, "count", admins.size()), "System admins retrieved");
    }
}
