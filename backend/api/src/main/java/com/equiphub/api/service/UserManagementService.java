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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── CREATE STAFF ─────────────────────────────────────────────────────────

    @Transactional
    public UserResponse createStaff(CreateStaffRequest req, UUID createdBy) {

        // Check duplicate email
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }

        // Validate role is not STUDENT (students self-register)
        User.Role role = req.getRole();
        if (role == User.Role.STUDENT) {
            throw new RuntimeException("Students must self-register through /auth/register");
        }

        // Resolve department
        Department department = null;
        if (req.getDepartmentId() != null && !req.getDepartmentId().isEmpty()) {
            UUID deptId = UUID.fromString(req.getDepartmentId());
            department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
        }

        // Validate: non-SYSTEMADMIN roles must have a department
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
                .emailVerified(true) // Admin-created staff don't need email verification
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);
        log.info("Staff created: {} ({}) by admin {}", saved.getEmail(), saved.getRole(), createdBy);
        return mapToResponse(saved);
    }

    // ─── GET ALL USERS ────────────────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != User.Role.STUDENT)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByDepartment(UUID departmentId) {
        return userRepository.findByDepartmentDepartmentId(departmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByRole(String role) {
        User.Role userRole = User.Role.valueOf(role);
        return userRepository.findByRole(userRole)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return mapToResponse(user);
    }

    // ─── UPDATE USER ──────────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest req, UUID updatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)  user.setLastName(req.getLastName());
        if (req.getPhone() != null)     user.setPhone(req.getPhone());

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

    // ─── SUSPEND / ACTIVATE ───────────────────────────────────────────────────

    @Transactional
    public UserResponse suspendUser(UUID userId, UUID suspendedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setStatus(User.Status.SUSPENDED);
        log.info("User {} suspended by {}", userId, suspendedBy);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse activateUser(UUID userId, UUID activatedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setStatus(User.Status.ACTIVE);
        log.info("User {} activated by {}", userId, activatedBy);
        return mapToResponse(userRepository.save(user));
    }

    // ─── RESET PASSWORD (Admin) ───────────────────────────────────────────────

    @Transactional
    public void resetPassword(UUID userId, String newPassword, UUID resetBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for user {} by admin {}", userId, resetBy);
    }

    // ─── MAPPER ───────────────────────────────────────────────────────────────

    public UserResponse mapToResponse(User user) {
        String deptName = null;
        String deptCode = null;
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
