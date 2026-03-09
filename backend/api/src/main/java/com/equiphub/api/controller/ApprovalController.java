package com.equiphub.api.controller;

import com.equiphub.api.dto.approval.ApprovalDecisionDTO;
import com.equiphub.api.dto.approval.ApprovalQueueItemDTO;
import com.equiphub.api.dto.approval.ApprovalResponseDTO;
import com.equiphub.api.dto.approval.ApprovalStatsDTO;
import com.equiphub.api.dto.approval.AutoApprovalResultDTO;
import com.equiphub.api.model.RequestApproval;
import com.equiphub.api.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Approval Workflow", description = "Equipment request approval chain management")
public class ApprovalController {

    private final ApprovalService approvalService;

    // ═══════════════════════════════════════════════════════════
    //  1. AUTO-APPROVAL — attempt auto-approval for a request
    //     POST /api/v1/approvals/requests/{requestId}/auto-approve
    //     Access: TECHNICALOFFICER, DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/requests/{requestId}/auto-approve")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Attempt auto-approval",
        description = "Runs 6-condition auto-approval engine on a COURSEWORK request. " +
                      "Returns result with per-condition breakdown."
    )
    public ResponseEntity<AutoApprovalResultDTO> attemptAutoApproval(
            @Parameter(description = "Request ID e.g. REQ-2026-00001")
            @PathVariable String requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[AUTO_APPROVE] Attempting auto-approval for request {} by {}",
                requestId, userDetails.getUsername());

        AutoApprovalResultDTO result = approvalService.attemptAutoApproval(requestId);
        return ResponseEntity.ok(result);
    }

    // ═══════════════════════════════════════════════════════════
    //  2. PROCESS DECISION — approve / reject / recommend
    //     POST /api/v1/approvals/requests/{requestId}/decide
    //     Access: LECTURER, APPOINTEDLECTURER, INSTRUCTOR,
    //             HEADOFDEPARTMENT, TECHNICALOFFICER,
    //             DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @PostMapping("/requests/{requestId}/decide")
    @PreAuthorize("hasAnyRole('LECTURER','APPOINTEDLECTURER','INSTRUCTOR'," +
                             "'HEADOFDEPARTMENT','TECHNICALOFFICER'," +
                             "'DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Process approval decision",
        description = "Submit an APPROVE / REJECT / RECOMMEND / MODIFY decision " +
                      "at the current approval stage. Actor role is validated against stage."
    )
    public ResponseEntity<ApprovalResponseDTO> processDecision(
            @Parameter(description = "Request ID e.g. REQ-2026-00001")
            @PathVariable String requestId,

            @Parameter(description = "Approval stage being acted on",
                       example = "LECTURERAPPROVAL")
            @RequestParam RequestApproval.ApprovalStage stage,

            @Valid @RequestBody ApprovalDecisionDTO dto,

            @AuthenticationPrincipal UserDetails userDetails) {

        UUID actorId = extractUserId(userDetails);
        log.info("[DECISION] {} processing {} on request {} at stage {}",
                userDetails.getUsername(), dto.getAction(), requestId, stage);

        ApprovalResponseDTO response =
                approvalService.processDecision(requestId, stage, dto, actorId);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  3. MY APPROVAL QUEUE — pending items for current user
    //     GET /api/v1/approvals/my-queue
    //     Access: LECTURER, APPOINTEDLECTURER, INSTRUCTOR,
    //             HEADOFDEPARTMENT, TECHNICALOFFICER,
    //             DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/my-queue")
    @PreAuthorize("hasAnyRole('LECTURER','APPOINTEDLECTURER','INSTRUCTOR'," +
                             "'HEADOFDEPARTMENT','TECHNICALOFFICER'," +
                             "'DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Get my approval queue",
        description = "Returns all requests pending the current user's action."
    )
    public ResponseEntity<List<ApprovalQueueItemDTO>> getMyApprovalQueue(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID actorId = extractUserId(userDetails);
        log.info("[QUEUE] Fetching approval queue for {}", userDetails.getUsername());

        List<ApprovalQueueItemDTO> queue = approvalService.getMyApprovalQueue(actorId);
        return ResponseEntity.ok(queue);
    }

    // ═══════════════════════════════════════════════════════════
    //  4. DEPARTMENT APPROVAL QUEUE — by department ID
    //     GET /api/v1/approvals/departments/{departmentId}/queue
    //     Access: HEADOFDEPARTMENT, TECHNICALOFFICER,
    //             DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/departments/{departmentId}/queue")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','TECHNICALOFFICER'," +
                             "'DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Get department approval queue",
        description = "Returns all pending approval items for the specified department."
    )
    public ResponseEntity<List<ApprovalQueueItemDTO>> getDepartmentApprovalQueue(
            @Parameter(description = "Department UUID")
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[QUEUE] Fetching dept queue for department {} by {}",
                departmentId, userDetails.getUsername());

        List<ApprovalQueueItemDTO> queue =
                approvalService.getDepartmentApprovalQueue(departmentId);
        return ResponseEntity.ok(queue);
    }

    // ═══════════════════════════════════════════════════════════
    //  5. APPROVAL HISTORY — full audit trail for a request
    //     GET /api/v1/approvals/requests/{requestId}/history
    //     Access: All authenticated (scoped by ownership in service)
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/requests/{requestId}/history")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','APPOINTEDLECTURER','INSTRUCTOR'," +
                             "'HEADOFDEPARTMENT','TECHNICALOFFICER'," +
                             "'DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Get approval history for a request",
        description = "Returns the full ordered audit trail of all approval " +
                      "actions taken on the specified request."
    )
    public ResponseEntity<List<ApprovalResponseDTO>> getApprovalHistory(
            @Parameter(description = "Request ID e.g. REQ-2026-00001")
            @PathVariable String requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[HISTORY] Fetching approval history for request {} by {}",
                requestId, userDetails.getUsername());

        List<ApprovalResponseDTO> history = approvalService.getApprovalHistory(requestId);
        return ResponseEntity.ok(history);
    }

    // ═══════════════════════════════════════════════════════════
    //  6. DEPARTMENT APPROVAL STATS
    //     GET /api/v1/approvals/departments/{departmentId}/stats
    //     Access: HEADOFDEPARTMENT, DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/departments/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('HEADOFDEPARTMENT','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Get department approval statistics",
        description = "Returns counts of pending / approved / rejected approvals, " +
                      "SLA breaches, emergency items, and breakdown by stage."
    )
    public ResponseEntity<ApprovalStatsDTO> getDepartmentApprovalStats(
            @Parameter(description = "Department UUID")
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[STATS] Fetching approval stats for department {} by {}",
                departmentId, userDetails.getUsername());

        ApprovalStatsDTO stats = approvalService.getDepartmentApprovalStats(departmentId);
        return ResponseEntity.ok(stats);
    }

    // ═══════════════════════════════════════════════════════════
    //  7. DETERMINE NEXT STAGE (utility — for frontend routing)
    //     GET /api/v1/approvals/requests/{requestId}/next-stage
    //     Access: TECHNICALOFFICER, DEPARTMENTADMIN, SYSTEMADMIN
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/requests/{requestId}/next-stage")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN'," +
                             "'HEADOFDEPARTMENT','LECTURER','APPOINTEDLECTURER')")
    @Operation(
        summary = "Get next approval stage",
        description = "Returns the next required approval stage for a request. " +
                      "Useful for frontend to route the approver to the correct action."
    )
    public ResponseEntity<NextStageResponseDTO> getNextApprovalStage(
            @Parameter(description = "Request ID e.g. REQ-2026-00001")
            @PathVariable String requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[NEXT_STAGE] Querying next stage for request {} by {}",
                requestId, userDetails.getUsername());

        com.equiphub.api.model.Request request =
                approvalService.findRequestPublic(requestId);
        RequestApproval.ApprovalStage nextStage =
                approvalService.determineNextStage(request);

        return ResponseEntity.ok(new NextStageResponseDTO(requestId, nextStage));
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILITY: extract UUID from Spring Security principal
    // ═══════════════════════════════════════════════════════════
    private UUID extractUserId(UserDetails userDetails) {
        // Your existing UserDetails implementation stores UUID as the username
        // (consistent with AuthController / UserService pattern in this project)
        return UUID.fromString(userDetails.getUsername());
    }

    // ═══════════════════════════════════════════════════════════
    //  INNER RESPONSE DTO for next-stage endpoint
    //  (lightweight — no separate file needed)
    // ═══════════════════════════════════════════════════════════
    public record NextStageResponseDTO(
            String requestId,
            RequestApproval.ApprovalStage nextStage
    ) {}
}
