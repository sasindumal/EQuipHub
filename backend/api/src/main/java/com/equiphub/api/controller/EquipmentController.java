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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/equipment")  // context-path /api/v1 is set in application.properties
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Management", description = "TO endpoints for full equipment lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentController {

    private final EquipmentService equipmentService;

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

    // ═══════════════════════════════════════════════════════════
    //  GET /api/v1/equipment
    //  Bug fix: SYSTEMADMIN has no departmentId — callerDeptId() would throw.
    //  SYSTEMADMIN now retrieves all equipment across all departments.
    // ═══════════════════════════════════════════════════════════
    @GetMapping
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','SYSTEMADMIN')")
    @Operation(
        summary = "Get equipment for my department (JWT-resolved)",
        description = "Returns all active (non-retired) equipment for the authenticated user's department. "
                    + "SYSTEMADMIN sees all equipment across all departments. "
                    + "Supports pagination via ?page=0&size=20 and ?activeOnly=true/false."
    )
    public ResponseEntity<Map<String, Object>> getEquipment(
            @RequestParam(defaultValue = "true")  boolean activeOnly,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "20")    int size,
            @RequestParam(defaultValue = "name")  String sortBy,
            @RequestParam(defaultValue = "ASC")   String direction,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<EquipmentResponse> equipment;

        if (currentUser.getRole() == User.Role.SYSTEMADMIN) {
            // SYSTEMADMIN has no departmentId — return all equipment
            equipment = equipmentService.getAll(activeOnly);
        } else {
            UUID deptId = callerDeptId(currentUser);
            equipment = equipmentService.getByDepartment(deptId, activeOnly);
            log.debug("[EQUIP_LIST] {} fetched {} equipment items for dept {}",
                    currentUser.getEmail(), equipment.size(), deptId);
        }

        return ok(
            Map.of(
                "equipment",  equipment,
                "count",      equipment.size(),
                "activeOnly", activeOnly
            ),
            "Equipment retrieved successfully"
        );
    }

    // ═══════════════════════════════════════════════════════════
    //  POST /api/v1/equipment
    // ═══════════════════════════════════════════════════════════
    @PostMapping
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Register new equipment", description = "TO and DEPTADMIN can register equipment only in their own department. SYSTEMADMIN can register for any department.")
    public ResponseEntity<Map<String, Object>> createEquipment(
            @Valid @RequestBody CreateEquipmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
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

    @GetMapping("/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get equipment by ID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID equipmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        EquipmentResponse eq = equipmentService.getById(equipmentId);
        if (!hasEquipmentAccess(currentUser, eq.getDepartmentId())) {
            return forbidden("You can only view equipment in your own department");
        }
        return ok(eq, "Equipment retrieved successfully");
    }

    @GetMapping("/{equipmentId}/availability")
    @Operation(summary = "Check equipment availability", description = "Returns availability status, condition score, and maintenance status.")
    public ResponseEntity<Map<String, Object>> checkAvailability(@PathVariable UUID equipmentId) {
        Map<String, Object> result = equipmentService.checkAvailability(equipmentId);
        return ok(result, "Availability checked successfully");
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get all equipment in a department", description = "Use ?activeOnly=true to exclude retired equipment (default true).")
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

    @GetMapping("/department/{departmentId}/available")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get available equipment in a department", description = "Returns all equipment with status AVAILABLE. Used for request creation.")
    public ResponseEntity<Map<String, Object>> getAvailable(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("You can only view equipment in your own department");
        }
        List<EquipmentResponse> equipment = equipmentService.getAvailableByDepartment(departmentId);
        return ok(Map.of("equipment", equipment, "count", equipment.size()), "Available equipment retrieved");
    }

    @GetMapping("/department/{departmentId}/status/{status}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get equipment by status in a department", description = "Valid statuses: AVAILABLE, RESERVED, INUSE, MAINTENANCE, DAMAGED, ARCHIVED")
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
        return ok(Map.of("equipment", equipment, "count", equipment.size()), "Equipment filtered by type");
    }

    @GetMapping("/department/{departmentId}/maintenance-due")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get equipment with overdue maintenance", description = "Returns all active equipment whose nextMaintenanceDate <= today.")
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

    @GetMapping("/department/{departmentId}/low-condition")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get equipment below condition threshold", description = "Default threshold = 20 (POOR). Pass ?threshold=40 to include FAIR and below.")
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

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Update equipment details", description = "Update metadata: name, location, specs, maintenance dates, condition notes, etc.")
    public ResponseEntity<Map<String, Object>> updateEquipment(
            @PathVariable UUID equipmentId,
            @Valid @RequestBody UpdateEquipmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            EquipmentResponse existing = equipmentService.getById(equipmentId);
            if (!existing.getDepartmentId().toString().equals(currentUser.getDepartmentId())) {
                return forbidden("You can only update equipment in your own department");
            }
        }
        EquipmentResponse updated = equipmentService.updateEquipment(equipmentId, request, currentUser.getUserId());
        return ok(updated, "Equipment updated successfully");
    }

    @PatchMapping("/{equipmentId}/status")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Update equipment operational status", description = "TO changes status: AVAILABLE \u2192 MAINTENANCE, MAINTENANCE \u2192 AVAILABLE, mark DAMAGED, etc. Reason is MANDATORY for MAINTENANCE and DAMAGED transitions.")
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
        EquipmentResponse updated = equipmentService.updateStatus(equipmentId, request, currentUser.getUserId());
        log.info("[EQUIP_STATUS_CHANGE] {} \u2192 {} by {}", equipmentId, request.getStatus(), currentUser.getEmail());
        return ok(updated, "Equipment status updated to " + request.getStatus());
    }

    @DeleteMapping("/{equipmentId}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Retire equipment (soft delete)", description = "Marks equipment as retired and ARCHIVED. Cannot retire IN_USE or RESERVED equipment.")
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
        EquipmentResponse retired = equipmentService.retireEquipment(equipmentId, reason, currentUser.getUserId());
        log.warn("[EQUIP_RETIRE] {} retired by {}", equipmentId, currentUser.getEmail());
        return ok(retired, "Equipment retired successfully");
    }

    @GetMapping("/department/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Equipment statistics for a department", description = "Returns counts by status, type, maintenance-due, and low-condition alerts.")
    public ResponseEntity<Map<String, Object>> getDepartmentStats(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (!hasEquipmentAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }
        Map<String, Object> stats = equipmentService.getDepartmentStats(departmentId);
        return ok(stats, "Department equipment statistics retrieved");
    }

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
        return ok(Map.of("equipment", retired, "count", retired.size()), "Retired equipment retrieved");
    }

    @GetMapping("/my-department")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN')")
    @Operation(summary = "TO shortcut: all active equipment in my department", description = "Returns all non-retired equipment for the TO's own department.")
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
