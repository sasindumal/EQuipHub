package com.equiphub.api.service;

import com.equiphub.api.dto.equipment.*;
import com.equiphub.api.model.Department;
import com.equiphub.api.model.Equipment;
import com.equiphub.api.model.EquipmentCategory;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.EquipmentCategoryRepository;
import com.equiphub.api.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentService {

    private final EquipmentRepository         equipmentRepository;
    private final DepartmentRepository        departmentRepository;
    private final EquipmentCategoryRepository categoryRepository;

    // ── Condition label thresholds ───────────────────────────────
    private static final int CONDITION_EXCELLENT = 85;
    private static final int CONDITION_GOOD      = 65;
    private static final int CONDITION_FAIR       = 40;
    private static final int CONDITION_POOR       = 20;

    // ─────────────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public EquipmentResponse createEquipment(CreateEquipmentRequest req, UUID createdBy) {
        if (equipmentRepository.existsById(req.getEquipmentId())) {
            throw new RuntimeException("Equipment ID '" + req.getEquipmentId() + "' already exists");
        }
        if (req.getSerialNumber() != null && !req.getSerialNumber().isBlank()
                && equipmentRepository.existsBySerialNumber(req.getSerialNumber())) {
            throw new RuntimeException("Serial number '" + req.getSerialNumber() + "' already registered");
        }

        Department dept = departmentRepository.findById(UUID.fromString(req.getDepartmentId()))
                .orElseThrow(() -> new RuntimeException("Department not found: " + req.getDepartmentId()));

        EquipmentCategory category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + req.getCategoryId()));

        Equipment equipment = Equipment.builder()
                .equipmentId(req.getEquipmentId())
                .name(req.getName())
                .category(category)
                .type(req.getType())
                .department(dept)
                .description(req.getDescription())
                .specificationsJson(req.getSpecificationsJson())
                .purchaseDate(req.getPurchaseDate())
                .purchaseValue(req.getPurchaseValue())
                .serialNumber(req.getSerialNumber())
                .currentCondition(100)
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .totalQuantity(req.getTotalQuantity() != null ? req.getTotalQuantity() : 1)
                .currentLocation(req.getCurrentLocation())
                .assignedLabs(req.getAssignedLabs())
                .maintenanceIntervalDays(req.getMaintenanceIntervalDays())
                .replacementCost(req.getReplacementCost())
                .depreciationRate(req.getDepreciationRate())
                .retired(false)
                .nextMaintenanceDate(
                    req.getMaintenanceIntervalDays() != null
                        ? LocalDate.now().plusDays(req.getMaintenanceIntervalDays())
                        : null
                )
                .build();

        Equipment saved = equipmentRepository.save(equipment);
        log.info("[EQUIP_CREATE] {} '{}' added to dept {} by {}",
                saved.getEquipmentId(), saved.getName(), dept.getCode(), createdBy);
        return mapToResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — single
    // ─────────────────────────────────────────────────────────────
    public EquipmentResponse getById(UUID equipmentId) {
        return mapToResponse(findEquipment(equipmentId));
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — all (SYSTEMADMIN)
    // ─────────────────────────────────────────────────────────────
    public List<EquipmentResponse> getAll(boolean activeOnly) {
        List<Equipment> list = activeOnly
                ? equipmentRepository.findAllActive()
                : equipmentRepository.findAll();
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  READ — by department
    // ─────────────────────────────────────────────────────────────
    public List<EquipmentResponse> getByDepartment(UUID departmentId, boolean activeOnly) {
        List<Equipment> list = activeOnly
                ? equipmentRepository.findActiveByDepartmentId(departmentId)
                : equipmentRepository.findByDepartmentDepartmentId(departmentId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getAvailableByDepartment(UUID departmentId) {
        return equipmentRepository.findAvailableByDepartmentId(departmentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getByDepartmentAndStatus(UUID departmentId, Equipment.EquipmentStatus status) {
        return equipmentRepository.findByDepartmentIdAndStatus(departmentId, status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getByDepartmentAndType(UUID departmentId, Equipment.EquipmentType type) {
        return equipmentRepository
                .findByDepartmentDepartmentIdAndTypeAndRetiredFalse(departmentId, type)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getRetiredByDepartment(UUID departmentId) {
        return equipmentRepository.findByDepartmentDepartmentIdAndRetiredTrue(departmentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getMaintenanceDue(UUID departmentId) {
        return equipmentRepository.findMaintenanceDueByDepartment(departmentId, LocalDate.now())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> getLowCondition(UUID departmentId, int threshold) {
        return equipmentRepository.findLowConditionByDepartment(departmentId, threshold)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────────────────────
    public List<EquipmentResponse> search(UUID departmentId, String keyword) {
        if (keyword == null || keyword.trim().length() < 2)
            throw new RuntimeException("Search keyword must be at least 2 characters");
        return equipmentRepository.searchByDepartment(departmentId, keyword.trim())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<EquipmentResponse> searchGlobal(String keyword) {
        if (keyword == null || keyword.trim().length() < 2)
            throw new RuntimeException("Search keyword must be at least 2 characters");
        return equipmentRepository.searchGlobal(keyword.trim())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE — metadata
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public EquipmentResponse updateEquipment(UUID equipmentId, UpdateEquipmentRequest req, UUID updatedBy) {
        Equipment e = findEquipment(equipmentId);

        if (req.getName() != null)                  e.setName(req.getName());
        if (req.getDescription() != null)           e.setDescription(req.getDescription());
        if (req.getSpecificationsJson() != null)    e.setSpecificationsJson(req.getSpecificationsJson());
        if (req.getPurchaseDate() != null)          e.setPurchaseDate(req.getPurchaseDate());
        if (req.getPurchaseValue() != null)         e.setPurchaseValue(req.getPurchaseValue());
        if (req.getReplacementCost() != null)       e.setReplacementCost(req.getReplacementCost());
        if (req.getDepreciationRate() != null)      e.setDepreciationRate(req.getDepreciationRate());
        if (req.getCurrentLocation() != null)       e.setCurrentLocation(req.getCurrentLocation());
        if (req.getAssignedLabs() != null)          e.setAssignedLabs(req.getAssignedLabs());
        if (req.getMaintenanceIntervalDays() != null) e.setMaintenanceIntervalDays(req.getMaintenanceIntervalDays());
        if (req.getLastMaintenanceDate() != null)   e.setLastMaintenanceDate(req.getLastMaintenanceDate());
        if (req.getNextMaintenanceDate() != null)   e.setNextMaintenanceDate(req.getNextMaintenanceDate());
        if (req.getCurrentCondition() != null)      e.setCurrentCondition(req.getCurrentCondition());
        if (req.getConditionNotes() != null)        e.setConditionNotes(req.getConditionNotes());
        if (req.getSerialNumber() != null) {
            if (!req.getSerialNumber().equals(e.getSerialNumber())
                    && equipmentRepository.existsBySerialNumber(req.getSerialNumber())) {
                throw new RuntimeException("Serial number '" + req.getSerialNumber() + "' already in use");
            }
            e.setSerialNumber(req.getSerialNumber());
        }
        if (req.getCategoryId() != null) {
            EquipmentCategory cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + req.getCategoryId()));
            e.setCategory(cat);
        }
        if (req.getType() != null) e.setType(req.getType());

        Equipment updated = equipmentRepository.save(e);
        log.info("[EQUIP_UPDATE] {} updated by {}", equipmentId, updatedBy);
        return mapToResponse(updated);
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE — status
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public EquipmentResponse updateStatus(UUID equipmentId, EquipmentStatusUpdateRequest req, UUID updatedBy) {
        Equipment e = findEquipment(equipmentId);

        validateStatusTransition(e.getStatus(), req.getStatus(), e.getEquipmentId());

        if (req.getStatus() == Equipment.EquipmentStatus.MAINTENANCE && (req.getReason() == null || req.getReason().isBlank())) {
            throw new RuntimeException("A reason is required when sending equipment to MAINTENANCE");
        }
        if (req.getStatus() == Equipment.EquipmentStatus.DAMAGED && (req.getReason() == null || req.getReason().isBlank())) {
            throw new RuntimeException("A reason is required when marking equipment as DAMAGED");
        }

        Equipment.EquipmentStatus oldStatus = e.getStatus();
        e.setStatus(req.getStatus());

        if (req.getConditionScore() != null)      e.setCurrentCondition(req.getConditionScore());
        if (req.getReason() != null)              e.setConditionNotes(req.getReason());
        if (req.getNextMaintenanceDate() != null) e.setNextMaintenanceDate(req.getNextMaintenanceDate());

        if (oldStatus == Equipment.EquipmentStatus.MAINTENANCE
                && req.getStatus() == Equipment.EquipmentStatus.AVAILABLE) {
            e.setLastMaintenanceDate(LocalDate.now());
            if (e.getMaintenanceIntervalDays() != null) {
                e.setNextMaintenanceDate(LocalDate.now().plusDays(e.getMaintenanceIntervalDays()));
            }
        }

        Equipment updated = equipmentRepository.save(e);
        log.info("[EQUIP_STATUS] {} status {} → {} by {}", equipmentId, oldStatus, req.getStatus(), updatedBy);
        return mapToResponse(updated);
    }

    // ─────────────────────────────────────────────────────────────
    //  RETIRE (soft delete)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public EquipmentResponse retireEquipment(UUID equipmentId, String reason, UUID retiredBy) {
        Equipment e = findEquipment(equipmentId);

        if (e.getRetired()) {
            throw new RuntimeException("Equipment '" + equipmentId + "' is already retired");
        }
        if (e.getStatus() == Equipment.EquipmentStatus.INUSE
                || e.getStatus() == Equipment.EquipmentStatus.RESERVED) {
            throw new RuntimeException("Cannot retire equipment that is currently " + e.getStatus() +
                                       ". Update status first.");
        }

        e.setRetired(true);
        e.setStatus(Equipment.EquipmentStatus.ARCHIVED);
        e.setConditionNotes("RETIRED: " + (reason != null ? reason : "No reason provided"));

        Equipment updated = equipmentRepository.save(e);
        log.warn("[EQUIP_RETIRE] {} retired by {} — reason: {}", equipmentId, retiredBy, reason);
        return mapToResponse(updated);
    }

    // ─────────────────────────────────────────────────────────────
    //  CHECK AVAILABILITY
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> checkAvailability(UUID equipmentId) {
        Equipment e = findEquipment(equipmentId);
        Map<String, Object> result = new HashMap<>();
        result.put("equipmentId",     e.getEquipmentId());
        result.put("name",            e.getName());
        result.put("status",          e.getStatus());
        result.put("isAvailable",     e.getStatus() == Equipment.EquipmentStatus.AVAILABLE && !e.getRetired());
        result.put("totalQuantity",   e.getTotalQuantity());
        result.put("currentLocation", e.getCurrentLocation());
        result.put("conditionScore",  e.getCurrentCondition());
        result.put("conditionLabel",  getConditionLabel(e.getCurrentCondition()));
        result.put("maintenanceDue",  isMaintenanceDue(e));
        result.put("retired",         e.getRetired());
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    //  DEPARTMENT DASHBOARD STATS
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> getDepartmentStats(UUID departmentId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("departmentId",        departmentId);
        stats.put("totalActive",         equipmentRepository.countActiveByDepartment(departmentId));
        stats.put("available",           equipmentRepository.countAvailableByDepartment(departmentId));
        stats.put("inMaintenance",       equipmentRepository.countMaintenanceByDepartment(departmentId));
        stats.put("maintenanceDueCount", equipmentRepository.findMaintenanceDueByDepartment(departmentId, LocalDate.now()).size());
        stats.put("lowConditionCount",   equipmentRepository.findLowConditionByDepartment(departmentId, CONDITION_POOR).size());

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Equipment.EquipmentStatus s : Equipment.EquipmentStatus.values()) {
            long count = equipmentRepository.findByDepartmentIdAndStatus(departmentId, s).size();
            byStatus.put(s.name(), count);
        }
        stats.put("byStatus", byStatus);

        long labDedicated = equipmentRepository
                .findByDepartmentDepartmentIdAndTypeAndRetiredFalse(departmentId, Equipment.EquipmentType.LABDEDICATED).size();
        long borrowable = equipmentRepository
                .findByDepartmentDepartmentIdAndTypeAndRetiredFalse(departmentId, Equipment.EquipmentType.BORROWABLE).size();
        stats.put("labDedicated",  labDedicated);
        stats.put("borrowable",    borrowable);
        stats.put("generatedAt",   LocalDateTime.now());

        return stats;
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────
    private Equipment findEquipment(UUID equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found: " + equipmentId));
    }

    private void validateStatusTransition(Equipment.EquipmentStatus from,
                                          Equipment.EquipmentStatus to,
                                          UUID id) {
        if (from == Equipment.EquipmentStatus.ARCHIVED) {
            throw new RuntimeException("Equipment " + id + " is ARCHIVED and cannot be updated");
        }
        if (from == Equipment.EquipmentStatus.INUSE && to != Equipment.EquipmentStatus.AVAILABLE
                && to != Equipment.EquipmentStatus.DAMAGED && to != Equipment.EquipmentStatus.MAINTENANCE) {
            throw new RuntimeException("IN_USE equipment can only transition to AVAILABLE, DAMAGED, or MAINTENANCE");
        }
    }

    private String getConditionLabel(int score) {
        if (score >= CONDITION_EXCELLENT) return "EXCELLENT";
        if (score >= CONDITION_GOOD)      return "GOOD";
        if (score >= CONDITION_FAIR)      return "FAIR";
        if (score >= CONDITION_POOR)      return "POOR";
        return "CRITICAL";
    }

    private boolean isMaintenanceDue(Equipment e) {
        return e.getNextMaintenanceDate() != null
                && !e.getNextMaintenanceDate().isAfter(LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────────────────────
    public EquipmentResponse mapToResponse(Equipment e) {
        return EquipmentResponse.builder()
                .equipmentId(e.getEquipmentId().toString())
                .name(e.getName())
                .categoryId(e.getCategory() != null ? e.getCategory().getCategoryId() : null)
                .categoryName(e.getCategory() != null ? e.getCategory().getName() : null)
                .type(e.getType())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .departmentCode(e.getDepartment() != null ? e.getDepartment().getCode() : null)
                .description(e.getDescription())
                .specificationsJson(e.getSpecificationsJson())
                .purchaseDate(e.getPurchaseDate())
                .purchaseValue(e.getPurchaseValue())
                .serialNumber(e.getSerialNumber())
                .currentCondition(e.getCurrentCondition())
                .conditionLabel(getConditionLabel(e.getCurrentCondition()))
                .conditionNotes(e.getConditionNotes())
                .status(e.getStatus())
                .totalQuantity(e.getTotalQuantity())
                .currentLocation(e.getCurrentLocation())
                .assignedLabs(e.getAssignedLabs())
                .lastMaintenanceDate(e.getLastMaintenanceDate())
                .nextMaintenanceDate(e.getNextMaintenanceDate())
                .maintenanceIntervalDays(e.getMaintenanceIntervalDays())
                .depreciationRate(e.getDepreciationRate())
                .replacementCost(e.getReplacementCost())
                .retired(e.getRetired() != null && e.getRetired())
                .maintenanceDue(isMaintenanceDue(e))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
