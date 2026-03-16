package com.equiphub.api.repository;

import com.equiphub.api.model.Penalty;
import com.equiphub.api.model.Penalty.PenaltyStatus;
import com.equiphub.api.model.Penalty.PenaltyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Integer> {

    List<Penalty> findByStudentUserIdOrderByCreatedAtDesc(UUID studentId);

    List<Penalty> findByStudentUserIdAndStatus(UUID studentId, PenaltyStatus status);

    List<Penalty> findByRequestRequestId(String requestId);

    List<Penalty> findByStatus(PenaltyStatus status);

    @Query("SELECT COALESCE(SUM(p.points), 0) FROM Penalty p " +
           "WHERE p.student.userId = :studentId AND p.status = :status")
    int sumPointsByStudentAndStatus(@Param("studentId") UUID studentId,
                                   @Param("status") PenaltyStatus status);

    @Query("SELECT p FROM Penalty p WHERE p.student.userId = :studentId " +
           "AND p.status IN :statuses ORDER BY p.createdAt DESC")
    List<Penalty> findActiveByStudent(@Param("studentId") UUID studentId,
                                     @Param("statuses") List<PenaltyStatus> statuses);

    @Query("SELECT COUNT(p) FROM Penalty p WHERE p.student.userId = :studentId " +
           "AND p.penaltyType = :type AND p.status != 'WAIVED'")
    long countByStudentAndType(@Param("studentId") UUID studentId,
                               @Param("type") PenaltyType type);

    @Query("SELECT p FROM Penalty p JOIN p.request r " +
           "WHERE r.department.departmentId = :deptId ORDER BY p.createdAt DESC")
    List<Penalty> findByDepartment(@Param("deptId") UUID departmentId);

    @Query("SELECT p FROM Penalty p JOIN p.request r " +
           "WHERE r.department.departmentId = :deptId AND p.status = :status")
    List<Penalty> findByDepartmentAndStatus(@Param("deptId") UUID departmentId,
                                            @Param("status") PenaltyStatus status);

    boolean existsByRequestRequestIdAndPenaltyType(String requestId, PenaltyType type);
}
