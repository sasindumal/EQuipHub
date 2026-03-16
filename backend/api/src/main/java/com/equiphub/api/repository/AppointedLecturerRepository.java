package com.equiphub.api.repository;

import com.equiphub.api.model.AppointedLecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointedLecturerRepository extends JpaRepository<AppointedLecturer, Integer> {
}
