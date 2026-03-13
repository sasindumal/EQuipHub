package com.equiphub.api.controller;

import com.equiphub.api.dto.inspection.*;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.InspectionService;
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
@RequestMapping("/inspections")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inspection & Equipment Issue/Return",
     description = "TO inspection, issue, and return endpoints per v3.8 specs")
@SecurityRequirement(name = "bearerAuth")
public class InspectionController {

    private final InspectionService inspectionService;

    // ─────────────────────────────────────────────────────────────
    //  RESPONSE HELPERS (same pattern as EquipmentController)
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

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, Object data,
                                                       String message, boolean success) {
        Map<String, Object> body = new HashMap<>();
        body.put("success",   success);
        body.put("message",   message);
        body.put("timestamp", LocalDateTime.now());
        if (data != null) body.put("data", data);
        return ResponseEntity.status(status).body(body);
    }

    private UUID callerDeptId(CustomUserDetails user) {
        if (user.getDepartmentId() == null)
            throw new RuntimeException("Your account has no department assigned");
        return UUID.fromString(user.getDepartmentId());
    }

    // ═════════════════════════════════════════════════════════════
    //  1. ISSUE EQUIPMENT (TO performs pre-inspection + handover)
    //     POST /api/v1/inspections/issue
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','SYSTEMADMIN')")
    @Operation(
        summary = "Issue equipment to student (pre-issuance inspection)",
        description = "TO inspects equipment condition, records it, then issues to student. " +
                      "Each item gets a PRE_ISSUANCE inspection record. " +
                      "Request status transitions: APPROVED → IN_USE."
    )
    public ResponseEntity<Map<String, Object>> issueEquipment(
            @Valid @RequestBody IssueEquipmentDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<InspectionResponseDTO> results =
                inspectionService.issueEquipment(dto, currentUser.getUserId());

        log.info("[ISSUE_EQUIPMENT] {} items issued for request {} by TO {}",
                results.size(), dto.getRequestId(), currentUser.getEmail());

        return created(
            Map.of("inspections", results, "count", results.size(),
                   "requestId", dto.getRequestId()),
            "Equipment issued successfully — " + results.size() + " items"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  2. PROCESS RETURN (TO receives + post-return inspection)
    //     POST /api/v1/inspections/return
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/return")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','SYSTEMADMIN')")
    @Operation(
        summary = "Process equipment return with post-return inspection",
        description = "TO inspects returned equipment, records condition after, damage level, " +
                      "and auto-triggers penalties if damage detected. " +
                      "Request status: IN_USE → RETURNED or PENALTY_ASSESSED."
    )
    public ResponseEntity<Map<String, Object>> processReturn(
            @Valid @RequestBody ReturnInspectionDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<InspectionResponseDTO> results =
                inspectionService.processReturn(dto, currentUser.getUserId());

        long damaged = results.stream().filter(r -> r.getDamageLevel() != null
                && r.getDamageLevel() > 0).count();

        String msg = damaged > 0
                ? results.size() + " items returned, " + damaged + " with damage detected"
                : results.size() + " items returned successfully — no damage";

        log.info("[RETURN_EQUIPMENT] {} for request {} by TO {}",
                msg, dto.getRequestId(), currentUser.getEmail());

        return ok(
            Map.of("inspections", results, "count", results.size(),
                   "damageDetected", damaged, "requestId", dto.getRequestId()),
            msg
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  3. STUDENT ACKNOWLEDGES INSPECTION
    //     POST /api/v1/inspections/{inspectionId}/acknowledge
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/{inspectionId}/acknowledge")
    @PreAuthorize("hasAnyRole('STUDENT','SYSTEMADMIN')")
    @Operation(
        summary = "Student acknowledges inspection result",
        description = "Student confirms they have seen and agree with the inspection findings."
    )
    public ResponseEntity<Map<String, Object>> acknowledgeInspection(
            @PathVariable Integer inspectionId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        InspectionResponseDTO result =
                inspectionService.acknowledgeInspection(inspectionId, currentUser.getUserId());

        return ok(result, "Inspection acknowledged by student");
    }

    // ═════════════════════════════════════════════════════════════
    //  4. GET INSPECTIONS FOR A REQUEST
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/request/{requestId}")
    @Operation(
        summary = "Get all inspections for a request",
        description = "Returns both pre-issuance and post-return inspections for all items."
    )
    public ResponseEntity<Map<String, Object>> getByRequest(
            @PathVariable String requestId) {

        List<InspectionResponseDTO> inspections =
                inspectionService.getInspectionsByRequest(requestId);

        return ok(
            Map.of("inspections", inspections, "count", inspections.size(),
                   "requestId", requestId),
            "Inspections retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  5. MY INSPECTIONS (TO dashboard)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my-inspections")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','SYSTEMADMIN')")
    @Operation(
        summary = "Get all inspections performed by current TO",
        description = "TO's inspection history, newest first."
    )
    public ResponseEntity<Map<String, Object>> getMyInspections(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<InspectionResponseDTO> inspections =
                inspectionService.getMyInspections(currentUser.getUserId());

        return ok(
            Map.of("inspections", inspections, "count", inspections.size()),
            "Your inspections retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  6. DEPARTMENT DAMAGE REPORT
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/damage-report")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Get damage report for department",
        description = "All inspections with damage detected. Default: last 30 days."
    )
    public ResponseEntity<Map<String, Object>> getDamageReport(
            @PathVariable UUID departmentId,
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            if (!departmentId.toString().equals(currentUser.getDepartmentId())) {
                return forbidden("Access restricted to your own department");
            }
        }

        List<InspectionResponseDTO> report =
                inspectionService.getDamageReport(departmentId, days);

        return ok(
            Map.of("damageReport", report, "count", report.size(),
                   "periodDays", days, "departmentId", departmentId),
            "Damage report generated — " + report.size() + " incidents"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  7. UNACKNOWLEDGED DAMAGE INSPECTIONS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/unacknowledged")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Get unacknowledged damage inspections",
        description = "Post-return inspections with damage where student hasn't acknowledged."
    )
    public ResponseEntity<Map<String, Object>> getUnacknowledged(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<InspectionResponseDTO> results =
                inspectionService.getUnacknowledgedDamage();

        return ok(
            Map.of("inspections", results, "count", results.size()),
            results.isEmpty()
                    ? "No unacknowledged damage inspections"
                    : results.size() + " inspections pending student acknowledgement"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  8. DEPARTMENT INSPECTION STATS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(
        summary = "Inspection statistics for department",
        description = "Counts by type, damage levels, average condition scores, penalty triggers."
    )
    public ResponseEntity<Map<String, Object>> getDepartmentStats(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (currentUser.getRole() != User.Role.SYSTEMADMIN) {
            if (!departmentId.toString().equals(currentUser.getDepartmentId())) {
                return forbidden("Access restricted to your own department");
            }
        }

        InspectionSummaryDTO stats =
                inspectionService.getDepartmentInspectionStats(departmentId);

        return ok(stats, "Department inspection stats retrieved");
    }

    // ═════════════════════════════════════════════════════════════
    //  9. MY DEPARTMENT SHORTCUTS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my-department/damage-report")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT')")
    @Operation(summary = "Shortcut: damage report for my department")
    public ResponseEntity<Map<String, Object>> getMyDeptDamageReport(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        List<InspectionResponseDTO> report =
                inspectionService.getDamageReport(deptId, days);

        return ok(
            Map.of("damageReport", report, "count", report.size(),
                   "periodDays", days),
            "Your department's damage report"
        );
    }

    @GetMapping("/my-department/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT')")
    @Operation(summary = "Shortcut: inspection stats for my department")
    public ResponseEntity<Map<String, Object>> getMyDeptStats(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        InspectionSummaryDTO stats =
                inspectionService.getDepartmentInspectionStats(deptId);

        return ok(stats, "Your department's inspection stats");
    }
}
