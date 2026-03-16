package com.equiphub.api.repository;

import com.equiphub.api.model.EquipmentConditionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentConditionHistoryRepository extends JpaRepository<EquipmentConditionHistory, Integer> {
}