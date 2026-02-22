package com.equiphub.api.repository;

import com.equiphub.api.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    boolean existsByCode(String code);
    boolean existsByName(String name);
    Optional<Department> findByCode(String code);
    List<Department> findAllByIsActiveTrue();
    Optional<Department> findByDepartmentIdAndIsActiveTrue(UUID departmentId);
}
