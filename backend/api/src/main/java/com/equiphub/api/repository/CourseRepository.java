package com.equiphub.api.repository;

import com.equiphub.api.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    boolean existsByCourseCode(String courseCode);
    Optional<Course> findByCourseCode(String courseCode);
}
