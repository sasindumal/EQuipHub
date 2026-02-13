package com.equiphub.api.repository;

import com.equiphub.api.model.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Integer> {
}
