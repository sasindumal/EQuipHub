package com.equiphub.api.service;

import com.equiphub.api.dto.CourseCreateRequestDTO;
import com.equiphub.api.dto.CourseResponseDTO;
import com.equiphub.api.dto.CourseUpdateRequestDTO;
import com.equiphub.api.exception.ResourceNotFoundException;
import com.equiphub.api.model.Course;
import com.equiphub.api.model.Department;
import com.equiphub.api.repository.CourseRepository;
import com.equiphub.api.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    // ─────────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public CourseResponseDTO createCourse(CourseCreateRequestDTO dto) {

        if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException(
                    "Course code already exists: " + dto.getCourseCode());
        }

        if (courseRepository.existsById(dto.getCourseId())) {
            throw new IllegalArgumentException(
                    "Course ID already exists: " + dto.getCourseId());
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "departmentId", dto.getDepartmentId()));

        Course course = Course.builder()
                .courseId(dto.getCourseId())
                .courseCode(dto.getCourseCode())
                .courseName(dto.getCourseName())
                .department(department)
                .semesterOffered(dto.getSemesterOffered())
                .credits(dto.getCredits())
                .labRequired(dto.getLabRequired() != null ? dto.getLabRequired() : false)
                .build();

        return toDTO(courseRepository.save(course));
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — single
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public CourseResponseDTO getCourse(String courseId) {
        return toDTO(findCourse(courseId));
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — all
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — by department (for COURSEWORK dropdown)
    // ─────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getCoursesByDepartment(UUID departmentId) {
        return courseRepository.findByDepartmentDepartmentId(departmentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public CourseResponseDTO updateCourse(String courseId, CourseUpdateRequestDTO dto) {
        Course course = findCourse(courseId);

        if (dto.getCourseCode() != null) {
            if (courseRepository.existsByCourseCodeAndCourseIdNot(dto.getCourseCode(), courseId)) {
                throw new IllegalArgumentException(
                        "Course code already in use: " + dto.getCourseCode());
            }
            course.setCourseCode(dto.getCourseCode());
        }
        if (dto.getCourseName()      != null) course.setCourseName(dto.getCourseName());
        if (dto.getSemesterOffered() != null) course.setSemesterOffered(dto.getSemesterOffered());
        if (dto.getCredits()         != null) course.setCredits(dto.getCredits());
        if (dto.getLabRequired()     != null) course.setLabRequired(dto.getLabRequired());

        return toDTO(courseRepository.save(course));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteCourse(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", "courseId", courseId);
        }
        courseRepository.deleteById(courseId);
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────
    private Course findCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "courseId", courseId));
    }

    private CourseResponseDTO toDTO(Course c) {
        return CourseResponseDTO.builder()
                .courseId(c.getCourseId())
                .courseCode(c.getCourseCode())
                .courseName(c.getCourseName())
                .departmentId(c.getDepartment() != null ? c.getDepartment().getDepartmentId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .departmentCode(c.getDepartment() != null ? c.getDepartment().getCode() : null)
                .semesterOffered(c.getSemesterOffered())
                .credits(c.getCredits())
                .labRequired(c.getLabRequired())
                .build();
    }
}
