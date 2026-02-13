package com.equiphub.api.repository;

import com.equiphub.api.model.Activity; 
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {
}