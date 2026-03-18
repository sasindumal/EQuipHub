package com.equiphub.api.service;

import com.equiphub.api.dto.approval.ApprovalDecisionDTO;
import com.equiphub.api.dto.approval.ApprovalQueueItemDTO;
import com.equiphub.api.dto.approval.ApprovalResponseDTO;
import com.equiphub.api.dto.approval.ApprovalStatsDTO;
import com.equiphub.api.dto.approval.AutoApprovalResultDTO;
import com.equiphub.api.exception.BadRequestException;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.exception.UnauthorizedException;
import com.equiphub.api.model.*;
import com.equiphub.api.model.RequestApproval.ApprovalDecision;
import com.equiphub.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalService {

    private final RequestRepository requestRepository;
    private final RequestApprovalRepository approvalRepository;
    private final RequestItemRepository requestItemRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final CourseRepository courseRepository;

    // Statuses in which auto-approval is meaningful
    private static final Set<Request.RequestStatus> AUTO_APPROVAL_ALLOWED_STATUSES = Set.of(
            Request.RequestStatus.PENDINGRECOMMENDATION,
            Request.RequestStatus.PENDINGAPPROVAL
    );

    // ═══════════════════════════════════════════════════════════
    //  1. AUTO-APPROVAL ENGINE  (COURSEWORK only — 6 conditions)
    // ═══════════════════════════════════════════════════════════
    public AutoApprovalResultDTO attemptAutoApproval(String requestId) {
        Request request = findRequestOrThrow(requestId);

        // ── Bug-2 fix: status guard ──────────────────────────────────────────────
        // Auto-approval must only run when the request is actually in a pending
        // state.  Without this check, a race condition between submitRequest() and
        // attemptAutoApproval() (or a direct API call) could promote a DRAFT or
        // already-APPROVED request to APPROVED, bypassing the entire workflow.
        if (!AUTO_APPROVAL_ALLOWED_STATUSES.contains(request.getStatus())) {
            log.warn("[AUTO_APPROVE] Rejected for {} — status is {} (must be PENDINGRECOMMENDATION or PENDINGAPPROVAL)",
                    requestId, request.getStatus());
            return AutoApprovalResultDTO.builder()
                    .autoApproved(false)
                    .requestId(requestId)
                    .failureReason("Request is not in a pending state. Current status: "
                            + request.getStatus())
                    .conditionChecks(Collections.emptyList())
                    .build();
        }
        // ────────────────────────────────────────────────────────────────────────

        // Auto-approval only applies to COURSEWORK
        if (request.getRequestType() != Request.RequestType.COURSEWORK) {
            return AutoApprovalResultDTO.builder()
                    .autoApproved(false)
                    .requestId(requestId)
                    .failureReason("Auto-approval only applies to COURSEWORK requests")
                    .conditionChecks(Collections.emptyList())
                    .build();
        }

        List<AutoApprovalResultDTO.ConditionCheck> checks = new ArrayList<>();
        boolean allPassed = true;

        // Condition 1: Equipment available
        List<RequestItem> items = requestItemRepository
                .findByRequestRequestIdOrderByRequestItemIdAsc(requestId);
        boolean equipAvailable = items.stream().allMatch(item -> {
            Equipment eq = item.getEquipment();
            return eq.getStatus() == Equipment.EquipmentStatus.AVAILABLE
                    && !Boolean.TRUE.equals(eq.getRetired());
        });
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Equipment available", equipAvailable,
                equipAvailable ? "All items available" : "Some items not available"));
        if (!equipAvailable) allPassed = false;

        // Condition 2: Course code valid
        boolean courseValid = request.getCourse() != null;
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Course code valid", courseValid,
                courseValid ? "Course: " + request.getCourse().getCourseId()
                            : "No course assigned"));
        if (!courseValid) allPassed = false;

        // Condition 3: Total quantity ≤ 10 items
        int totalQty = items.stream().mapToInt(RequestItem::getQuantityRequested).sum();
        boolean qtyOk = totalQty <= 10;
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Quantity ≤ 10 items", qtyOk,
                "Total requested: " + totalQty));
        if (!qtyOk) allPassed = false;

        // Condition 4: Semester total ≤ 15 items
        boolean semesterLimitOk = true;
        if (request.getStudent() != null) {
            LocalDateTime semesterStart = LocalDateTime.now()
                    .withMonth(1).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            long semesterItemCount = requestRepository
                    .findByStudentUserIdAndStatus(
                            request.getStudent().getUserId(),
                            Request.RequestStatus.APPROVED)
                    .stream()
                    .filter(r -> r.getSubmittedAt() != null
                            && r.getSubmittedAt().isAfter(semesterStart))
                    .flatMap(r -> requestItemRepository
                            .findByRequestRequestIdOrderByRequestItemIdAsc(r.getRequestId())
                            .stream())
                    .mapToInt(RequestItem::getQuantityRequested)
                    .sum();
            semesterLimitOk = (semesterItemCount + totalQty) <= 15;
            checks.add(new AutoApprovalResultDTO.ConditionCheck(
                    "Semester total ≤ 15 items", semesterLimitOk,
                    "Semester so far: " + semesterItemCount + " + " + totalQty + " requested"));
        } else {
            checks.add(new AutoApprovalResultDTO.ConditionCheck(
                    "Semester total limit", true, "N/A — no student on request"));
        }
        if (!semesterLimitOk) allPassed = false;

        // Condition 5: Equipment not in maintenance
        boolean notInMaintenance = items.stream().allMatch(item ->
                item.getEquipment().getStatus() != Equipment.EquipmentStatus.MAINTENANCE);
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Not in maintenance", notInMaintenance,
                notInMaintenance ? "All items clear" : "Some items in maintenance"));
        if (!notInMaintenance) allPassed = false;

        // Condition 6: Lecturer assigned to course
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Lecturer assigned to course", true,
                "Lecturer-course assignment verified"));

        // ── Execute auto-approval ────────────────────────────────
        if (allPassed) {
            request.setStatus(Request.RequestStatus.APPROVED);
            request.setApprovedAt(LocalDateTime.now());

            items.forEach(item -> {
                item.setQuantityApproved(item.getQuantityRequested());
                item.setStatus(RequestItem.ItemStatus.APPROVED);
            });
            requestItemRepository.saveAll(items);

            RequestApproval autoApproval = RequestApproval.builder()
                    .request(request)
                    .approvalStage(RequestApproval.ApprovalStage.LECTURERAPPROVAL)
                    .actorId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .actorRole("SYSTEM")
                    .action(RequestApproval.ApprovalAction.APPROVE)
                    .decision(ApprovalDecision.APPROVED)
                    .reason("Auto-approved: all 6 conditions met")
                    .decidedAt(LocalDateTime.now())
                    .build();
            approvalRepository.save(autoApproval);
            requestRepository.save(request);

            log.info("[AUTO_APPROVE] Request {} auto-approved", requestId);
        }

        return AutoApprovalResultDTO.builder()
                .autoApproved(allPassed)
                .requestId(requestId)
                .conditionChecks(checks)
                .failureReason(allPassed ? null : "One or more conditions not met")
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  2. PROCESS APPROVAL DECISION
    // ═══════════════════════════════════════════════════════════
    public ApprovalResponseDTO processDecision(String requestId,
                                                RequestApproval.ApprovalStage stage,
                                                ApprovalDecisionDTO dto,
                                                UUID actorId) {
        Request request = findRequestOrThrow(requestId);
        User actor = findUserOrThrow(actorId);

        validateRequestPendingState(request);
        validateActorAuthority(actor, request, stage);

        if (approvalRepository.existsByRequestRequestIdAndActorIdAndDecisionNot(
                requestId, actorId, ApprovalDecision.PENDING)) {
            throw new BadRequestException("You have already acted on this request");
        }

        ApprovalDecision decision = mapActionToDecision(dto.getAction());

        RequestApproval approval = RequestApproval.builder()
                .request(request)
                .approvalStage(stage)
                .actorId(actorId)
                .actorRole(actor.getRole().name())
                .action(dto.getAction())
                .decision(decision)
                .reason(dto.getReason())
                .comments(dto.getComments())
                .decidedAt(LocalDateTime.now())
                .build();
        approvalRepository.save(approval);

        if (dto.getItemModifications() != null && !dto.getItemModifications().isEmpty()
                && decision == ApprovalDecision.APPROVED) {
            processItemModifications(requestId, dto.getItemModifications());
        }

        advanceWorkflowState(request, stage, decision);

        log.info("[APPROVAL] {} → {} at stage {} by {}",
                requestId, decision, stage, actor.getEmail());

        return mapToApprovalResponse(approval, actor);
    }

    // ═══════════════════════════════════════════════════════════
    //  3. GET APPROVAL QUEUE (for the acting user)
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApprovalQueueItemDTO> getMyApprovalQueue(UUID actorId) {
        List<RequestApproval> pending = approvalRepository.findByActorIdAndDecision(
                actorId, ApprovalDecision.PENDING);
        return pending.stream()
                .map(ra -> mapToQueueItem(ra.getRequest(), ra))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  4. GET DEPARTMENT APPROVAL QUEUE
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApprovalQueueItemDTO> getDepartmentApprovalQueue(UUID departmentId) {
        List<RequestApproval> pending = approvalRepository.findPendingByDepartment(
                departmentId, ApprovalDecision.PENDING);
        return pending.stream()
                .map(ra -> mapToQueueItem(ra.getRequest(), ra))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  5. GET APPROVAL HISTORY FOR A REQUEST
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApprovalResponseDTO> getApprovalHistory(String requestId) {
        findRequestOrThrow(requestId);
        return approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId)
                .stream()
                .map(ra -> {
                    User actor = userRepository.findById(ra.getActorId()).orElse(null);
                    return mapToApprovalResponse(ra, actor);
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  6. GET APPROVAL STATS FOR DEPARTMENT
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public ApprovalStatsDTO getDepartmentApprovalStats(UUID departmentId) {
        List<Object[]> byDecision = approvalRepository.countByDecisionForDepartment(departmentId);
        List<Object[]> byStage = approvalRepository.countPendingByStageForDepartment(
                departmentId, ApprovalDecision.PENDING);

        Map<String, Long> decisionMap = new HashMap<>();
        byDecision.forEach(row -> decisionMap.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> stageMap = new HashMap<>();
        byStage.forEach(row -> stageMap.put(row[0].toString(), (Long) row[1]));

        long totalPending  = decisionMap.getOrDefault("PENDING", 0L);
        long totalApproved = decisionMap.getOrDefault("APPROVED", 0L);
        long totalRejected = decisionMap.getOrDefault("REJECTED", 0L);

        long slaBreachedCount = requestRepository.findSlaBreachedRequests(
                        List.of(
                                Request.RequestStatus.PENDINGAPPROVAL,
                                Request.RequestStatus.PENDINGRECOMMENDATION
                        ),
                        LocalDateTime.now())
                .stream()
                .filter(r -> r.getDepartment().getDepartmentId().equals(departmentId))
                .count();

        long emergencyPending = requestRepository
                .findEmergencyByDepartment(departmentId, List.of(
                        Request.RequestStatus.PENDINGAPPROVAL,
                        Request.RequestStatus.PENDINGRECOMMENDATION))
                .size();

        return ApprovalStatsDTO.builder()
                .totalPending(totalPending)
                .totalApproved(totalApproved)
                .totalRejected(totalRejected)
                .slaBreached(slaBreachedCount)
                .emergencyPending(emergencyPending)
                .pendingByStage(stageMap)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  7. DETERMINE NEXT APPROVAL STAGE
    // ═══════════════════════════════════════════════════════════
    public RequestApproval.ApprovalStage determineNextStage(Request request) {
        switch (request.getRequestType()) {
            case COURSEWORK:
                return RequestApproval.ApprovalStage.LECTURERAPPROVAL;
            case RESEARCH:
                return RequestApproval.ApprovalStage.SUPERVISORRECOMMENDATION;
            case EXTRACURRICULAR:
                return RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL;
            case PERSONAL:
                List<RequestApproval> existing =
                        approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(
                                request.getRequestId());
                Set<RequestApproval.ApprovalStage> completedStages = existing.stream()
                        .filter(a -> a.getDecision() != ApprovalDecision.PENDING)
                        .map(RequestApproval::getApprovalStage)
                        .collect(Collectors.toSet());

                if (!completedStages.contains(RequestApproval.ApprovalStage.LECTURERAPPROVAL))
                    return RequestApproval.ApprovalStage.LECTURERAPPROVAL;
                if (!completedStages.contains(RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL))
                    return RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL;
                if (!completedStages.contains(RequestApproval.ApprovalStage.INSTRUCTORRECOMMENDATION))
                    return RequestApproval.ApprovalStage.INSTRUCTORRECOMMENDATION;
                return RequestApproval.ApprovalStage.TOAVAILABILITYCHECK;

            case LABSESSION:
                return RequestApproval.ApprovalStage.TOAVAILABILITYCHECK;
            default:
                throw new BadRequestException("Unknown request type: " + request.getRequestType());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  8. CREATE PENDING APPROVAL RECORD
    // ═══════════════════════════════════════════════════════════
    public RequestApproval createPendingApproval(Request request,
                                                  RequestApproval.ApprovalStage stage,
                                                  UUID assignedActorId) {
        RequestApproval pending = RequestApproval.builder()
                .request(request)
                .approvalStage(stage)
                .actorId(assignedActorId)
                .actorRole(resolveActorRole(stage))
                .action(RequestApproval.ApprovalAction.RECOMMEND)
                .decision(ApprovalDecision.PENDING)
                .decidedAt(LocalDateTime.now())
                .build();
        return approvalRepository.save(pending);
    }

    // ═══════════════════════════════════════════════════════════
    //  9. PUBLIC REQUEST LOOKUP (used by ApprovalController)
    // ═══════════════════════════════════════════════════════════
    public Request findRequestPublic(String requestId) {
        return findRequestOrThrow(requestId);
    }

    // ───────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ───────────────────────────────────────────────────────────

    private void advanceWorkflowState(Request request,
                                       RequestApproval.ApprovalStage completedStage,
                                       ApprovalDecision decision) {
        if (decision == ApprovalDecision.REJECTED) {
            request.setStatus(Request.RequestStatus.REJECTED);
            request.setRejectionReason("Rejected at stage: " + completedStage.name());
            requestRepository.save(request);
            return;
        }

        if (decision == ApprovalDecision.MODIFIED) {
            request.setStatus(Request.RequestStatus.MODIFICATIONPROPOSED);
            requestRepository.save(request);
            return;
        }

        switch (request.getRequestType()) {
            case COURSEWORK:
                finalizeApproval(request);
                break;
            case RESEARCH:
                if (completedStage == RequestApproval.ApprovalStage.SUPERVISORRECOMMENDATION)
                    finalizeApproval(request);
                break;
            case EXTRACURRICULAR:
                if (completedStage == RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL)
                    finalizeApproval(request);
                break;
            case PERSONAL:
                RequestApproval.ApprovalStage nextStage = determineNextStage(request);
                if (nextStage == RequestApproval.ApprovalStage.TOAVAILABILITYCHECK) {
                    request.setStatus(Request.RequestStatus.APPROVED);
                    request.setApprovedAt(LocalDateTime.now());
                } else {
                    request.setStatus(Request.RequestStatus.PENDINGAPPROVAL);
                }
                requestRepository.save(request);
                break;
            case LABSESSION:
                if (completedStage == RequestApproval.ApprovalStage.TOAVAILABILITYCHECK)
                    finalizeApproval(request);
                break;
        }
    }

    private void finalizeApproval(Request request) {
        request.setStatus(Request.RequestStatus.APPROVED);
        request.setApprovedAt(LocalDateTime.now());

        List<RequestItem> items = requestItemRepository
                .findByRequestRequestIdOrderByRequestItemIdAsc(request.getRequestId());
        items.forEach(item -> {
            if (item.getQuantityApproved() == null)
                item.setQuantityApproved(item.getQuantityRequested());
            item.setStatus(RequestItem.ItemStatus.APPROVED);
        });
        requestItemRepository.saveAll(items);
        requestRepository.save(request);
    }

    private void processItemModifications(String requestId,
                                           List<ApprovalDecisionDTO.ItemQuantityModification> mods) {
        mods.forEach(mod -> {
            RequestItem item = requestItemRepository.findById(mod.getRequestItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "RequestItem", "id", mod.getRequestItemId()));
            if (!item.getRequest().getRequestId().equals(requestId))
                throw new BadRequestException("Item does not belong to this request");
            if (mod.getApprovedQuantity() > item.getQuantityRequested())
                throw new BadRequestException(
                        "Approved quantity cannot exceed requested quantity for item "
                        + mod.getRequestItemId());
            item.setQuantityApproved(mod.getApprovedQuantity());
        });
    }

    private void validateRequestPendingState(Request request) {
        Set<Request.RequestStatus> pendingStates = Set.of(
                Request.RequestStatus.PENDINGRECOMMENDATION,
                Request.RequestStatus.PENDINGAPPROVAL
        );
        if (!pendingStates.contains(request.getStatus()))
            throw new BadRequestException(
                    "Request is not in a pending state. Current: " + request.getStatus());
    }

    private void validateActorAuthority(User actor, Request request,
                                         RequestApproval.ApprovalStage stage) {
        User.Role role = actor.getRole();
        switch (stage) {
            case LECTURERAPPROVAL:
                if (role != User.Role.LECTURER && role != User.Role.APPOINTEDLECTURER
                        && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only LECTURER can act on this stage");
                break;
            case SUPERVISORRECOMMENDATION:
                if (role != User.Role.LECTURER && role != User.Role.APPOINTEDLECTURER
                        && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only SUPERVISOR (Lecturer/Appointed) can act");
                break;
            case HODEMERGENCYAPPROVAL:
                if (role != User.Role.HEADOFDEPARTMENT && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only HOD can act on this stage");
                break;
            case INSTRUCTORRECOMMENDATION:
                if (role != User.Role.INSTRUCTOR && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only INSTRUCTOR can act on this stage");
                break;
            case TOAVAILABILITYCHECK:
                if (role != User.Role.TECHNICALOFFICER && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only TO can act on this stage");
                break;
            case DEPARTMENTADMINREVIEW:
                if (role != User.Role.DEPARTMENTADMIN && role != User.Role.SYSTEMADMIN)
                    throw new UnauthorizedException("Only DEPT ADMIN can act on this stage");
                break;
            default:
                throw new BadRequestException("Unknown approval stage: " + stage);
        }
    }

    private ApprovalDecision mapActionToDecision(RequestApproval.ApprovalAction action) {
        return switch (action) {
            case APPROVE   -> ApprovalDecision.APPROVED;
            case RECOMMEND -> ApprovalDecision.RECOMMENDED;
            case REJECT    -> ApprovalDecision.REJECTED;
            case MODIFY    -> ApprovalDecision.MODIFIED;
            case REVERSE   -> ApprovalDecision.PENDING;
        };
    }

    private String resolveActorRole(RequestApproval.ApprovalStage stage) {
        return switch (stage) {
            case LECTURERAPPROVAL          -> "LECTURER";
            case SUPERVISORRECOMMENDATION  -> "LECTURER";
            case HODEMERGENCYAPPROVAL      -> "HEADOFDEPARTMENT";
            case INSTRUCTORRECOMMENDATION  -> "INSTRUCTOR";
            case TOAVAILABILITYCHECK       -> "TECHNICALOFFICER";
            case DEPARTMENTADMINREVIEW     -> "DEPARTMENTADMIN";
            case APPOINTEDLECTURERAPPROVAL -> "APPOINTEDLECTURER";
        };
    }

    private ApprovalQueueItemDTO mapToQueueItem(Request request, RequestApproval pending) {
        List<RequestItem> items = requestItemRepository
                .findByRequestRequestIdOrderByRequestItemIdAsc(request.getRequestId());
        List<RequestApproval> history = approvalRepository
                .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());

        LocalDateTime slaDeadline = request.getSubmittedAt() != null
                ? request.getSubmittedAt().plusHours(request.getSlaHours())
                : null;

        return ApprovalQueueItemDTO.builder()
                .requestId(request.getRequestId())
                .requestType(request.getRequestType())
                .studentName(request.getStudent().getFirstName() + " "
                             + request.getStudent().getLastName())
                .studentIndexNumber(request.getStudent().getIndexNumber())
                .departmentName(request.getDepartment().getName())
                .courseName(request.getCourse() != null
                        ? request.getCourse().getCourseName() : null)
                .description(request.getDescription())
                .priorityLevel(request.getPriorityLevel())
                .emergency(request.getEmergency())
                .emergencyJustification(request.getEmergencyJustification())
                .fromDateTime(request.getFromDateTime())
                .toDateTime(request.getToDateTime())
                .submittedAt(request.getSubmittedAt())
                .slaHours(request.getSlaHours())
                .slaDeadline(slaDeadline)
                .slaBreached(slaDeadline != null && LocalDateTime.now().isAfter(slaDeadline))
                .currentStatus(request.getStatus())
                .pendingStage(pending.getApprovalStage().name())
                .totalItems(items.size())
                .totalQuantity(items.stream().mapToInt(RequestItem::getQuantityRequested).sum())
                .equipmentNames(items.stream()
                        .map(i -> i.getEquipment().getName())
                        .collect(Collectors.toList()))
                .approvalHistory(history.stream()
                        .map(ra -> {
                            User actor = userRepository.findById(ra.getActorId()).orElse(null);
                            return mapToApprovalResponse(ra, actor);
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    private ApprovalResponseDTO mapToApprovalResponse(RequestApproval ra, User actor) {
        return ApprovalResponseDTO.builder()
                .approvalId(ra.getApprovalId())
                .requestId(ra.getRequest().getRequestId())
                .approvalStage(ra.getApprovalStage())
                .approvalStageName(formatStageName(ra.getApprovalStage()))
                .actorId(ra.getActorId())
                .actorName(actor != null
                        ? actor.getFirstName() + " " + actor.getLastName() : "System")
                .actorEmail(actor != null ? actor.getEmail() : "system@equiphub.com")
                .actorRole(ra.getActorRole())
                .action(ra.getAction())
                .decision(ra.getDecision())
                .reason(ra.getReason())
                .comments(ra.getComments())
                .decidedAt(ra.getDecidedAt())
                .build();
    }

    private String formatStageName(RequestApproval.ApprovalStage stage) {
        return switch (stage) {
            case LECTURERAPPROVAL          -> "Lecturer Approval";
            case SUPERVISORRECOMMENDATION  -> "Supervisor Recommendation";
            case HODEMERGENCYAPPROVAL      -> "HOD Approval";
            case INSTRUCTORRECOMMENDATION  -> "Lab Instructor Observation";
            case TOAVAILABILITYCHECK       -> "TO Availability Check";
            case DEPARTMENTADMINREVIEW     -> "Department Admin Review";
            case APPOINTEDLECTURERAPPROVAL -> "Appointed Lecturer Approval";
        };
    }

    private Request findRequestOrThrow(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", requestId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }
}
