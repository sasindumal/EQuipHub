package com.equiphub.api.repository;

import com.equiphub.api.model.PenaltyAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyAppealRepository extends JpaRepository<PenaltyAppeal, Integer> {
    
}
