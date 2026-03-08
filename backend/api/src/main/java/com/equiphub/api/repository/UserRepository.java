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
       List<User> findByDepartmentDepartmentId(UUID departmentId);

       // Find users by department and role
       List<User> findByDepartmentDepartmentIdAndRole(UUID departmentId, User.Role role);

       List<User> findByDepartmentDepartmentIdAndStatus(UUID departmentId, User.Status status);

       // ✅ FIX 1: replaced `!= STUDENT` (unqualified literal) with `<> 'STUDENT'` (string literal)
       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role <> 'STUDENT' AND u.deletedAt IS NULL")
       List<User> findStaffByDepartmentId(@Param("deptId") UUID departmentId);

       // Find active users by role
       List<User> findByRoleAndStatus(User.Role role, User.Status status);

       // ✅ FIX 2: replaced bare `HEADOFDEPARTMENT` and `ACTIVE` literals with quoted string literals
       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'HEADOFDEPARTMENT' AND u.status = 'ACTIVE'")
       Optional<User> findHodByDepartmentId(@Param("deptId") UUID departmentId);

       // Search users
       @Query("SELECT u FROM User u WHERE " +
              "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
              "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
              "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
              "AND u.deletedAt IS NULL")
       List<User> searchUsers(@Param("keyword") String keyword);

       // ✅ FIX 3: replaced `com.equiphub.api.model.User.Status.ACTIVE` (FQN — CRASHES Hibernate 6)
       //           with `'ACTIVE'` string literal. Named param :role kept as-is.
       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = :role AND u.status = 'ACTIVE'")
       List<User> findActiveByDepartmentAndRole(
              @Param("deptId") UUID departmentId,
              @Param("role") User.Role role);

              // Role-specific convenience queries
       @Query("SELECT u FROM User u WHERE u.role = 'SYSTEMADMIN' AND u.deletedAt IS NULL")
       List<User> findSystemAdmins();

       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'DEPARTMENTADMIN' AND u.status = 'ACTIVE'")
       List<User> findDepartmentAdminsByDepartmentId(@Param("deptId") UUID departmentId);

              // Head of Department: `findHodByDepartmentId` already exists and returns Optional<User>

       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'LECTURER' AND u.status = 'ACTIVE'")
       List<User> findLecturersByDepartmentId(@Param("deptId") UUID departmentId);

       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'INSTRUCTOR' AND u.status = 'ACTIVE'")
       List<User> findInstructorsByDepartmentId(@Param("deptId") UUID departmentId);

       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'APPOINTEDLECTURER' AND u.status = 'ACTIVE'")
       List<User> findAppointedLecturersByDepartmentId(@Param("deptId") UUID departmentId);

       @Query("SELECT u FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'TECHNICALOFFICER' AND u.status = 'ACTIVE'")
       List<User> findTechnicalOfficersByDepartmentId(@Param("deptId") UUID departmentId);

       // ✅ FIX 4: count users with staff-like roles (non-students)
       @Query("SELECT COUNT(u) FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role IN ('SYSTEMADMIN','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER') " +
              "AND u.status = 'ACTIVE' AND u.deletedAt IS NULL")
       long countActiveStaffByDepartment(@Param("deptId") UUID departmentId);

       // ✅ FIX 5: THE CRASH CAUSE — replaced `com.equiphub.api.model.User.Status.ACTIVE`
       //           (FQN enum path unsupported in Hibernate 6) with `'ACTIVE'` string literal
       @Query("SELECT COUNT(u) FROM User u WHERE u.department.departmentId = :deptId " +
              "AND u.role = 'STUDENT' AND u.status = 'ACTIVE'")
       long countActiveStudentsByDepartment(@Param("deptId") UUID departmentId);

       @Query("SELECT u FROM User u WHERE u.role <> 'STUDENT' AND u.deletedAt IS NULL")
       List<User> findAllStaff();
}
