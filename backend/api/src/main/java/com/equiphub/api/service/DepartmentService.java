package com.equiphub.api.service;

import com.equiphub.api.dto.department.CreateDepartmentRequest;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.model.Department;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.UserRepository;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest req, UUID createdBy) {

        if (departmentRepository.existsByCode(req.getCode())) {
            throw new RuntimeException("Department code '" + req.getCode() + "' already exists");
        }
        if (departmentRepository.existsByName(req.getName())) {
            throw new RuntimeException("Department name '" + req.getName() + "' already exists");
        }

        Department dept = Department.builder()
                .code(req.getCode().toUpperCase())
                .name(req.getName())
                .description(req.getDescription())
                .isActive(true)
                .createdBy(createdBy)
                .build();

        Department saved = departmentRepository.save(dept);
        log.info("Department created: {} ({}) by {}", saved.getName(), saved.getCode(), createdBy);
        return mapToResponse(saved);
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DepartmentResponse> getActiveDepartments() {
        return departmentRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DepartmentResponse getDepartmentById(UUID departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
        return mapToResponse(dept);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public DepartmentResponse updateDepartment(UUID departmentId, UpdateDepartmentRequest req, UUID updatedBy) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        if (req.getName() != null)        dept.setName(req.getName());
        if (req.getDescription() != null) dept.setDescription(req.getDescription());
        if (req.getIsActive() != null)    dept.setIsActive(req.getIsActive());

        // Assign HOD
        if (req.getHodId() != null) {
            UUID hodId = UUID.fromString(req.getHodId());
            User hod = userRepository.findById(hodId)
                    .orElseThrow(() -> new RuntimeException("HOD user not found: " + hodId));
            if (hod.getRole() != User.Role.HEADOFDEPARTMENT) {
                throw new RuntimeException("User is not a HEAD OF DEPARTMENT");
            }
            dept.setHodId(hodId);
            hod.setDepartment(dept);
            userRepository.save(hod);
        }

        // Assign Department Admin
        if (req.getAdminId() != null) {
            UUID adminId = UUID.fromString(req.getAdminId());
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin user not found: " + adminId));
            if (admin.getRole() != User.Role.DEPARTMENTADMIN) {
                throw new RuntimeException("User is not a DEPARTMENT ADMIN");
            }
            dept.setAdminId(adminId);
            admin.setDepartment(dept);
            userRepository.save(admin);
        }

        Department updated = departmentRepository.save(dept);
        log.info("Department updated: {} by {}", updated.getCode(), updatedBy);
        return mapToResponse(updated);
    }

    // ─── DELETE (Soft) ────────────────────────────────────────────────────────

    @Transactional
    public void deactivateDepartment(UUID departmentId, UUID deactivatedBy) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        // Check no active staff/students
        List<User> activeUsers = userRepository.findByDepartment_DepartmentId(departmentId);
        if (!activeUsers.isEmpty()) {
            throw new RuntimeException(
                "Cannot deactivate department with " + activeUsers.size() + " active users. Reassign them first."
            );
        }

        dept.setIsActive(false);
        departmentRepository.save(dept);
        log.info("Department deactivated: {} by {}", dept.getCode(), deactivatedBy);
    }

    // ─── MAPPER ───────────────────────────────────────────────────────────────

    private DepartmentResponse mapToResponse(Department dept) {
        long totalStaff = userRepository.findByDepartment_DepartmentId(dept.getDepartmentId())
                .stream().filter(u -> u.getRole() != User.Role.STUDENT).count();
        long totalStudents = userRepository.findByDepartment_DepartmentId(dept.getDepartmentId())
                .stream().filter(u -> u.getRole() == User.Role.STUDENT).count();

        String hodName = null;
        String adminName = null;

        if (dept.getHodId() != null) {
            hodName = userRepository.findById(dept.getHodId())
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse(null);
        }
        if (dept.getAdminId() != null) {
            adminName = userRepository.findById(dept.getAdminId())
                    .map(u -> u.getFirstName() + " " + u.getLastName())
                    .orElse(null);
        }

        return DepartmentResponse.builder()
                .departmentId(dept.getDepartmentId())
                .code(dept.getCode())
                .name(dept.getName())
                .description(dept.getDescription())
                .isActive(dept.getIsActive())
                .hodId(dept.getHodId())
                .hodName(hodName)
                .adminId(dept.getAdminId())
                .adminName(adminName)
                .totalStaff(totalStaff)
                .totalStudents(totalStudents)
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
