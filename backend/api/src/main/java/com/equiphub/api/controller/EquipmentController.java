package com.equiphub.api.controller;

import com.equiphub.api.dto.equipment.*;
import com.equiphub.api.model.Equipment;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Management", description = "TO endpoints for full equipment lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ─────────────────────────────────────────────────────────────
    //  RESPONSE HELPERS
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> ok(Object data, String message) {
        return build(HttpStatus.OK, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> created(Object data, String message) {
        return build(HttpStatus.CREATED, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        return build(HttpStatus.FORBIDDEN, null, message, false);
    }

    private ResponseEntity<Map<String, Object>> bad(String message) {
        return build(HttpStatus.BAD_REQUEST, null, message, false);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, Object data,
                                                       String message, boolean success) {
        Map<String, Object> body = new HashMap<>();
        body.put("success",   success);
        body.put("message",   message);
        body.put("timestamp", LocalDateTime.now());
        if (data != null) body.put("data", data);
        return ResponseEntity.status(status).body(body);
    }

    // ── Department guard: TO and DEPTADMIN only for own dept ─────
    private boolean hasEquipmentAccess(CustomUserDetails user, UUID targetDeptId) {
        if (user.getRole() == User.Role.SYSTEMADMIN) return true;
        if (user.getDepartmentId() == null) return false;
        return targetDeptId.toString().equals(user.getDepartmentId());
    }

    private UUID callerDeptId(CustomUserDetails user) {
        if (user.getDepartmentId() == null)
            throw new RuntimeException("Your account has no department assigned");
        return UUID.fromString(user.getDepartmentId());
    }

    // ═════════════════════════════════════════════════════════════
    //  1. CREATE EQUIPMENT
    //     TO (own dept) | DEPTADMIN (own dept) | SYSTEMADMIN (any)
    // ═════════════════════════════════════════════════════════════
    @PostMapping
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Register new equipment",
        description = "TO and DEPTADMIN can register equipment only in their own department. " +
                      "SYSTEMADMIN can register for any department."
    )
    public ResponseEntity<Map<String, Object>> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Scope check for non-SYSTEMADMIN
        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            String callerDept = currentUser.getDepartmentId();
            if (!request.getDepartmentId().equals(callerDept)) {
                return forbidden("You can only register equipment in your own department");
            }
        }

        EquipmentResponse created = equipmentService.createEquipment(request, currentUser.getUserId());
        log.info("[EQUIP_REGISTER] {} by {}", created.getEquipmentId(), currentUser.getEmail());
        return created(created, "Equipment registered successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  2. GET EQUIPMENT BY ID  (any authenticated user)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/{equipmentId}")
    @Operation(summary = "Get equipment by ID")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID equipmentId) {
        return ok(equipmentService.getById(equipmentId), "Equipment retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  3. CHECK AVAILABILITY  (any authenticated user)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/{equipmentId}/availability")
    @Operation(
        summary = "Check equipment availability",
        description = "Returns availability status, condition score, and maintenance status."
    )
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable UUID equipmentId) {
        Map<String, Object> result = equipmentService.checkAvailability(equipmentId);
        return ok(result, "Availability checked successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  4. GET EQUIPMENT BY DEPARTMENT
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}")
    @Operation(
        summary = "Get all equipment in a department",
        description = "Use ?activeOnly=true to exclude retired equipment (default true)."
    )
    public ResponseEntity<Map<String, Object>> getByDepartment(
            @PathVariable UUID departmentId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("You can only view equipment in your own department");
        }

        List<EquipmentResponse> equipment = equipmentService.getByDepartment(departmentId, activeOnly);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size(), "departmentId", departmentId),
            "Equipment retrieved successfully"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  5. GET AVAILABLE EQUIPMENT BY DEPARTMENT  (all authenticated)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/available")
    @Operation(
        summary = "Get available equipment in a department",
        description = "Returns all equipment with status AVAILABLE. Used for request creation."
    )
    public ResponseEntity<Map<String, Object>> getAvailable(@PathVariable UUID departmentId) {
        List<EquipmentResponse> equipment = equipmentService.getAvailableByDepartment(departmentId);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size()),
            "Available equipment retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  6. GET EQUIPMENT BY STATUS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/status/{status}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Get equipment by status in a department",
        description = "Valid statuses: AVAILABLE, RESERVED, INUSE, MAINTENANCE, DAMAGED, ARCHIVED"
    )
    public ResponseEntity<Map<String, Object>> getByStatus(
            @PathVariable UUID departmentId,
            @PathVariable String status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("You can only view equipment in your own department");
        }

        Equipment.EquipmentStatus equipStatus;
        try {
            equipStatus = Equipment.EquipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return bad("Invalid status '" + status + "'. Valid: " +
                       java.util.Arrays.toString(Equipment.EquipmentStatus.values()));
        }

        List<EquipmentResponse> equipment = equipmentService.getByDepartmentAndStatus(departmentId, equipStatus);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size(), "status", status.toUpperCase()),
            "Equipment filtered by status"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  7. GET EQUIPMENT BY TYPE (LABDEDICATED / BORROWABLE)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/type/{type}")
    @Operation(summary = "Get equipment by type", description = "LABDEDICATED or BORROWABLE")
    public ResponseEntity<Map<String, Object>> getByType(
            @PathVariable UUID departmentId,
            @PathVariable String type) {

        Equipment.EquipmentType equipType;
        try {
            equipType = Equipment.EquipmentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return bad("Invalid type. Use LABDEDICATED or BORROWABLE");
        }

        List<EquipmentResponse> equipment = equipmentService.getByDepartmentAndType(departmentId, equipType);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size()),
            "Equipment filtered by type"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  8. MAINTENANCE DUE LIST  (TO, DEPTADMIN)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/maintenance-due")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Get equipment with overdue maintenance",
        description = "Returns all active equipment whose nextMaintenanceDate <= today."
    )
    public ResponseEntity<Map<String, Object>> getMaintenanceDue(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        List<EquipmentResponse> equipment = equipmentService.getMaintenanceDue(departmentId);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size(),
                   "message", equipment.isEmpty() ? "No maintenance overdue" : equipment.size() + " items require maintenance"),
            "Maintenance due list retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  9. LOW CONDITION ALERTS  (TO, DEPTADMIN)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/low-condition")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Get equipment below condition threshold",
        description = "Default threshold = 20 (POOR). Pass ?threshold=40 to include FAIR and below."
    )
    public ResponseEntity<Map<String, Object>> getLowCondition(
            @PathVariable UUID departmentId,
            @RequestParam(defaultValue = "20") int threshold,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }
        if (threshold < 0 || threshold > 100) {
            return bad("Threshold must be between 0 and 100");
        }

        List<EquipmentResponse> equipment = equipmentService.getLowCondition(departmentId, threshold);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size(), "threshold", threshold),
            "Low condition equipment retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  10. SEARCH EQUIPMENT
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/search")
    @Operation(summary = "Search equipment by name or serial number", description = "Minimum 2 characters.")
    public ResponseEntity<Map<String, Object>> search(
            @PathVariable UUID departmentId,
            @RequestParam String keyword,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }
        List<EquipmentResponse> results = equipmentService.search(departmentId, keyword);
        return ok(
            Map.of("results", results, "count", results.size(), "keyword", keyword),
            "Search completed"
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(summary = "Global equipment search (SYSTEMADMIN only)")
    public ResponseEntity<Map<String, Object>> searchGlobal(@RequestParam String keyword) {
        List<EquipmentResponse> results = equipmentService.searchGlobal(keyword);
        return ok(
            Map.of("results", results, "count", results.size(), "keyword", keyword),
            "Global search completed"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  11. UPDATE EQUIPMENT METADATA  (TO, DEPTADMIN)
    // ═════════════════════════════════════════════════════════════
    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Update equipment details",
        description = "Update metadata: name, location, specs, maintenance dates, condition notes, etc."
    )
    public ResponseEntity<Map<String, Object>> updateEquipment(
            @PathVariable UUID equipmentId,
            @Valid @RequestBody UpdateEquipmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Validate department scope
        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            EquipmentResponse existing = equipmentService.getById(equipmentId);
            if (!existing.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only update equipment in your own department");
            }
        }

        EquipmentResponse updated = equipmentService.updateEquipment(
                equipmentId, request, currentUser.getUserId()
        );
        return ok(updated, "Equipment updated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  12. UPDATE EQUIPMENT STATUS  (TO primary responsibility)
    // ═════════════════════════════════════════════════════════════
    @PatchMapping("/{equipmentId}/status")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Update equipment operational status",
        description = "TO changes status: AVAILABLE → MAINTENANCE, MAINTENANCE → AVAILABLE, mark DAMAGED, etc. " +
                      "Reason is MANDATORY for MAINTENANCE and DAMAGED transitions."
    )
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable UUID equipmentId,
            @Valid @RequestBody EquipmentStatusUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            EquipmentResponse existing = equipmentService.getById(equipmentId);
            if (!existing.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only update status of equipment in your own department");
            }
        }

        EquipmentResponse updated = equipmentService.updateStatus(
                equipmentId, request, currentUser.getUserId()
        );
        log.info("[EQUIP_STATUS_CHANGE] {} → {} by {}",
                equipmentId, request.getStatus(), currentUser.getEmail());
        return ok(updated, "Equipment status updated to " + request.getStatus());
    }

    // ═════════════════════════════════════════════════════════════
    //  13. RETIRE EQUIPMENT  (TO + DEPTADMIN)
    // ═════════════════════════════════════════════════════════════
    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Retire equipment (soft delete)",
        description = "Marks equipment as retired and ARCHIVED. Cannot retire IN_USE or RESERVED equipment."
    )
    public ResponseEntity<Map<String, Object>> retireEquipment(
            @PathVariable UUID equipmentId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            EquipmentResponse existing = equipmentService.getById(equipmentId);
            if (!existing.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only retire equipment in your own department");
            }
        }

        EquipmentResponse retired = equipmentService.retireEquipment(
                equipmentId, reason, currentUser.getUserId()
        );
        log.warn("[EQUIP_RETIRE] {} retired by {}", equipmentId, currentUser.getEmail());
        return ok(retired, "Equipment retired successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  14. DEPARTMENT EQUIPMENT DASHBOARD STATS  (TO, DEPTADMIN, HOD)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Equipment statistics for a department",
        description = "Returns counts by status, type, maintenance-due, and low-condition alerts."
    )
    public ResponseEntity<Map<String, Object>> getDepartmentStats(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        Map<String, Object> stats = equipmentService.getDepartmentStats(departmentId);
        return ok(stats, "Department equipment statistics retrieved");
    }

    // ═════════════════════════════════════════════════════════════
    //  15. GET RETIRED EQUIPMENT  (audit trail)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/retired")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get retired equipment in a department")
    public ResponseEntity<Map<String, Object>> getRetired(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        List<EquipmentResponse> retired = equipmentService.getRetiredByDepartment(departmentId);
        return ok(
            Map.of("equipment", retired, "count", retired.size()),
            "Retired equipment retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  16. MY DEPARTMENT QUICK OVERVIEW  (TO shortcut)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my-department")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN')")
    @Operation(
        summary = "TO shortcut: all active equipment in my department",
        description = "Returns all non-retired equipment for the TO's own department."
    )
    public ResponseEntity<Map<String, Object>> getMyDepartmentEquipment(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        List<EquipmentResponse> equipment = equipmentService.getByDepartment(deptId, true);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size(), "departmentId", deptId),
            "Your department's equipment retrieved"
        );
    }

    @GetMapping("/my-department/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT')")
    @Operation(summary = "TO shortcut: equipment stats for my department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStats(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        return ok(equipmentService.getDepartmentStats(deptId), "Your department stats retrieved");
    }

    @GetMapping("/my-department/maintenance-due")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT')")
    @Operation(summary = "TO shortcut: maintenance-due equipment in my department")
    public ResponseEntity<Map<String, Object>> getMyMaintenanceDue(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        List<EquipmentResponse> equipment = equipmentService.getMaintenanceDue(deptId);
        return ok(
            Map.of("equipment", equipment, "count", equipment.size()),
            equipment.isEmpty() ? "No maintenance overdue" : equipment.size() + " items require maintenance"
        );
    }
}
