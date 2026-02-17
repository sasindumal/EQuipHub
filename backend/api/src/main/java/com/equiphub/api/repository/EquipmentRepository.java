package com.equiphub.api.repository;

import com.equiphub.api.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, String> {
    List<Equipment> findByDepartmentDepartmentId(String departmentId);
    List<Equipment> findByStatus(Equipment.EquipmentStatus status);
}