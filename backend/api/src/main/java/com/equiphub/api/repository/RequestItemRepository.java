package com.equiphub.api.repository;

import com.equiphub.api.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestItemRepository extends JpaRepository<RequestItem, Integer> {
}
