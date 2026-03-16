package com.equiphub.api.repository;

import com.equiphub.api.model.EquipmentUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentUnavailabilityRepository extends JpaRepository<EquipmentUnavailability, Integer> {
}
