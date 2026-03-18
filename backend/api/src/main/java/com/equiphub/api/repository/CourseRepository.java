package com.equiphub.api.repository;

import com.equiphub.api.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    boolean existsByCourseCode(String courseCode);

    Optional<Course> findByCourseCode(String courseCode);

    /** All courses belonging to a department — used for the COURSEWORK dropdown. */
    @Query("SELECT c FROM Course c JOIN FETCH c.department d WHERE d.departmentId = :departmentId ORDER BY c.semesterOffered, c.courseCode")
    List<Course> findByDepartmentDepartmentId(@Param("departmentId") UUID departmentId);

    /** Guard against duplicate course codes during update (ignore self). */
    boolean existsByCourseCodeAndCourseIdNot(String courseCode, String courseId);
}
