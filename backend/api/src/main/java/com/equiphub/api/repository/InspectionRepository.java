package com.equiphub.api.repository;

import com.equiphub.api.model.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Integer> {

    // ── By request item ─────────────────────────────────────────
    List<Inspection> findByRequestItemRequestItemIdOrderByInspectedAtDesc(Integer requestItemId);

    // ── By type for a request item ──────────────────────────────
    Optional<Inspection> findByRequestItemRequestItemIdAndInspectionType(
            Integer requestItemId, com.equiphub.api.model.InspectionType type);

    // ── All inspections by inspector ────────────────────────────
    List<Inspection> findByInspectorIdOrderByInspectedAtDesc(UUID inspectorId);

    // ── All inspections for a request (via items) ───────────────
    @Query("SELECT i FROM Inspection i " +
           "JOIN i.requestItem ri " +
           "WHERE ri.request.requestId = :requestId " +
           "ORDER BY i.inspectedAt DESC")
    List<Inspection> findByRequestId(@Param("requestId") String requestId);

    // ── Inspections with damage ─────────────────────────────────
    @Query("SELECT i FROM Inspection i " +
           "WHERE i.damageLevel IS NOT NULL AND i.damageLevel > 0 " +
           "AND i.inspectedAt >= :since " +
           "ORDER BY i.damageLevel DESC, i.inspectedAt DESC")
    List<Inspection> findDamagedSince(@Param("since") LocalDateTime since);

    // ── Inspections where penalty was triggered ─────────────────
    @Query("SELECT i FROM Inspection i " +
           "WHERE i.penaltyApplicable = true " +
           "AND i.inspectedAt >= :since " +
           "ORDER BY i.inspectedAt DESC")
    List<Inspection> findPenaltyApplicableSince(@Param("since") LocalDateTime since);

    // ── Inspections by department (via equipment) ───────────────
    @Query("SELECT i FROM Inspection i " +
           "JOIN i.requestItem ri " +
           "JOIN ri.equipment e " +
           "WHERE e.department.departmentId = :departmentId " +
           "ORDER BY i.inspectedAt DESC")
    List<Inspection> findByDepartment(@Param("departmentId") UUID departmentId);

    // ── Count by inspection type for stats ──────────────────────
    @Query("SELECT i.inspectionType, COUNT(i) FROM Inspection i " +
           "JOIN i.requestItem ri " +
           "JOIN ri.equipment e " +
           "WHERE e.department.departmentId = :departmentId " +
           "GROUP BY i.inspectionType")
    List<Object[]> countByTypeForDepartment(@Param("departmentId") UUID departmentId);

    // ── Average condition scores for stats ──────────────────────
    @Query("SELECT AVG(i.conditionBefore), AVG(i.conditionAfter) FROM Inspection i " +
           "JOIN i.requestItem ri " +
           "JOIN ri.equipment e " +
           "WHERE e.department.departmentId = :departmentId " +
           "AND i.conditionAfter IS NOT NULL")
    Object[] avgConditionScoresForDepartment(@Param("departmentId") UUID departmentId);

    // ── Unacknowledged inspections (student didn't sign) ────────
@Query("SELECT i FROM Inspection i WHERE i.inspectionType = :type " +
       "AND i.damageLevel IS NOT NULL " +
       "AND i.studentAcknowledged = false")
List<Inspection> findUnacknowledgedDamage(@Param("type") com.equiphub.api.model.InspectionType type);


    // ── Check if pre-issuance inspection exists ─────────────────
    boolean existsByRequestItemRequestItemIdAndInspectionType(
            Integer requestItemId,com.equiphub.api.model.InspectionType type);

    // ── Count damage inspections by level ───────────────────────
    @Query("SELECT i.damageLevel, COUNT(i) FROM Inspection i " +
           "JOIN i.requestItem ri " +
           "JOIN ri.equipment e " +
           "WHERE e.department.departmentId = :departmentId " +
           "AND i.damageLevel IS NOT NULL AND i.damageLevel > 0 " +
           "GROUP BY i.damageLevel")
    List<Object[]> countByDamageLevelForDepartment(@Param("departmentId") UUID departmentId);
}
