package com.equiphub.api.repository;

import com.equiphub.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIndexNumber(String indexNumber);

    // Find users by role
    List<User> findByRole(User.Role role);

    // Find users by department
    List<User> findByDepartment_DepartmentId(UUID departmentId);

    // Find users by department and role
    List<User> findByDepartment_DepartmentIdAndRole(UUID departmentId, User.Role role);
    List<User> findByDepartment_DepartmentIdAndIsActive(UUID departmentId, boolean isActive);

    // Find all staff in a department (non-student)
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
           "AND u.role != 'STUDENT' AND u.deletedAt IS NULL")
    List<User> findStaffByDepartmentId(@Param("deptId") UUID departmentId);

    // Find active users by role
    List<User> findByRoleAndStatus(User.Role role, User.Status status);

    // Find HOD of a department
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
           "AND u.role = 'HEADOFDEPARTMENT' AND u.status = 'ACTIVE'")
    Optional<User> findHodByDepartmentId(@Param("deptId") UUID departmentId);

    // Search users
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND u.deletedAt IS NULL")
    List<User> searchUsers(@Param("keyword") String keyword);

    // ── Combined queries ─────────────────────────────────────────────────────
    @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
           "AND u.role = :role AND u.isActive = true")
    List<User> findActiveByDepartmentAndRole(
            @Param("deptId") UUID departmentId,
            @Param("role") User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.departmentId = :deptId " +
           "AND u.role = 'STAFF' AND u.isActive = true")
    long countActiveStaffByDepartment(@Param("deptId") UUID departmentId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.department.departmentId = :deptId " +
           "AND u.role = 'STUDENT' AND u.isActive = true")
    long countActiveStudentsByDepartment(@Param("deptId") UUID departmentId);
}
