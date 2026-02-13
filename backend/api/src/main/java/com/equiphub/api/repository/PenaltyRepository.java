package com.equiphub.api.repository;

import com.equiphub.api.model.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyRepository extends JpaRepository<Penalty, Integer> {
    
}
