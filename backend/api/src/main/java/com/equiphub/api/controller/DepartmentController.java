package com.equiphub.api.controller;

import com.equiphub.api.dto.department.CreateDepartmentRequest;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Department Management", description = "SYSTEMADMIN: Manage departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(summary = "Create department", description = "Create a new department (CSE, EEE, etc.)")
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request,
            Authentication authentication) {
        try {
            CustomUserDetails admin = (CustomUserDetails) authentication.getPrincipal();
            DepartmentResponse response = departmentService.createDepartment(request, admin.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active departments", description = "Public list of active departments")
    public ResponseEntity<List<DepartmentResponse>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAnyRole('SYSTEMADMIN', 'DEPARTMENTADMIN', 'HEADOFDEPARTMENT')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<?> getDepartmentById(@PathVariable UUID departmentId) {
        try {
            return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(summary = "Update department", description = "Update details or assign HOD/Admin")
    public ResponseEntity<?> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request,
            Authentication authentication) {
        try {
            CustomUserDetails admin = (CustomUserDetails) authentication.getPrincipal();
            DepartmentResponse response = departmentService.updateDepartment(departmentId, request, admin.getUserId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('SYSTEMADMIN')")
    @Operation(summary = "Deactivate department", description = "Soft-delete. Fails if users exist in department.")
    public ResponseEntity<?> deactivateDepartment(
            @PathVariable UUID departmentId,
            Authentication authentication) {
        try {
            CustomUserDetails admin = (CustomUserDetails) authentication.getPrincipal();
            departmentService.deactivateDepartment(departmentId, admin.getUserId());
            return ResponseEntity.ok(Map.of("message", "Department deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
