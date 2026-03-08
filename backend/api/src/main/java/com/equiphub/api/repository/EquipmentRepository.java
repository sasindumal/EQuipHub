package com.equiphub.api.repository;

import com.equiphub.api.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    // ── Department queries ────────────────────────────────────────
    List<Equipment> findByDepartmentDepartmentId(UUID departmentId);
    List<Equipment> findByDepartmentDepartmentIdAndStatus(UUID departmentId, Equipment.EquipmentStatus status);

    // ── Status queries ────────────────────────────────────────────
    List<Equipment> findByStatus(Equipment.EquipmentStatus status);
    List<Equipment> findByStatusAndRetiredFalse(Equipment.EquipmentStatus status);

    // ── Serial number ─────────────────────────────────────────────
    Optional<Equipment> findBySerialNumber(String serialNumber);
    boolean existsBySerialNumber(String serialNumber);

    // ── Active equipment in a department ─────────────────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.retired = false ORDER BY e.name ASC")
    List<Equipment> findActiveByDepartmentId(@Param("deptId") UUID departmentId);

    // ── Available equipment in a department ──────────────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.status = 'AVAILABLE' AND e.retired = false")
    List<Equipment> findAvailableByDepartmentId(@Param("deptId") UUID departmentId);

    // ── Filter by department + status (non-retired) ───────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.status = :status AND e.retired = false")
    List<Equipment> findByDepartmentIdAndStatus(
            @Param("deptId") UUID departmentId,
            @Param("status") Equipment.EquipmentStatus status);

    // ── Equipment due for maintenance ─────────────────────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.nextMaintenanceDate <= :date AND e.retired = false")
    List<Equipment> findMaintenanceDueByDepartment(
            @Param("deptId") UUID departmentId,
            @Param("date") LocalDate date);

    // ── Search by name or serial number ──────────────────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.retired = false " +
           "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Equipment> searchByDepartment(
            @Param("deptId") UUID departmentId,
            @Param("keyword") String keyword);

    // ── Search globally (SYSTEMADMIN) ─────────────────────────────
    @Query("SELECT e FROM Equipment e WHERE e.retired = false " +
           "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Equipment> searchGlobal(@Param("keyword") String keyword);

    // ── Equipment by type and department ──────────────────────────
    List<Equipment> findByDepartmentDepartmentIdAndTypeAndRetiredFalse(
            UUID departmentId, Equipment.EquipmentType type);

    // ── Counts for dashboard ──────────────────────────────────────
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.status = 'AVAILABLE' AND e.retired = false")
    long countAvailableByDepartment(@Param("deptId") UUID departmentId);

    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.status = 'MAINTENANCE' AND e.retired = false")
    long countMaintenanceByDepartment(@Param("deptId") UUID departmentId);

    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.retired = false")
    long countActiveByDepartment(@Param("deptId") UUID departmentId);

    // ── Low condition alert (condition <= threshold) ───────────────
    @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
           "AND e.currentCondition <= :threshold AND e.retired = false")
    List<Equipment> findLowConditionByDepartment(
            @Param("deptId") UUID departmentId,
            @Param("threshold") int threshold);

    // ── All retired equipment ─────────────────────────────────────
    List<Equipment> findByDepartmentDepartmentIdAndRetiredTrue(UUID departmentId);
}
