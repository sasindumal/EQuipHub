package com.equiphub.api.service;

import com.equiphub.api.dto.penalty.*;
import com.equiphub.api.exception.BadRequestException;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.model.*;
import com.equiphub.api.model.Penalty.PenaltyStatus;
import com.equiphub.api.model.Penalty.PenaltyType;
import com.equiphub.api.model.PenaltyAppeal.AppealDecision;
import com.equiphub.api.model.PenaltyAppeal.AppealStatus;
import com.equiphub.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final PenaltyAppealRepository appealRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    // ── Threshold constants ──────────────────────────────────
    private static final int YELLOW_THRESHOLD = 10;
    private static final int ORANGE_THRESHOLD = 20;
    private static final int RED_THRESHOLD    = 30;
    private static final int APPEAL_DEADLINE_DAYS = 7;

    // ═══════════════════════════════════════════════════════════
    //  1. CREATE PENALTY
    // ═══════════════════════════════════════════════════════════
    public PenaltyResponseDTO createPenalty(CreatePenaltyDTO dto, UUID issuedById) {
        Request request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Request", "id", dto.getRequestId()));
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getStudentId()));

        if (penaltyRepository.existsByRequestRequestIdAndPenaltyType(
                dto.getRequestId(), dto.getPenaltyType())) {
            throw new BadRequestException(
                    "A " + dto.getPenaltyType() + " penalty already exists for this request");
        }

        int currentPoints = penaltyRepository.sumPointsByStudentAndStatus(
                dto.getStudentId(), PenaltyStatus.APPROVED);
        int newTotal = currentPoints + dto.getPoints();
        String level = calculateLevel(newTotal);

        Penalty penalty = Penalty.builder()
                .request(request)
                .student(student)
                .penaltyType(dto.getPenaltyType())
                .points(dto.getPoints())
                .reason(dto.getReason())
                .calculationDetailsJson(dto.getCalculationDetailsJson())
                .status(PenaltyStatus.PENDING)
                .totalPointsAfter(newTotal)
                .statusLevel(level)
                .appealed(false)
                .build();
        penalty = penaltyRepository.save(penalty);

        log.info("[PENALTY] Created penalty {} for student {} — {} points ({})",
                penalty.getPenaltyId(), student.getEmail(), dto.getPoints(), dto.getPenaltyType());

        return mapToResponse(penalty);
    }

    // ═══════════════════════════════════════════════════════════
    //  2. APPROVE / CONFIRM PENALTY (by HOD / DeptAdmin)
    // ═══════════════════════════════════════════════════════════
    public PenaltyResponseDTO approvePenalty(Integer penaltyId, UUID approvedById) {
        Penalty penalty = findPenaltyOrThrow(penaltyId);

        if (penalty.getStatus() != PenaltyStatus.PENDING) {
            throw new BadRequestException("Penalty is not in PENDING state");
        }

        int currentApproved = penaltyRepository.sumPointsByStudentAndStatus(
                penalty.getStudent().getUserId(), PenaltyStatus.APPROVED);
        int newTotal = currentApproved + penalty.getPoints();

        penalty.setStatus(PenaltyStatus.APPROVED);
        penalty.setApprovedById(approvedById);
        penalty.setApprovedAt(LocalDateTime.now());
        penalty.setTotalPointsAfter(newTotal);
        penalty.setStatusLevel(calculateLevel(newTotal));
        penaltyRepository.save(penalty);

        log.info("[PENALTY] Approved penalty {} — student total now {} points ({})",
                penaltyId, newTotal, penalty.getStatusLevel());

        return mapToResponse(penalty);
    }

    // ═══════════════════════════════════════════════════════════
    //  3. WAIVE PENALTY (by HOD / SysAdmin)
    // ═══════════════════════════════════════════════════════════
    public PenaltyResponseDTO waivePenalty(Integer penaltyId, UUID waivedById, String reason) {
        Penalty penalty = findPenaltyOrThrow(penaltyId);

        penalty.setStatus(PenaltyStatus.WAIVED);
        penalty.setApprovedById(waivedById);
        penalty.setApprovedAt(LocalDateTime.now());
        penalty.setReason(penalty.getReason() + " | WAIVED: " + reason);
        penaltyRepository.save(penalty);

        log.info("[PENALTY] Waived penalty {} by {}", penaltyId, waivedById);
        return mapToResponse(penalty);
    }

    // ═══════════════════════════════════════════════════════════
    //  4. GET STUDENT PENALTIES
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getStudentPenalties(UUID studentId) {
        return penaltyRepository.findByStudentUserIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  5. GET STUDENT PENALTY SUMMARY
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public StudentPenaltySummaryDTO getStudentSummary(UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", studentId));

        int activePoints = penaltyRepository.sumPointsByStudentAndStatus(
                studentId, PenaltyStatus.APPROVED);
        List<Penalty> all = penaltyRepository.findByStudentUserIdOrderByCreatedAtDesc(studentId);

        long pending  = all.stream().filter(p -> p.getStatus() == PenaltyStatus.PENDING).count();
        long appealed = all.stream().filter(p -> p.getStatus() == PenaltyStatus.APPEALED).count();
        long lateReturn = penaltyRepository.countByStudentAndType(studentId, PenaltyType.LATERETURN);
        long damage     = penaltyRepository.countByStudentAndType(studentId, PenaltyType.DAMAGE);

        String level = calculateLevel(activePoints);

        return StudentPenaltySummaryDTO.builder()
                .studentId(studentId)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .studentIndexNumber(student.getIndexNumber())
                .totalActivePoints(activePoints)
                .currentLevel(level)
                .totalPenalties(all.size())
                .pendingPenalties(pending)
                .appealedPenalties(appealed)
                .lateReturnCount(lateReturn)
                .damageCount(damage)
                .borrowingRestricted(activePoints >= RED_THRESHOLD)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  6. GET DEPARTMENT PENALTIES
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getDepartmentPenalties(UUID departmentId) {
        return penaltyRepository.findByDepartment(departmentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  7. GET PENDING PENALTIES FOR DEPARTMENT
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<PenaltyResponseDTO> getDepartmentPendingPenalties(UUID departmentId) {
        return penaltyRepository.findByDepartmentAndStatus(departmentId, PenaltyStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  8. SUBMIT APPEAL (by student)
    // ═══════════════════════════════════════════════════════════
    public PenaltyResponseDTO submitAppeal(AppealRequestDTO dto, UUID studentId) {
        Penalty penalty = findPenaltyOrThrow(dto.getPenaltyId());

        if (!penalty.getStudent().getUserId().equals(studentId)) {
            throw new BadRequestException("You can only appeal your own penalties");
        }
        if (penalty.getStatus() != PenaltyStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED penalties can be appealed");
        }
        if (Boolean.TRUE.equals(penalty.getAppealed())) {
            throw new BadRequestException("This penalty has already been appealed");
        }

        PenaltyAppeal appeal = PenaltyAppeal.builder()
                .penalty(penalty)
                .student(penalty.getStudent())
                .appealReason(dto.getAppealReason())
                .evidenceDocuments(dto.getEvidenceDocuments())
                .appealStatus(AppealStatus.PENDING)
                .appealedAt(LocalDateTime.now())
                .appealDeadline(LocalDateTime.now().plusDays(APPEAL_DEADLINE_DAYS))
                .build();
        appealRepository.save(appeal);

        penalty.setStatus(PenaltyStatus.APPEALED);
        penalty.setAppealed(true);
        penaltyRepository.save(penalty);

        log.info("[APPEAL] Student {} appealed penalty {}", studentId, dto.getPenaltyId());
        return mapToResponse(penalty);
    }

    // ═══════════════════════════════════════════════════════════
    //  9. DECIDE APPEAL (by HOD / DeptAdmin)
    // ═══════════════════════════════════════════════════════════
    public PenaltyResponseDTO decideAppeal(Integer penaltyId,
                                            AppealDecisionDTO dto,
                                            UUID decidedById) {
        PenaltyAppeal appeal = appealRepository.findByPenaltyPenaltyId(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal", "penaltyId", penaltyId));

        if (appeal.getAppealStatus() != AppealStatus.PENDING
                && appeal.getAppealStatus() != AppealStatus.UNDERREVIEW) {
            throw new BadRequestException("Appeal is not in a decidable state");
        }

        Penalty penalty = appeal.getPenalty();
        appeal.setDecidedById(decidedById);
        appeal.setDecision(dto.getDecision());
        appeal.setDecisionReason(dto.getDecisionReason());
        appeal.setDecidedAt(LocalDateTime.now());

        switch (dto.getDecision()) {
            case APPROVED -> {
                appeal.setAppealStatus(AppealStatus.APPROVED);
                appeal.setPointsWaived(penalty.getPoints());
                penalty.setStatus(PenaltyStatus.WAIVED);
            }
            case PARTIALLYWAIVED -> {
                int waived = dto.getPointsWaived() != null ? dto.getPointsWaived() : 0;
                if (waived <= 0 || waived >= penalty.getPoints()) {
                    throw new BadRequestException(
                            "Waived points must be between 1 and " + (penalty.getPoints() - 1));
                }
                appeal.setAppealStatus(AppealStatus.PARTIALLYAPPROVED);
                appeal.setPointsWaived(waived);
                penalty.setPoints(penalty.getPoints() - waived);
                penalty.setStatus(PenaltyStatus.REDUCED);
            }
            case REJECTED -> {
                appeal.setAppealStatus(AppealStatus.REJECTED);
                penalty.setStatus(PenaltyStatus.APPROVED); // revert to approved
            }
        }

        // Recalculate total
        int newTotal = penaltyRepository.sumPointsByStudentAndStatus(
                penalty.getStudent().getUserId(), PenaltyStatus.APPROVED);
        if (penalty.getStatus() == PenaltyStatus.APPROVED
                || penalty.getStatus() == PenaltyStatus.REDUCED) {
            newTotal += penalty.getPoints(); // include this one
        }
        appeal.setNewTotalPoints(newTotal);
        penalty.setTotalPointsAfter(newTotal);
        penalty.setStatusLevel(calculateLevel(newTotal));

        appealRepository.save(appeal);
        penaltyRepository.save(penalty);

        log.info("[APPEAL] Penalty {} appeal decided: {} by {}",
                penaltyId, dto.getDecision(), decidedById);
        return mapToResponse(penalty);
    }

    // ═══════════════════════════════════════════════════════════
    //  10. GET PENDING APPEALS FOR DEPARTMENT
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<PenaltyAppeal> getPendingAppeals(UUID departmentId) {
        return appealRepository.findByDepartmentAndStatus(departmentId, AppealStatus.PENDING);
    }

    // ═══════════════════════════════════════════════════════════
    //  11. CHECK IF STUDENT CAN BORROW
    // ═══════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public boolean canStudentBorrow(UUID studentId) {
        int points = penaltyRepository.sumPointsByStudentAndStatus(
                studentId, PenaltyStatus.APPROVED);
        return points < RED_THRESHOLD;
    }

    // ───────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ───────────────────────────────────────────────────────────
    private String calculateLevel(int points) {
        if (points >= RED_THRESHOLD)    return "RED";
        if (points >= ORANGE_THRESHOLD) return "ORANGE";
        if (points >= YELLOW_THRESHOLD) return "YELLOW";
        return "GREEN";
    }

    private Penalty findPenaltyOrThrow(Integer penaltyId) {
        return penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty", "id", penaltyId));
    }

    private PenaltyResponseDTO mapToResponse(Penalty p) {
        User approver = p.getApprovedById() != null
                ? userRepository.findById(p.getApprovedById()).orElse(null) : null;

        return PenaltyResponseDTO.builder()
                .penaltyId(p.getPenaltyId())
                .requestId(p.getRequest().getRequestId())
                .studentId(p.getStudent().getUserId())
                .studentName(p.getStudent().getFirstName() + " " + p.getStudent().getLastName())
                .studentIndexNumber(p.getStudent().getIndexNumber())
                .penaltyType(p.getPenaltyType())
                .points(p.getPoints())
                .reason(p.getReason())
                .calculationDetailsJson(p.getCalculationDetailsJson())
                .status(p.getStatus())
                .approvedById(p.getApprovedById())
                .approvedByName(approver != null
                        ? approver.getFirstName() + " " + approver.getLastName() : null)
                .approvedAt(p.getApprovedAt())
                .totalPointsAfter(p.getTotalPointsAfter())
                .statusLevel(p.getStatusLevel())
                .appealed(p.getAppealed())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
