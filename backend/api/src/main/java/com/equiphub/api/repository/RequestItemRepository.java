package com.equiphub.api.repository;

import com.equiphub.api.model.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Integer> {

    /**
     * Eagerly fetches the associated Equipment in a single JOIN query.
     * This prevents LazyInitializationException when the session is closed
     * before mapItemToResponse() accesses equipment.getName() / getSerialNumber().
     */
    @Query("SELECT ri FROM RequestItem ri JOIN FETCH ri.equipment WHERE ri.request.requestId = :requestId")
    List<RequestItem> findByRequestRequestId(@Param("requestId") String requestId);

    @Query("SELECT ri FROM RequestItem ri JOIN FETCH ri.equipment " +
           "WHERE ri.request.requestId = :requestId AND ri.status = :status")
    List<RequestItem> findByRequestRequestIdAndStatus(
            @Param("requestId") String requestId,
            @Param("status") RequestItem.ItemStatus status);

    List<RequestItem> findByEquipmentEquipmentId(UUID equipmentId);

    @Query("SELECT ri FROM RequestItem ri JOIN FETCH ri.equipment " +
           "WHERE ri.equipment.equipmentId = :equipmentId " +
           "AND ri.status IN ('PENDING', 'APPROVED', 'ISSUED')")
    List<RequestItem> findActiveByEquipment(@Param("equipmentId") UUID equipmentId);

    void deleteByRequestRequestId(String requestId);

    @Query("SELECT ri FROM RequestItem ri JOIN FETCH ri.equipment " +
           "WHERE ri.request.requestId = :requestId ORDER BY ri.requestItemId ASC")
    List<RequestItem> findByRequestRequestIdOrderByRequestItemIdAsc(@Param("requestId") String requestId);
}
