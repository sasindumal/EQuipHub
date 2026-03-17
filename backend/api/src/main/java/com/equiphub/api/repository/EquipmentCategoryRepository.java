package com.equiphub.api.repository;

import com.equiphub.api.model.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Integer> {

    /**
     * Find a category by its name, case-insensitive.
     * Used when the frontend sends a category name string instead of a numeric ID.
     */
    Optional<EquipmentCategory> findByNameIgnoreCase(String name);
}
