package com.equiphub.api.repository;

import com.equiphub.api.model.RequestApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestApprovalRepository extends JpaRepository<RequestApproval, Integer> {

    // ── Core lookups ──────────────────────────────────────────────────────────

    List<RequestApproval> findByRequestRequestIdOrderByDecidedAtAsc(String requestId);

    Optional<RequestApproval> findByRequestRequestIdAndApprovalStage(
            String requestId, RequestApproval.ApprovalStage stage);

    List<RequestApproval> findByActorId(UUID actorId);

    // ── Pending approvals for a specific actor ────────────────────────────────
    // BUG 1 FIX: 'PENDING' string literal → use derived query with enum param
    List<RequestApproval> findByActorIdAndDecision(
            UUID actorId,
            RequestApproval.ApprovalDecision decision);

    // ── Pending approvals by actor role ──────────────────────────────────────
    // BUG 2 FIX: com.equiphub.api.model.ApprovalDecision does NOT exist —
    //            ApprovalDecision is a NESTED enum inside RequestApproval.
    //            Also fixed: department.id → department.departmentId
    @Query("SELECT ra FROM RequestApproval ra " +
           "WHERE ra.actorRole = :role " +
           "AND ra.request.department.departmentId = :departmentId " +
           "AND ra.decision = :decision")
    List<RequestApproval> findPendingByRoleAndDepartment(
            @Param("role") String role,
            @Param("departmentId") UUID departmentId,
            @Param("decision") RequestApproval.ApprovalDecision decision);

    // ── Check if stage already decided ───────────────────────────────────────
    // BUG 3 FIX: same wrong class — must be RequestApproval.ApprovalDecision
    boolean existsByRequestRequestIdAndApprovalStageAndDecisionNot(
            String requestId,
            RequestApproval.ApprovalStage stage,
            RequestApproval.ApprovalDecision excludeDecision);

    long countByRequestRequestId(String requestId);

    // ── Pending approvals for a department ───────────────────────────────────
    // BUG 4 FIX: inline enum path in JPQL → bind as :decision param
    //            Also fixed: wrong class path for ApprovalDecision
    @Query("SELECT ra FROM RequestApproval ra " +
           "JOIN ra.request r " +
           "WHERE ra.decision = :decision " +
           "AND r.department.departmentId = :departmentId " +
           "ORDER BY r.emergency DESC, r.priorityLevel ASC, r.submittedAt ASC")
    List<RequestApproval> findPendingByDepartment(
            @Param("departmentId") UUID departmentId,
            @Param("decision") RequestApproval.ApprovalDecision decision);

    // ── Count pending by stage for stats ─────────────────────────────────────
    // BUG 5 FIX: inline enum path still present despite comment saying "FIXED"
    //            Also fixed: department.id → department.departmentId
    @Query("SELECT ra.approvalStage, COUNT(ra) FROM RequestApproval ra " +
           "WHERE ra.request.department.departmentId = :departmentId " +
           "AND ra.decision = :decision " +
           "GROUP BY ra.approvalStage")
    List<Object[]> countPendingByStageForDepartment(
            @Param("departmentId") UUID departmentId,
            @Param("decision") RequestApproval.ApprovalDecision decision);

    // ── Count by decision for stats ───────────────────────────────────────────
    // BUG 5b FIX: department.id → department.departmentId
    @Query("SELECT ra.decision, COUNT(ra) FROM RequestApproval ra " +
           "WHERE ra.request.department.departmentId = :departmentId " +
           "GROUP BY ra.decision")
    List<Object[]> countByDecisionForDepartment(@Param("departmentId") UUID departmentId);

    // ── Latest approval for a request ────────────────────────────────────────
    @Query("SELECT ra FROM RequestApproval ra " +
           "WHERE ra.request.requestId = :requestId " +
           "ORDER BY ra.decidedAt DESC")
    List<RequestApproval> findLatestByRequestId(@Param("requestId") String requestId);

    // ── Check if actor already acted on this request ─────────────────────────
    // BUG 3b FIX: same wrong class — must be RequestApproval.ApprovalDecision
    boolean existsByRequestRequestIdAndActorIdAndDecisionNot(
            String requestId,
            UUID actorId,
            RequestApproval.ApprovalDecision excludeDecision);
}
