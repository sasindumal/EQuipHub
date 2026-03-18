package com.equiphub.api.controller;

import com.equiphub.api.dto.CourseCreateRequestDTO;
import com.equiphub.api.dto.CourseResponseDTO;
import com.equiphub.api.dto.CourseUpdateRequestDTO;
import com.equiphub.api.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Management", description = "CRUD for courses; list endpoints used by COURSEWORK request form")
public class CourseController {

    private final CourseService courseService;

    // ── 1. Create ─────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Create a course",
               description = "Department Admin or System Admin only")
    public ResponseEntity<CourseResponseDTO> createCourse(
            @Valid @RequestBody CourseCreateRequestDTO dto) {
        log.info("[COURSE] Creating course: {}", dto.getCourseCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(dto));
    }

    // ── 2. Get single ──────────────────────────────────────────────────
    @GetMapping("/{courseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a course by ID")
    public ResponseEntity<CourseResponseDTO> getCourse(
            @PathVariable String courseId) {
        return ResponseEntity.ok(courseService.getCourse(courseId));
    }

    // ── 3. Get all ────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all courses",
               description = "Available to all authenticated users")
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // ── 4. Get by department ──────────────────────────────────────────
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List courses for a department",
               description = "Used to populate the course dropdown when COURSEWORK request type is selected")
    public ResponseEntity<List<CourseResponseDTO>> getCoursesByDepartment(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(courseService.getCoursesByDepartment(departmentId));
    }

    // ── 5. Update ─────────────────────────────────────────────────────
    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Update a course",
               description = "Partial update — only provided fields are changed")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CourseUpdateRequestDTO dto) {
        log.info("[COURSE] Updating course: {}", courseId);
        return ResponseEntity.ok(courseService.updateCourse(courseId, dto));
    }

    // ── 6. Delete ─────────────────────────────────────────────────────
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(summary = "Delete a course",
               description = "Permanently removes the course. Existing requests that reference this course are NOT affected (FK is preserved at DB level).")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable String courseId) {
        log.info("[COURSE] Deleting course: {}", courseId);
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}
