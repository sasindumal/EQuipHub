package com.equiphub.api.controller;

import com.equiphub.api.dto.penalty.*;
import com.equiphub.api.service.PenaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/penalties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Penalty Management", description = "Penalty points, appeals & borrowing restrictions")
public class PenaltyController {

    private final PenaltyService penaltyService;

    // ── 1. Create penalty ────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Create a penalty", description = "Issue penalty points for late return, damage, or lab override")
    public ResponseEntity<PenaltyResponseDTO> createPenalty(
            @Valid @RequestBody CreatePenaltyDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID issuedById = UUID.fromString(userDetails.getUsername());
        log.info("[PENALTY] Creating {} penalty for student {} by {}",
                dto.getPenaltyType(), dto.getStudentId(), userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(penaltyService.createPenalty(dto, issuedById));
    }

    // ── 2. Approve penalty ───────────────────────────────────
    @PostMapping("/{penaltyId}/approve")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Approve a pending penalty")
    public ResponseEntity<PenaltyResponseDTO> approvePenalty(
            @PathVariable Integer penaltyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID approvedById = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(penaltyService.approvePenalty(penaltyId, approvedById));
    }

    // ── 3. Waive penalty ─────────────────────────────────────
    @PostMapping("/{penaltyId}/waive")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Waive a penalty entirely")
    public ResponseEntity<PenaltyResponseDTO> waivePenalty(
            @PathVariable Integer penaltyId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID waivedById = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(penaltyService.waivePenalty(penaltyId, waivedById, reason));
    }

    // ── 4. Get student penalties ─────────────────────────────
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get all penalties for a student")
    public ResponseEntity<List<PenaltyResponseDTO>> getStudentPenalties(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(penaltyService.getStudentPenalties(studentId));
    }

    // ── 5. Get student summary ───────────────────────────────
    @GetMapping("/students/{studentId}/summary")
    @PreAuthorize("hasAnyRole('STUDENT','TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get penalty summary with level and borrowing status")
    public ResponseEntity<StudentPenaltySummaryDTO> getStudentSummary(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(penaltyService.getStudentSummary(studentId));
    }

    // ── 6. Check borrowing eligibility ───────────────────────
    @GetMapping("/students/{studentId}/can-borrow")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Check if student can borrow equipment")
    public ResponseEntity<Boolean> canStudentBorrow(@PathVariable UUID studentId) {
        return ResponseEntity.ok(penaltyService.canStudentBorrow(studentId));
    }

    // ── 7. Get department penalties ──────────────────────────
    @GetMapping("/departments/{departmentId}")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Get all penalties for a department")
    public ResponseEntity<List<PenaltyResponseDTO>> getDepartmentPenalties(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(penaltyService.getDepartmentPenalties(departmentId));
    }

    // ── 8. Get department pending penalties ───────────────────
    @GetMapping("/departments/{departmentId}/pending")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Get pending penalties for a department")
    public ResponseEntity<List<PenaltyResponseDTO>> getDepartmentPendingPenalties(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(penaltyService.getDepartmentPendingPenalties(departmentId));
    }

    // ── 9. Submit appeal (student) ───────────────────────────
    @PostMapping("/appeals")
    @PreAuthorize("hasAnyRole('STUDENT','SYSTEMADMIN')")
    @Operation(summary = "Submit a penalty appeal", description = "Students can appeal approved penalties within 7 days")
    public ResponseEntity<PenaltyResponseDTO> submitAppeal(
            @Valid @RequestBody AppealRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(penaltyService.submitAppeal(dto, studentId));
    }

    // ── 10. Decide appeal (HOD / DeptAdmin) ──────────────────
    @PostMapping("/appeals/{penaltyId}/decide")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Decide on a penalty appeal")
    public ResponseEntity<PenaltyResponseDTO> decideAppeal(
            @PathVariable Integer penaltyId,
            @Valid @RequestBody AppealDecisionDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID decidedById = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(penaltyService.decideAppeal(penaltyId, dto, decidedById));
    }

    // ── 11. My penalties (student self) ──────────────────────
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT','SYSTEMADMIN')")
    @Operation(summary = "Get my own penalties")
    public ResponseEntity<List<PenaltyResponseDTO>> getMyPenalties(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(penaltyService.getStudentPenalties(studentId));
    }

    // ── 12. My penalty summary ───────────────────────────────
    @GetMapping("/my/summary")
    @PreAuthorize("hasAnyRole('STUDENT','SYSTEMADMIN')")
    @Operation(summary = "Get my penalty summary")
    public ResponseEntity<StudentPenaltySummaryDTO> getMySummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID studentId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(penaltyService.getStudentSummary(studentId));
    }
}
