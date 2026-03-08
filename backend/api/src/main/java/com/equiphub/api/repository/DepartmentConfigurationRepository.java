package com.equiphub.api.repository;

import com.equiphub.api.model.DepartmentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentConfigurationRepository extends JpaRepository<DepartmentConfiguration, Integer> {

    Optional<DepartmentConfiguration> findByDepartmentDepartmentId(UUID departmentId);

    boolean existsByDepartmentDepartmentId(UUID departmentId);

    List<DepartmentConfiguration> findAllByOrderByUpdatedAtDesc();
}
