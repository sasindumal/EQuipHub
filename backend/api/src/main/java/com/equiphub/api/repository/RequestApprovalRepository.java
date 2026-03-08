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

    List<RequestApproval> findByRequestRequestIdOrderByDecidedAtAsc(String requestId);

    Optional<RequestApproval> findByRequestRequestIdAndApprovalStage(
            String requestId, RequestApproval.ApprovalStage stage);

    List<RequestApproval> findByActorId(UUID actorId);

    @Query("SELECT ra FROM RequestApproval ra WHERE ra.actorId = :actorId " +
           "AND ra.decision = 'PENDING'")
    List<RequestApproval> findPendingByActor(@Param("actorId") UUID actorId);

    boolean existsByRequestRequestIdAndApprovalStageAndDecisionNot(
            String requestId,
            RequestApproval.ApprovalStage stage,
            RequestApproval.ApprovalDecision excludeDecision);

    long countByRequestRequestId(String requestId);
}
