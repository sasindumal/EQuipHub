package com.equiphub.api.controller;

import com.equiphub.api.dto.CourseCreateRequestDTO;
import com.equiphub.api.model.Course;
import com.equiphub.api.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CourseCreateRequestDTO dto) {
        Course created = courseService.createCourse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
