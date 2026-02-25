package com.equiphub.api.repository;

import com.equiphub.api.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> { // ✅ String, not UUID

       // Department-based queries — UUID because Department.departmentId is UUID
       List<Equipment> findByDepartment_DepartmentId(UUID departmentId);

       List<Equipment> findByDepartment_DepartmentIdAndStatus(
              UUID departmentId,
              Equipment.EquipmentStatus status
       );

       // Status queries
       List<Equipment> findByStatus(Equipment.EquipmentStatus status);

       List<Equipment> findByStatusAndRetiredFalse(Equipment.EquipmentStatus status);

       // Serial number
       Optional<Equipment> findBySerialNumber(String serialNumber);
       boolean existsBySerialNumber(String serialNumber);

       // Custom queries — UUID for department, String for equipment ID
       @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
              "AND e.retired = false ORDER BY e.name ASC")
       List<Equipment> findActiveByDepartmentId(@Param("deptId") UUID departmentId);

       @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
              "AND e.status = 'AVAILABLE' AND e.retired = false")
       List<Equipment> findAvailableByDepartmentId(@Param("deptId") UUID departmentId);

       @Query("SELECT e FROM Equipment e WHERE e.department.departmentId = :deptId " +
              "AND e.status = :status AND e.retired = false")
       List<Equipment> findByDepartmentIdAndStatus(
              @Param("deptId") UUID departmentId,
              @Param("status") Equipment.EquipmentStatus status
       );
}
