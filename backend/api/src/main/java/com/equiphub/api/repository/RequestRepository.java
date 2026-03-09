package com.equiphub.api.repository;

import com.equiphub.api.model.Request;
import com.equiphub.api.model.Request.RequestStatus;
import com.equiphub.api.model.Request.RequestType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, String> {

    // ── Find by student ────────────────────────────────────
    Page<Request> findByStudentUserId(UUID studentId, Pageable pageable);

    List<Request> findByStudentUserIdAndStatus(UUID studentId, Request.RequestStatus status);

    // ── Find by department ─────────────────────────────────
    Page<Request> findByDepartmentDepartmentId(UUID departmentId, Pageable pageable);

    List<Request> findByDepartmentDepartmentIdAndStatus(UUID departmentId, Request.RequestStatus status);

    // ── Find by status ─────────────────────────────────────
    Page<Request> findByStatus(Request.RequestStatus status, Pageable pageable);

    List<Request> findByStatusIn(List<Request.RequestStatus> statuses);

    // ── Find by request type ───────────────────────────────
    Page<Request> findByRequestType(Request.RequestType type, Pageable pageable);

    Page<Request> findByDepartmentDepartmentIdAndRequestType(
            UUID departmentId, Request.RequestType type, Pageable pageable);

    // ── Complex queries ────────────────────────────────────
    @Query("SELECT r FROM Request r WHERE r.department.departmentId = :deptId " +
           "AND r.status = :status AND r.requestType = :type")
    Page<Request> findByDepartmentStatusAndType(
            @Param("deptId") UUID departmentId,
            @Param("status") Request.RequestStatus status,
            @Param("type") Request.RequestType type,
            Pageable pageable);

    @Query("SELECT r FROM Request r WHERE r.department.departmentId = :deptId " +
           "AND r.fromDateTime >= :from AND r.toDateTime <= :to")
    List<Request> findByDepartmentAndDateRange(
            @Param("deptId") UUID departmentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT r FROM Request r WHERE r.student.userId = :studentId " +
           "AND r.status IN :statuses ORDER BY r.createdAt DESC")
    List<Request> findActiveByStudent(
            @Param("studentId") UUID studentId,
            @Param("statuses") List<Request.RequestStatus> statuses);

    // ── Emergency requests ─────────────────────────────────
    List<Request> findByEmergencyTrueAndStatus(Request.RequestStatus status);

    @Query("SELECT r FROM Request r WHERE r.department.departmentId = :deptId " +
           "AND r.emergency = true AND r.status IN :statuses")
    List<Request> findEmergencyByDepartment(
            @Param("deptId") UUID departmentId,
            @Param("statuses") List<Request.RequestStatus> statuses);

    // ── SLA tracking ───────────────────────────────────────
    @Query("SELECT r FROM Request r WHERE r.status IN :statuses " +
           "AND r.submittedAt IS NOT NULL " +
           "AND FUNCTION('TIMESTAMPADD', HOUR, r.slaHours, r.submittedAt) < :now")
    List<Request> findSlaBreachedRequests(
            @Param("statuses") List<Request.RequestStatus> statuses,
            @Param("now") LocalDateTime now);

    // ── Count queries for dashboard ────────────────────────
    long countByDepartmentDepartmentIdAndStatus(UUID departmentId, Request.RequestStatus status);

    long countByStudentUserIdAndStatus(UUID studentId, Request.RequestStatus status);

    long countByDepartmentDepartmentId(UUID departmentId);

    // ── Request ID generation ──────────────────────────────
    @Query("SELECT r.requestId FROM Request r WHERE r.requestId LIKE :prefix% " +
           "ORDER BY r.requestId DESC LIMIT 1")
    Optional<String> findLastRequestIdByPrefix(@Param("prefix") String prefix);

    // ── Overlap detection for lab sessions ─────────────────
    @Query("SELECT r FROM Request r JOIN RequestItem ri ON ri.request = r " +
           "WHERE ri.equipment.equipmentId = :equipmentId " +
           "AND r.status IN :activeStatuses " +
           "AND r.fromDateTime < :toDateTime " +
           "AND r.toDateTime > :fromDateTime")
    List<Request> findOverlappingRequests(
            @Param("equipmentId") UUID equipmentId,
            @Param("activeStatuses") List<Request.RequestStatus> activeStatuses,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime);

    @Query("SELECT r "+
            "FROM Request r "+
            "WHERE r.status = :requestStatus "+
            "AND r.requestType = :requestType"
            )
            List<Request> findByStatusAndType(
                @Param("requestStatus") RequestStatus status, 
                @Param("requestType") RequestType requestType);

    @Query("SELECT COUNT(r) FROM Request r " +
       "WHERE r.department.departmentId = :deptId " +
       "AND r.submittedAt IS NOT NULL " +
       "AND r.status IN :pendingStatuses")
    long countSlaBreachedByDepartment(
            @Param("deptId") UUID deptId,
            @Param("pendingStatuses") List<Request.RequestStatus> pendingStatuses);
            
    @Query("SELECT COUNT(ri) FROM RequestItem ri " +
            "JOIN ri.request r " +
            "WHERE r.student.userId = :studentId " +
            "AND r.status = 'APPROVED' " +
            "AND r.submittedAt >= :semesterStart")
        long countApprovedItemsByStudentThisSemester(
                @Param("studentId") UUID studentId,
                @Param("semesterStart") LocalDateTime semesterStart);


    }

    
