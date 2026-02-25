package com.equiphub.api.repository;

import com.equiphub.api.model.DepartmentConfiguration;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentConfigurationRepository extends JpaRepository<DepartmentConfiguration, UUID> {
}