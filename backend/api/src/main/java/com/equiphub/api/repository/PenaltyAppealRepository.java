package com.equiphub.api.repository;

import com.equiphub.api.model.PenaltyAppeal;
import com.equiphub.api.model.PenaltyAppeal.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PenaltyAppealRepository extends JpaRepository<PenaltyAppeal, Integer> {

    // ── Find appeal by its linked penalty ID ─────────────────────────────
    Optional<PenaltyAppeal> findByPenaltyPenaltyId(Integer penaltyId);

    // ── All appeals submitted by a student ───────────────────────────────
    List<PenaltyAppeal> findByStudentUserIdOrderByAppealedAtDesc(UUID studentId);

    // ── All appeals in a given status (system-wide) ───────────────────────
    List<PenaltyAppeal> findByAppealStatus(AppealStatus status);

    // ── Appeals for a specific department filtered by status ──────────────
    // Navigation: PenaltyAppeal → Penalty → Request → Department
    @Query("""
        SELECT pa FROM PenaltyAppeal pa
        JOIN pa.penalty p
        JOIN p.request r
        WHERE r.department.departmentId = :deptId
        AND pa.appealStatus = :status
        ORDER BY pa.appealedAt DESC
        """)
    List<PenaltyAppeal> findByDepartmentAndStatus(
            @Param("deptId") UUID departmentId,
            @Param("status") AppealStatus status);

    // ── All pending appeals for a department (any status) ─────────────────
    @Query("""
        SELECT pa FROM PenaltyAppeal pa
        JOIN pa.penalty p
        JOIN p.request r
        WHERE r.department.departmentId = :deptId
        ORDER BY pa.appealedAt DESC
        """)
    List<PenaltyAppeal> findAllByDepartment(@Param("deptId") UUID departmentId);

    // ── Expired pending appeals — for scheduled cleanup job ───────────────
    @Query("""
        SELECT pa FROM PenaltyAppeal pa
        WHERE pa.appealStatus = 'PENDING'
        AND pa.appealDeadline < :now
        """)
    List<PenaltyAppeal> findExpiredPendingAppeals(@Param("now") LocalDateTime now);

    // ── Guard: has this penalty already been appealed (active) ───────────
    boolean existsByPenaltyPenaltyIdAndAppealStatusIn(
            Integer penaltyId,
            List<AppealStatus> statuses);

    // ── Count pending appeals per department (for dashboard badge) ─────────
    @Query("""
        SELECT COUNT(pa) FROM PenaltyAppeal pa
        JOIN pa.penalty p
        JOIN p.request r
        WHERE r.department.departmentId = :deptId
        AND pa.appealStatus = 'PENDING'
        """)
    long countPendingByDepartment(@Param("deptId") UUID departmentId);
}
