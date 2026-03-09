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

    List<RequestItem> findByRequestRequestId(String requestId);

    List<RequestItem> findByRequestRequestIdAndStatus(String requestId, RequestItem.ItemStatus status);

    List<RequestItem> findByEquipmentEquipmentId(UUID equipmentId);

    @Query("SELECT ri FROM RequestItem ri WHERE ri.equipment.equipmentId = :equipmentId " +
           "AND ri.status IN ('PENDING', 'APPROVED', 'ISSUED')")
    List<RequestItem> findActiveByEquipment(@Param("equipmentId") UUID equipmentId);

    void deleteByRequestRequestId(String requestId);

    List<RequestItem> findByRequestRequestIdOrderByRequestItemIdAsc(String requestId);
}
