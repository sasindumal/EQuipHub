package com.equiphub.api.repository;

import com.equiphub.api.model.DepartmentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentConfigurationRepository extends JpaRepository<DepartmentConfiguration, Integer> {
}