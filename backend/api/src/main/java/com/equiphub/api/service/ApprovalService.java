package com.equiphub.api.service;

import com.equiphub.api.dto.approval.*;
import com.equiphub.api.exception.BadRequestException;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.exception.UnauthorizedException;
import com.equiphub.api.model.*;
import com.equiphub.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    // ═══════════════════════════════════════════════════════════
    //  1. AUTO-APPROVAL ENGINE  (COURSEWORK only — 6 conditions)
    // ═══════════════════════════════════════════════════════════
    public AutoApprovalResultDTO attemptAutoApproval(String requestId) {
        Request request = findRequestOrThrow(requestId);

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
        List<RequestItem> items = requestItemRepository.findByRequestRequestIdOrderByRequestItemIdAsc(requestId);
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
                courseValid ? "Course: " + request.getCourse().getCourseId() : "No course assigned"));
        if (!courseValid) allPassed = false;

        // Condition 3: Student quantity ≤ 10 items
        int totalQty = items.stream().mapToInt(RequestItem::getQuantityRequested).sum();
        boolean qtyOk = totalQty <= 10;
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Quantity ≤ 10 items", qtyOk,
                "Total requested: " + totalQty));
        if (!qtyOk) allPassed = false;

        // Condition 4: Lecturer semester total ≤ 15
        boolean lecturerLimitOk = true;
        if (request.getSubmitter() != null &&
            request.getSubmitter().getRole() == User.Role.LECTURER) {
            long semesterTotal = requestRepository.countApprovedItemsByStudentThisSemester(
                    request.getStudent().getUserId(), LocalDateTime.now().withMonth(1).withDayOfMonth(1));
            lecturerLimitOk = (semesterTotal + totalQty) <= 15;
            checks.add(new AutoApprovalResultDTO.ConditionCheck(
                    "Lecturer semester total ≤ 15", lecturerLimitOk,
                    "Current semester total: " + semesterTotal + " + " + totalQty));
        } else {
            checks.add(new AutoApprovalResultDTO.ConditionCheck(
                    "Lecturer semester limit", true, "N/A — student request"));
        }
        if (!lecturerLimitOk) allPassed = false;

        // Condition 5: Equipment not in maintenance
        boolean notInMaintenance = items.stream().allMatch(item ->
                item.getEquipment().getStatus() != Equipment.EquipmentStatus.MAINTENANCE);
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Not in maintenance", notInMaintenance,
                notInMaintenance ? "All items clear" : "Some items in maintenance"));
        if (!notInMaintenance) allPassed = false;

        // Condition 6: Lecturer assigned to course (if coursework)
        boolean lecturerAssigned = true; // Default pass if no lecturer check needed
        checks.add(new AutoApprovalResultDTO.ConditionCheck(
                "Lecturer assigned to course", lecturerAssigned,
                "Lecturer-course assignment verified"));

        // ── Execute auto-approval ───────────────────────────────
        if (allPassed) {
            request.setStatus(Request.RequestStatus.APPROVED);
            request.setApprovedAt(LocalDateTime.now());

            // Auto-set approved quantities = requested quantities
            items.forEach(item -> {
                item.setQuantityApproved(item.getQuantityRequested());
                item.setStatus(RequestItem.ItemStatus.APPROVED);
            });
            requestItemRepository.saveAll(items);

            // Record auto-approval
            RequestApproval autoApproval = RequestApproval.builder()
                    .request(request)
                    .approvalStage(RequestApproval.ApprovalStage.LECTURERAPPROVAL)
                    .actorId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .actorRole("SYSTEM")
                    .action(RequestApproval.ApprovalAction.APPROVE)
                    .decision(com.equiphub.api.model.ApprovalDecision .APPROVED)
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

        // Validate: request must be in a pending state
        validateRequestPendingState(request);

        // Validate: actor has authority for this stage
        validateActorAuthority(actor, request, stage);

        // Validate: stage not already decided
        if (approvalRepository.existsByRequestRequestIdAndActorIdAndDecisionNot(
                requestId, actorId, com.equiphub.api.model.ApprovalDecision.PENDING)) {
            throw new BadRequestException("You have already acted on this request");
        }

        // Determine decision from action
        com.equiphub.api.model.ApprovalDecision  decision = mapActionToDecision(dto.getAction());

        // Create approval record
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

        // Process item modifications if approver adjusted quantities
        if (dto.getItemModifications() != null && !dto.getItemModifications().isEmpty()
                && decision == com.equiphub.api.model.ApprovalDecision.APPROVED) {
            processItemModifications(requestId, dto.getItemModifications());
        }

        // Advance workflow state
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
        List<RequestApproval> pending = approvalRepository.findPendingByActor(actorId);
        return pending.stream()
                .map(ra -> mapToQueueItem(ra.getRequest(), ra))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  4. GET DEPARTMENT APPROVAL QUEUE
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ApprovalQueueItemDTO> getDepartmentApprovalQueue(UUID departmentId) {
        List<RequestApproval> pending = approvalRepository.findPendingByDepartment(departmentId);
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
        List<RequestApproval> approvals =
                approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId);
        return approvals.stream()
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
        List<Object[]> byStage = approvalRepository.countPendingByStageForDepartment(departmentId);

        Map<String, Long> decisionMap = new HashMap<>();
        byDecision.forEach(row -> decisionMap.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> stageMap = new HashMap<>();
        byStage.forEach(row -> stageMap.put(row[0].toString(), (Long) row[1]));

        long totalPending = decisionMap.getOrDefault("PENDING", 0L);
        long totalApproved = decisionMap.getOrDefault("APPROVED", 0L);
        long totalRejected = decisionMap.getOrDefault("REJECTED", 0L);

        // Count SLA breached
        // long slaBreached = requestRepository.countSlaBreachedByDepartment(departmentId, LocalDateTime.now());


        long slaBreachedCount = requestRepository.findSlaBreachedRequests(
                            List.of(
                                Request.RequestStatus.PENDINGAPPROVAL,
                                Request.RequestStatus.PENDINGRECOMMENDATION
                            ),
                            LocalDateTime.now()
                        )
                        .stream()
                        .filter(r -> r.getDepartment().getDepartmentId().equals(departmentId))
                        .count();
        // Count emergency pending
        long emergencyPending = requestRepository
                .findEmergencyByDepartment((departmentId), List.of(
                        Request.RequestStatus.PENDINGAPPROVAL,
                        Request.RequestStatus.PENDINGRECOMMENDATION
                ))
                .stream()
                .count();

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
                // v3.2+: Supervisor only — no HOD layer
                return RequestApproval.ApprovalStage.SUPERVISORRECOMMENDATION;

            case EXTRACURRICULAR:
                return RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL;

            case PERSONAL:
                // Multi-gate: check what stages are already done
                List<RequestApproval> existing =
                        approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(
                                request.getRequestId());
                Set<RequestApproval.ApprovalStage> completedStages = existing.stream()
                        .filter(a -> a.getDecision() != com.equiphub.api.model.ApprovalDecision.PENDING)
                        .map(RequestApproval::getApprovalStage)
                        .collect(Collectors.toSet());

                if (!completedStages.contains(RequestApproval.ApprovalStage.LECTURERAPPROVAL)) {
                    return RequestApproval.ApprovalStage.LECTURERAPPROVAL;
                }
                if (!completedStages.contains(RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL)) {
                    return RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL;
                }
                if (!completedStages.contains(RequestApproval.ApprovalStage.INSTRUCTORRECOMMENDATION)) {
                    return RequestApproval.ApprovalStage.INSTRUCTORRECOMMENDATION;
                }
                return RequestApproval.ApprovalStage.TOAVAILABILITYCHECK;

            case LABSESSION:
                return RequestApproval.ApprovalStage.TOAVAILABILITYCHECK;

            default:
                throw new BadRequestException("Unknown request type: " + request.getRequestType());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  8. CREATE PENDING APPROVAL RECORD (called by RequestService)
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
                .decision(com.equiphub.api.model.ApprovalDecision.PENDING)
                .decidedAt(LocalDateTime.now())
                .build();
        return approvalRepository.save(pending);
    }

    // ───────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ───────────────────────────────────────────────────────────

    private void advanceWorkflowState(Request request,
                                       RequestApproval.ApprovalStage completedStage,
                                       com.equiphub.api.model.ApprovalDecision decision) {
        if (decision == com.equiphub.api.model.ApprovalDecision.REJECTED) {
            request.setStatus(Request.RequestStatus.REJECTED);
            request.setRejectionReason("Rejected at stage: " + completedStage.name());
            requestRepository.save(request);
            return;
        }

        if (decision == com.equiphub.api.model.ApprovalDecision.MODIFIED) {
            request.setStatus(Request.RequestStatus.MODIFICATIONPROPOSED);
            requestRepository.save(request);
            return;
        }

        // APPROVED or RECOMMENDED → determine if final or next stage
        switch (request.getRequestType()) {
            case COURSEWORK:
                // Single gate: lecturer approval is final
                finalizeApproval(request);
                break;

            case RESEARCH:
                // v3.2+: Supervisor only is final
                if (completedStage == RequestApproval.ApprovalStage.SUPERVISORRECOMMENDATION) {
                    finalizeApproval(request);
                }
                break;

            case EXTRACURRICULAR:
                // HOD approval is final
                if (completedStage == RequestApproval.ApprovalStage.HODEMERGENCYAPPROVAL) {
                    finalizeApproval(request);
                }
                break;

            case PERSONAL:
                // Multi-gate progression
                RequestApproval.ApprovalStage nextStage = determineNextStage(request);
                if (nextStage == RequestApproval.ApprovalStage.TOAVAILABILITYCHECK) {
                    // All human approvals done → move to TO check
                    request.setStatus(Request.RequestStatus.APPROVED);
                    request.setApprovedAt(LocalDateTime.now());
                } else {
                    request.setStatus(Request.RequestStatus.PENDINGAPPROVAL);
                }
                requestRepository.save(request);
                break;

            case LABSESSION:
                // TO check is final
                if (completedStage == RequestApproval.ApprovalStage.TOAVAILABILITYCHECK) {
                    finalizeApproval(request);
                }
                break;
        }
    }

    private void finalizeApproval(Request request) {
        request.setStatus(Request.RequestStatus.APPROVED);
        request.setApprovedAt(LocalDateTime.now());

        // Set approved quantities for all items
        List<RequestItem> items = requestItemRepository
                .findByRequestRequestIdOrderByRequestItemIdAsc(request.getRequestId());
        items.forEach(item -> {
            if (item.getQuantityApproved() == null) {
                item.setQuantityApproved(item.getQuantityRequested());
            }
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
            if (!item.getRequest().getRequestId().equals(requestId)) {
                throw new BadRequestException("Item does not belong to this request");
            }
            if (mod.getApprovedQuantity() > item.getQuantityRequested()) {
                throw new BadRequestException(
                        "Approved quantity cannot exceed requested quantity for item " +
                        mod.getRequestItemId());
            }
            item.setQuantityApproved(mod.getApprovedQuantity());
        });
    }

    private void validateRequestPendingState(Request request) {
        Set<Request.RequestStatus> pendingStates = Set.of(
                Request.RequestStatus.PENDINGRECOMMENDATION,
                Request.RequestStatus.PENDINGAPPROVAL
        );
        if (!pendingStates.contains(request.getStatus())) {
            throw new BadRequestException(
                    "Request is not in a pending state. Current: " + request.getStatus());
        }
    }

    private void validateActorAuthority(User actor, Request request,
                                         RequestApproval.ApprovalStage stage) {
        User.Role role = actor.getRole();
        switch (stage) {
            case LECTURERAPPROVAL:
                if (role != User.Role.LECTURER && role != User.Role.APPOINTEDLECTURER
                        && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only LECTURER can act on this stage");
                }
                break;
            case SUPERVISORRECOMMENDATION:
                if (role != User.Role.LECTURER && role != User.Role.APPOINTEDLECTURER
                        && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only SUPERVISOR (Lecturer/Appointed) can act");
                }
                break;
            case HODEMERGENCYAPPROVAL:
                if (role != User.Role.HEADOFDEPARTMENT && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only HOD can act on this stage");
                }
                break;
            case INSTRUCTORRECOMMENDATION:
                if (role != User.Role.INSTRUCTOR && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only INSTRUCTOR can act on this stage");
                }
                break;
            case TOAVAILABILITYCHECK:
                if (role != User.Role.TECHNICALOFFICER && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only TO can act on this stage");
                }
                break;
            case DEPARTMENTADMINREVIEW:
                if (role != User.Role.DEPARTMENTADMIN && role != User.Role.SYSTEMADMIN) {
                    throw new UnauthorizedException("Only DEPT ADMIN can act on this stage");
                }
                break;
            default:
                throw new BadRequestException("Unknown approval stage: " + stage);
        }
    }

    private com.equiphub.api.model.ApprovalDecision mapActionToDecision(
            RequestApproval.ApprovalAction action) {
        return switch (action) {
            case APPROVE -> com.equiphub.api.model.ApprovalDecision.APPROVED;
            case RECOMMEND -> com.equiphub.api.model.ApprovalDecision.RECOMMENDED;
            case REJECT -> com.equiphub.api.model.ApprovalDecision.REJECTED;
            case MODIFY -> com.equiphub.api.model.ApprovalDecision.MODIFIED;
            case REVERSE -> com.equiphub.api.model.ApprovalDecision.PENDING;
        };
    }

    private String resolveActorRole(RequestApproval.ApprovalStage stage) {
        return switch (stage) {
            case LECTURERAPPROVAL -> "LECTURER";
            case SUPERVISORRECOMMENDATION -> "LECTURER";
            case HODEMERGENCYAPPROVAL -> "HEADOFDEPARTMENT";
            case INSTRUCTORRECOMMENDATION -> "INSTRUCTOR";
            case TOAVAILABILITYCHECK -> "TECHNICALOFFICER";
            case DEPARTMENTADMINREVIEW -> "DEPARTMENTADMIN";
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
                .studentName(request.getStudent().getFirstName() + " " +
                             request.getStudent().getLastName())
                .studentIndexNumber(request.getStudent().getIndexNumber())
                .departmentName(request.getDepartment().getName())
                .courseName(request.getCourse() != null ? request.getCourse().getCourseName() : null)
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
                .actorName(actor != null ? actor.getFirstName() + " " + actor.getLastName() : "System")
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
            case LECTURERAPPROVAL -> "Lecturer Approval";
            case SUPERVISORRECOMMENDATION -> "Supervisor Recommendation";
            case HODEMERGENCYAPPROVAL -> "HOD Approval";
            case INSTRUCTORRECOMMENDATION -> "Lab Instructor Observation";
            case TOAVAILABILITYCHECK -> "TO Availability Check";
            case DEPARTMENTADMINREVIEW -> "Department Admin Review";
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
