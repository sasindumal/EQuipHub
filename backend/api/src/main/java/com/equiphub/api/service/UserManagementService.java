package com.equiphub.api.service;

import com.equiphub.api.dto.user.CreateStaffRequest;
import com.equiphub.api.dto.user.UpdateUserRequest;
import com.equiphub.api.dto.user.UserResponse;
import com.equiphub.api.model.Department;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────
    //  CREATE STAFF
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public UserResponse createStaff(CreateStaffRequest req, UUID createdBy) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }

        User.Role role = req.getRole();
        if (role == User.Role.STUDENT) {
            throw new RuntimeException("Students must self-register through /auth/register");
        }

        Department department = null;
        if (req.getDepartmentId() != null && !req.getDepartmentId().isBlank()) {
            UUID deptId = UUID.fromString(req.getDepartmentId());
            department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
        }

        if (role != User.Role.SYSTEMADMIN && department == null) {
            throw new RuntimeException("Non-SYSTEMADMIN roles require a department assignment");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getTemporaryPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .phone(req.getPhone())
                .role(role)
                .department(department)
                .status(User.Status.ACTIVE)
                .emailVerified(true)
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);
        log.info("Staff created: {} [{}] by {}", saved.getEmail(), saved.getRole(), createdBy);
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllStaff() {
        return userRepository.findAllStaff()        // uses @Query — no full table scan
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT)
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getStaffByDepartment(UUID departmentId) {
        return userRepository.findStaffByDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getStudentsByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentDepartmentIdAndRole(departmentId, User.Role.STUDENT)
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentDepartmentId(departmentId)
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(String role) {
        User.Role userRole = User.Role.valueOf(role);
        return userRepository.findByRole(userRole)
                .stream()
                .filter(u -> u.getDeletedAt() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return mapToResponse(user);
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest req, UUID updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (req.getFirstName() != null)  user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)   user.setLastName(req.getLastName());
        if (req.getPhone() != null)      user.setPhone(req.getPhone());

        if (req.getStatus() != null) {
            user.setStatus(User.Status.valueOf(req.getStatus()));
            log.info("User {} status changed to {} by {}", userId, req.getStatus(), updatedBy);
        }

        if (req.getDepartmentId() != null) {
            UUID deptId = UUID.fromString(req.getDepartmentId());
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
            user.setDepartment(dept);
        }

        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    // ─────────────────────────────────────────────────────────────
    //  SUSPEND / ACTIVATE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public UserResponse suspendUser(UUID userId, UUID suspendedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new RuntimeException("User is already suspended");
        }

        user.setStatus(User.Status.SUSPENDED);
        log.info("User {} suspended by {}", userId, suspendedBy);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(UUID userId, UUID activatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getStatus() == User.Status.ACTIVE) {
            throw new RuntimeException("User is already active");
        }

        user.setStatus(User.Status.ACTIVE);
        log.info("User {} activated by {}", userId, activatedBy);
        return mapToResponse(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────
    //  RESET PASSWORD
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void resetPassword(UUID userId, String newPassword, UUID resetBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for user {} by admin {}", userId, resetBy);
    }

    // ─────────────────────────────────────────────────────────────
    //  SOFT DELETE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteUser(UUID userId, UUID deletedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("User is already deleted");
        }

        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);
        log.warn("User {} soft-deleted by {}", userId, deletedBy);
    }

    // ─────────────────────────────────────────────────────────────
    //  DEPARTMENT STATISTICS
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> getDepartmentStats(UUID departmentId) {
        long totalStaff    = userRepository.countActiveStaffByDepartment(departmentId);
        long totalStudents = userRepository.countActiveStudentsByDepartment(departmentId);

        Map<String, Long> roleBreakdown = new HashMap<>();
        for (User.Role role : User.Role.values()) {
            if (role != User.Role.STUDENT) {
                long count = userRepository.findByDepartmentDepartmentIdAndRole(departmentId, role)
                        .stream().filter(u -> u.getDeletedAt() == null).count();
                if (count > 0) roleBreakdown.put(role.name(), count);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("departmentId", departmentId);
        stats.put("totalStaff", totalStaff);
        stats.put("totalStudents", totalStudents);
        stats.put("totalUsers", totalStaff + totalStudents);
        stats.put("roleBreakdown", roleBreakdown);
        return stats;
    }

    // ─────────────────────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────────────────────
    public UserResponse mapToResponse(User user) {
        String deptName = null, deptCode = null;
        UUID deptId = null;

        if (user.getDepartment() != null) {
            deptId   = user.getDepartment().getDepartmentId();
            deptName = user.getDepartment().getName();
            deptCode = user.getDepartment().getCode();
        }

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .departmentId(deptId)
                .departmentName(deptName)
                .departmentCode(deptCode)
                .emailVerified(user.getEmailVerified())
                .indexNumber(user.getIndexNumber())
                .semesterYear(user.getSemesterYear())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
