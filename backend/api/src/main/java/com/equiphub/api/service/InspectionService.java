package com.equiphub.api.service;

import com.equiphub.api.dto.inspection.*;
import com.equiphub.api.exception.BadRequestException;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.model.*;
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
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final RequestRepository requestRepository;
    private final RequestItemRepository requestItemRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;

    // ═══════════════════════════════════════════════════════════
    //  1. ISSUE EQUIPMENT (Pre-issuance inspection + handover)
    //     TO performs: inspect → record condition → issue to student
    // ═══════════════════════════════════════════════════════════
    public List<InspectionResponseDTO> issueEquipment(IssueEquipmentDTO dto, UUID inspectorId) {
        Request request = findRequestOrThrow(dto.getRequestId());

        // Validate: request must be APPROVED
        if (request.getStatus() != Request.RequestStatus.APPROVED) {
            throw new BadRequestException(
                    "Can only issue equipment for APPROVED requests. Current: " + request.getStatus());
        }

        User inspector = findUserOrThrow(inspectorId);
        List<InspectionResponseDTO> results = new ArrayList<>();

        for (IssueEquipmentDTO.ItemIssue itemDto : dto.getItems()) {
            RequestItem item = findRequestItemOrThrow(itemDto.getRequestItemId());
            validateItemBelongsToRequest(item, request);

            // Check: not already issued
            if (item.getStatus() == RequestItem.ItemStatus.ISSUED) {
                throw new BadRequestException("Item " + itemDto.getRequestItemId() +
                        " is already issued");
            }

            // Validate quantity
            int approvedQty = item.getQuantityApproved() != null
                    ? item.getQuantityApproved() : item.getQuantityRequested();
            if (itemDto.getQuantityToIssue() > approvedQty) {
                throw new BadRequestException("Cannot issue more than approved quantity (" +
                        approvedQty + ") for item " + itemDto.getRequestItemId());
            }

            // Create PRE-ISSUANCE inspection
            Inspection inspection = Inspection.builder()
                    .requestItem(item)
                    .inspectionType(com.equiphub.api.model.InspectionType.PREISSUANCE)
                    .inspectorId(inspectorId)
                    .conditionBefore(itemDto.getConditionBefore())
                    .notes(itemDto.getNotes())
                    .penaltyApplicable(false)
                    .studentAcknowledged(false)
                    .inspectedAt(LocalDateTime.now())
                    .build();
            inspectionRepository.save(inspection);

            // Update item: mark as ISSUED
            item.setQuantityIssued(itemDto.getQuantityToIssue());
            item.setStatus(RequestItem.ItemStatus.ISSUED);
            requestItemRepository.save(item);

            // Update equipment status to INUSE
            Equipment equipment = item.getEquipment();
            equipment.setStatus(Equipment.EquipmentStatus.INUSE);
            equipmentRepository.save(equipment);

            results.add(mapToResponse(inspection, inspector, item));
        }

        // Update request status to INUSE
        request.setStatus(Request.RequestStatus.INUSE);
        requestRepository.save(request);

        log.info("[EQUIPMENT_ISSUED] Request {} — {} items issued by TO {}",
                dto.getRequestId(), dto.getItems().size(), inspector.getEmail());

        return results;
    }

    // ═══════════════════════════════════════════════════════════
    //  2. RETURN + POST-RETURN INSPECTION
    //     TO receives equipment → inspects condition → records damage
    // ═══════════════════════════════════════════════════════════
    public List<InspectionResponseDTO> processReturn(ReturnInspectionDTO dto, UUID inspectorId) {
        Request request = findRequestOrThrow(dto.getRequestId());

        // Validate: request must be INUSE
        if (request.getStatus() != Request.RequestStatus.INUSE) {
            throw new BadRequestException(
                    "Can only process returns for IN_USE requests. Current: " + request.getStatus());
        }

        User inspector = findUserOrThrow(inspectorId);
        List<InspectionResponseDTO> results = new ArrayList<>();
        boolean anyDamage = false;

        for (ReturnInspectionDTO.ItemReturnInspection itemDto : dto.getItems()) {
            RequestItem item = findRequestItemOrThrow(itemDto.getRequestItemId());
            validateItemBelongsToRequest(item, request);

            if (item.getStatus() != RequestItem.ItemStatus.ISSUED) {
                throw new BadRequestException("Item " + itemDto.getRequestItemId() +
                        " is not currently issued");
            }

            // Get pre-issuance condition for comparison
            Inspection preInspection = inspectionRepository
                    .findByRequestItemRequestItemIdAndInspectionType(
                            item.getRequestItemId(), com.equiphub.api.model.InspectionType.PREISSUANCE)
                    .orElse(null);
            int conditionBefore = preInspection != null
                    ? preInspection.getConditionBefore()
                    : item.getEquipment().getCurrentCondition();

            // Determine if damage occurred
            boolean damaged = itemDto.getDamageLevel() != null && itemDto.getDamageLevel() > 0;
            boolean conditionDropped = itemDto.getConditionAfter() < conditionBefore;
            boolean penaltyApplicable = damaged || (conditionDropped &&
                    (conditionBefore - itemDto.getConditionAfter()) > 20);

            if (damaged) anyDamage = true;

            // Create POST-RETURN inspection
            Inspection inspection = Inspection.builder()
                    .requestItem(item)
                    .inspectionType(com.equiphub.api.model.InspectionType.POSTRETURN)
                    .inspectorId(inspectorId)
                    .conditionBefore(conditionBefore)
                    .conditionAfter(itemDto.getConditionAfter())
                    .damageLevel(itemDto.getDamageLevel())
                    .damageDescription(itemDto.getDamageDescription())
                    .damagePhotos(itemDto.getDamagePhotos())
                    .penaltyApplicable(penaltyApplicable)
                    .studentAcknowledged(false)
                    .notes(itemDto.getNotes())
                    .inspectedAt(LocalDateTime.now())
                    .build();
            inspectionRepository.save(inspection);

            // Update item: mark quantities returned and status
            item.setQuantityReturned(itemDto.getQuantityReturned());
            if (damaged) {
                item.setStatus(RequestItem.ItemStatus.DAMAGED);
            } else {
                item.setStatus(RequestItem.ItemStatus.RETURNED);
            }
            requestItemRepository.save(item);

            // Update equipment condition + status
            Equipment equipment = item.getEquipment();
            equipment.setCurrentCondition(itemDto.getConditionAfter());
            if (damaged && itemDto.getDamageLevel() >= 3) {
                equipment.setStatus(Equipment.EquipmentStatus.DAMAGED);
                equipment.setConditionNotes("Damaged during use — " +
                        (itemDto.getDamageDescription() != null
                                ? itemDto.getDamageDescription() : "See inspection report"));
            } else {
                equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
            }
            equipmentRepository.save(equipment);

            // Auto-create penalty if applicable
            if (penaltyApplicable) {
                createDamagePenalty(request, item, inspection, conditionBefore,
                        itemDto.getConditionAfter(), itemDto.getDamageLevel());
            }

            results.add(mapToResponse(inspection, inspector, item));
        }

        // Update request status
        boolean allReturned = requestItemRepository
                .findByRequestRequestIdOrderByRequestItemIdAsc(request.getRequestId())
                .stream().allMatch(i ->
                        i.getStatus() == RequestItem.ItemStatus.RETURNED
                     || i.getStatus() == RequestItem.ItemStatus.DAMAGED);

        if (allReturned) {
            request.setStatus(anyDamage
                    ? Request.RequestStatus.PENALTYASSESSED
                    : Request.RequestStatus.RETURNED);
            request.setReturnedAt(LocalDateTime.now());
        }
        requestRepository.save(request);

        log.info("[EQUIPMENT_RETURNED] Request {} — {} items returned, damage={}",
                dto.getRequestId(), dto.getItems().size(), anyDamage);

        return results;
    }

    // ═══════════════════════════════════════════════════════════
    //  3. STUDENT ACKNOWLEDGES INSPECTION
    // ═══════════════════════════════════════════════════════════
    public InspectionResponseDTO acknowledgeInspection(Integer inspectionId, UUID studentId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", "id", inspectionId));

        // Validate: student must own the request
        Request request = inspection.getRequestItem().getRequest();
        if (!request.getStudent().getUserId().equals(studentId)) {
            throw new BadRequestException("You can only acknowledge your own inspections");
        }

        inspection.setStudentAcknowledged(true);
        inspection.setStudentAcknowledgementAt(LocalDateTime.now());
        inspectionRepository.save(inspection);

        User inspector = userRepository.findById(inspection.getInspectorId()).orElse(null);
        log.info("[INSPECTION_ACK] Student {} acknowledged inspection {}",
                studentId, inspectionId);

        return mapToResponse(inspection, inspector, inspection.getRequestItem());
    }

    // ═══════════════════════════════════════════════════════════
    //  4. GET INSPECTIONS FOR A REQUEST
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<InspectionResponseDTO> getInspectionsByRequest(String requestId) {
        findRequestOrThrow(requestId);
        return inspectionRepository.findByRequestId(requestId).stream()
                .map(i -> {
                    User inspector = userRepository.findById(i.getInspectorId()).orElse(null);
                    return mapToResponse(i, inspector, i.getRequestItem());
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  5. GET INSPECTIONS BY INSPECTOR (TO dashboard)
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<InspectionResponseDTO> getMyInspections(UUID inspectorId) {
        return inspectionRepository.findByInspectorIdOrderByInspectedAtDesc(inspectorId).stream()
                .map(i -> {
                    User inspector = userRepository.findById(i.getInspectorId()).orElse(null);
                    return mapToResponse(i, inspector, i.getRequestItem());
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  6. GET DAMAGE REPORT (department-wide)
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<InspectionResponseDTO> getDamageReport(UUID departmentId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return inspectionRepository.findDamagedSince(since).stream()
                .filter(i -> {
                    Equipment eq = i.getRequestItem().getEquipment();
                    return eq.getDepartment().getDepartmentId().equals(departmentId);
                })
                .map(i -> {
                    User inspector = userRepository.findById(i.getInspectorId()).orElse(null);
                    return mapToResponse(i, inspector, i.getRequestItem());
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  7. GET UNACKNOWLEDGED DAMAGE INSPECTIONS
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<InspectionResponseDTO> getUnacknowledgedDamage() {
        return inspectionRepository.findUnacknowledgedDamage(com.equiphub.api.model.InspectionType.POSTRETURN).stream()
                .map(i -> {
                    User inspector = userRepository.findById(i.getInspectorId()).orElse(null);
                    return mapToResponse(i, inspector, i.getRequestItem());
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  8. INSPECTION STATS FOR DEPARTMENT
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public InspectionSummaryDTO getDepartmentInspectionStats(UUID departmentId) {
        List<Object[]> byType = inspectionRepository.countByTypeForDepartment(departmentId);
        Object[] avgScores = inspectionRepository.avgConditionScoresForDepartment(departmentId);
        List<Object[]> byDamage = inspectionRepository.countByDamageLevelForDepartment(departmentId);

        Map<String, Long> typeMap = new HashMap<>();
        byType.forEach(row -> typeMap.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> damageMap = new HashMap<>();
        byDamage.forEach(row -> damageMap.put("Level " + row[0].toString(), (Long) row[1]));

        long penaltyCount = inspectionRepository
                .findPenaltyApplicableSince(LocalDateTime.now().minusDays(365))
                .stream()
                .filter(i -> i.getRequestItem().getEquipment().getDepartment()
                        .getDepartmentId().equals(departmentId))
                .count();

        double avgBefore = avgScores[0] != null ? ((Number) avgScores[0]).doubleValue() : 0;
        double avgAfter = avgScores[1] != null ? ((Number) avgScores[1]).doubleValue() : 0;

        return InspectionSummaryDTO.builder()
                .totalInspections(typeMap.values().stream().mapToLong(Long::longValue).sum())
                .preIssuanceCount(typeMap.getOrDefault("PREISSUANCE", 0L))
                .postReturnCount(typeMap.getOrDefault("POSTRETURN", 0L))
                .damageDetectedCount(damageMap.values().stream().mapToLong(Long::longValue).sum())
                .penaltiesTriggered(penaltyCount)
                .averageConditionBefore(Math.round(avgBefore * 100.0) / 100.0)
                .averageConditionAfter(Math.round(avgAfter * 100.0) / 100.0)
                .averageConditionDelta(Math.round((avgBefore - avgAfter) * 100.0) / 100.0)
                .inspectionsByDamageLevel(damageMap)
                .build();
    }

    // ───────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ───────────────────────────────────────────────────────────

    private void createDamagePenalty(Request request, RequestItem item,
                                     Inspection inspection,
                                     int condBefore, int condAfter, Integer damageLevel) {
        int points = calculatePenaltyPoints(condBefore, condAfter, damageLevel);
        String reason = String.format("Damage detected: condition %d → %d (level %d) on %s",
                condBefore, condAfter,
                damageLevel != null ? damageLevel : 0,
                item.getEquipment().getName());

        Penalty penalty = Penalty.builder()
                .request(request)
                .student(request.getStudent())
                .penaltyType(Penalty.PenaltyType.DAMAGE)
                .points(points)
                .reason(reason)
                .calculationDetailsJson(String.format(
                        "{\"condBefore\":%d,\"condAfter\":%d,\"damageLevel\":%d,\"inspectionId\":%d}",
                        condBefore, condAfter,
                        damageLevel != null ? damageLevel : 0,
                        inspection.getInspectionId()))
                .status(Penalty.PenaltyStatus.PENDING)
                .build();
        penaltyRepository.save(penalty);

        log.warn("[PENALTY_AUTO] {} points for student {} — {}",
                points, request.getStudent().getEmail(), reason);
    }

    private int calculatePenaltyPoints(int condBefore, int condAfter, Integer damageLevel) {
        int conditionDrop = condBefore - condAfter;
        int basePenalty = 0;

        // Points based on condition drop
        if (conditionDrop > 50) basePenalty = 25;
        else if (conditionDrop > 30) basePenalty = 15;
        else if (conditionDrop > 20) basePenalty = 10;
        else if (conditionDrop > 10) basePenalty = 5;

        // Bonus points for damage level
        if (damageLevel != null) {
            basePenalty += damageLevel * 5;
        }

        return Math.max(basePenalty, 5); // Minimum 5 points
    }

    private InspectionResponseDTO mapToResponse(Inspection inspection,
                                                 User inspector,
                                                 RequestItem item) {
        Equipment equipment = item.getEquipment();
        Integer condDelta = null;
        if (inspection.getConditionBefore() != null && inspection.getConditionAfter() != null) {
            condDelta = inspection.getConditionBefore() - inspection.getConditionAfter();
        }

       return InspectionResponseDTO.builder()
        .inspectionId(inspection.getInspectionId())
        .requestItemId(item.getRequestItemId())
        .requestId(item.getRequest().getRequestId())
        .inspectionType(inspection.getInspectionType())                          // ✅ pass enum directly
        .inspectionTypeName(formatInspectionType(inspection.getInspectionType()))
        .inspectorId(inspection.getInspectorId())
        .inspectorName(inspector != null
                ? inspector.getFirstName() + " " + inspector.getLastName() : "Unknown")
        .inspectorEmail(inspector != null ? inspector.getEmail() : null)
        .equipmentId(equipment.getEquipmentId())
        .equipmentName(equipment.getName())
        .equipmentSerialNumber(equipment.getSerialNumber())
        .conditionBefore(inspection.getConditionBefore())
        .conditionBeforeLabel(conditionLabel(inspection.getConditionBefore()))
        .conditionAfter(inspection.getConditionAfter())
        .conditionAfterLabel(conditionLabel(inspection.getConditionAfter()))
        .conditionDelta(condDelta)
        .damageLevel(inspection.getDamageLevel())
        .damageLevelLabel(damageLevelLabel(inspection.getDamageLevel()))
        .damageDescription(inspection.getDamageDescription())
        .damagePhotos(inspection.getDamagePhotos())
        .preDamageEvidence(inspection.getPreDamageEvidence())
        .penaltyApplicable(inspection.getPenaltyApplicable())
        .studentAcknowledged(inspection.getStudentAcknowledged())
        .studentAcknowledgementAt(inspection.getStudentAcknowledgementAt())
        .notes(inspection.getNotes())
        .inspectedAt(inspection.getInspectedAt())
        .build();
    }



    private String conditionLabel(Integer score) {
        if (score == null) return null;
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }

    private String damageLevelLabel(Integer level) {
        if (level == null || level == 0) return "NONE";
        return switch (level) {
            case 1 -> "MINOR — Cosmetic only";
            case 2 -> "MODERATE — Functional but degraded";
            case 3 -> "SIGNIFICANT — Needs repair";
            case 4 -> "SEVERE — Major repair needed";
            case 5 -> "DESTROYED — Replacement required";
            default -> "UNKNOWN";
        };
    }

    private String formatInspectionType(com.equiphub.api.model.InspectionType type) {
        return switch (type) {
            case com.equiphub.api.model.InspectionType.PREISSUANCE -> "Pre-Issuance Inspection";
            case com.equiphub.api.model.InspectionType.POSTRETURN -> "Post-Return Inspection";
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

    private RequestItem findRequestItemOrThrow(Integer itemId) {
        return requestItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("RequestItem", "id", itemId));
    }

    private void validateItemBelongsToRequest(RequestItem item, Request request) {
        if (!item.getRequest().getRequestId().equals(request.getRequestId())) {
            throw new BadRequestException("Item " + item.getRequestItemId() +
                    " does not belong to request " + request.getRequestId());
        }
    }
}
