package com.equiphub.api.service;

import com.equiphub.api.dto.CourseCreateRequestDTO;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.model.Course;
import com.equiphub.api.model.Department;
import com.equiphub.api.repository.CourseRepository;
import com.equiphub.api.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public Course createCourse(CourseCreateRequestDTO dto) {

        if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException(
                "Course code already exists: " + dto.getCourseCode());
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Department not found: " + dto.getDepartmentId()));

        Course course = Course.builder()
            .courseId(dto.getCourseId())
            .courseCode(dto.getCourseCode())
            .courseName(dto.getCourseName())
            .department(department)
            .semesterOffered(dto.getSemesterOffered())
            .credits(dto.getCredits())
            .labRequired(dto.getLabRequired() != null ? dto.getLabRequired() : false)
            .build();

        return courseRepository.save(course);
    }
}
