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

    // ── Core lookups ────────────────────────────────────────────
    List<RequestApproval> findByRequestRequestIdOrderByDecidedAtAsc(String requestId);

    Optional<RequestApproval> findByRequestRequestIdAndApprovalStage(
            String requestId, RequestApproval.ApprovalStage stage);

    List<RequestApproval> findByActorId(UUID actorId);

    // ── Pending approvals for a specific actor ──────────────────
    @Query("SELECT ra FROM RequestApproval ra WHERE ra.actorId = :actorId " +
           "AND ra.decision = 'PENDING'")
    List<RequestApproval> findPendingByActor(@Param("actorId") UUID actorId);

    // ── Pending approvals by actor role ─────────────────────────
       @Query("SELECT ra FROM RequestApproval ra " +
              "WHERE ra.actorRole = :role " +
              "AND ra.request.department.id = :departmentId " +
              "AND ra.decision = :decision")
       List<RequestApproval> findPendingByRoleAndDepartment(
       @Param("role") String role,
       @Param("departmentId") UUID departmentId,
       @Param("decision") com.equiphub.api.model.ApprovalDecision decision
       );

    // ── Check if stage already decided ──────────────────────────
    boolean existsByRequestRequestIdAndApprovalStageAndDecisionNot(
            String requestId,
            RequestApproval.ApprovalStage stage,
            com.equiphub.api.model.ApprovalDecision excludeDecision);

    long countByRequestRequestId(String requestId);

    // ── Pending approvals for a department ──────────────────────
    @Query("SELECT ra FROM RequestApproval ra " +
           "JOIN ra.request r " +
           "WHERE ra.decision = com.equiphub.api.model.RequestApproval.ApprovalDecision.PENDING " +
           "AND r.department.departmentId = :departmentId " +
           "ORDER BY r.emergency DESC, r.priorityLevel ASC, r.submittedAt ASC")
    List<RequestApproval> findPendingByDepartment(@Param("departmentId") UUID departmentId);

    // ── Count pending by stage for stats ────────────────────────
    @Query("SELECT ra.approvalStage, COUNT(ra) FROM RequestApproval ra " +
           "WHERE ra.decision = com.equiphub.api.model.RequestApproval.ApprovalDecision.PENDING " +
           "AND ra.request.department.departmentId = :departmentId " +
           "GROUP BY ra.approvalStage")
    List<Object[]> countPendingByStageForDepartment(@Param("departmentId") UUID departmentId);

    // ── Count by decision for stats ─────────────────────────────
    @Query("SELECT ra.decision, COUNT(ra) FROM RequestApproval ra " +
           "WHERE ra.request.department.departmentId = :departmentId " +
           "GROUP BY ra.decision")
    List<Object[]> countByDecisionForDepartment(@Param("departmentId") UUID departmentId);

    // ── Latest approval for a request ───────────────────────────
    @Query("SELECT ra FROM RequestApproval ra " +
           "WHERE ra.request.requestId = :requestId " +
           "ORDER BY ra.decidedAt DESC")
    List<RequestApproval> findLatestByRequestId(@Param("requestId") String requestId);

    // ── Check if actor already acted on this request ────────────
    boolean existsByRequestRequestIdAndActorIdAndDecisionNot(
            String requestId, UUID actorId,
            com.equiphub.api.model.ApprovalDecision excludeDecision);
}
