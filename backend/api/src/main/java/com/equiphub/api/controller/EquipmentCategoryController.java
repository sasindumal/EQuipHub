package com.equiphub.api.controller;

import com.equiphub.api.model.EquipmentCategory;
import com.equiphub.api.repository.EquipmentCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipment-categories")
@RequiredArgsConstructor
@Tag(name = "Equipment Categories",
     description = "CRUD for equipment categories. " +
                   "GET endpoints open to all authenticated users; " +
                   "POST/PUT restricted to DEPARTMENTADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentCategoryController {

    private final EquipmentCategoryRepository categoryRepository;

    // ────────────────────────────────────────────────────────────
    //  Inner DTO — keeps the controller self-contained
    // ────────────────────────────────────────────────────────────
    @Data
    public static class CategoryRequest {
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Name must be 100 characters or fewer")
        private String name;

        private String description;
        private Double damageMultiplierBase;
        private BigDecimal typicalReplacementCost;
    }

    // ────────────────────────────────────────────────────────────
    //  GET /api/v1/equipment-categories  (all authenticated users)
    // ────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all equipment categories",
               description = "Returns all categories. Used to populate the category dropdown.")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Map<String, Object>> data = categoryRepository.findAll()
                .stream().map(this::toMap).collect(Collectors.toList());
        return ok("Equipment categories retrieved successfully", data, data.size());
    }

    // ────────────────────────────────────────────────────────────
    //  GET /api/v1/equipment-categories/{id}  (all authenticated users)
    // ────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get equipment category by ID")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        return categoryRepository.findById(id)
                .map(c -> {
                    response.put("success",   true);
                    response.put("message",   "Category retrieved successfully");
                    response.put("timestamp", LocalDateTime.now());
                    response.put("data",      toMap(c));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success",   false);
                    response.put("message",   "Category not found with ID: " + id);
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(404).body(response);
                });
    }

    // ────────────────────────────────────────────────────────────
    //  POST /api/v1/equipment-categories  (DEPARTMENTADMIN only)
    //
    //  BUG FIX: was @PreAuthorize("hasRole('DEPTADMIN')")
    //  CustomUserDetails.build() grants: ROLE_DEPARTMENTADMIN
    //    (via: new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
    //  hasRole('DEPTADMIN') checks for:  ROLE_DEPTADMIN  ← MISMATCH → 403
    //  hasRole('DEPARTMENTADMIN') checks: ROLE_DEPARTMENTADMIN ← MATCH → 201
    // ────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('DEPARTMENTADMIN')")
    @Operation(summary = "Create a new equipment category",
               description = "Restricted to DEPARTMENTADMIN. Returns 409 if a category with the same name already exists.")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody CategoryRequest req) {
        Map<String, Object> response = new HashMap<>();

        if (categoryRepository.findByNameIgnoreCase(req.getName().trim()).isPresent()) {
            response.put("success",   false);
            response.put("message",   "A category named '" + req.getName().trim() + "' already exists");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(409).body(response);
        }

        EquipmentCategory saved = categoryRepository.save(
            EquipmentCategory.builder()
                .name(req.getName().trim())
                .description(req.getDescription())
                .damageMultiplierBase(req.getDamageMultiplierBase() != null ? req.getDamageMultiplierBase() : 1.0)
                .typicalReplacementCost(req.getTypicalReplacementCost())
                .build()
        );

        response.put("success",   true);
        response.put("message",   "Category '" + saved.getName() + "' created successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("data",      toMap(saved));
        return ResponseEntity.status(201).body(response);
    }

    // ────────────────────────────────────────────────────────────
    //  PUT /api/v1/equipment-categories/{id}  (DEPARTMENTADMIN only)
    //  Same fix as POST — DEPTADMIN → DEPARTMENTADMIN
    // ────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DEPARTMENTADMIN')")
    @Operation(summary = "Update an equipment category",
               description = "Restricted to DEPARTMENTADMIN. Partial update — only non-null fields are changed.")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest req) {

        Map<String, Object> response = new HashMap<>();

        return categoryRepository.findById(id)
                .map(existing -> {
                    if (req.getName() != null) {
                        String newName = req.getName().trim();
                        categoryRepository.findByNameIgnoreCase(newName).ifPresent(conflict -> {
                            if (!conflict.getCategoryId().equals(id)) {
                                throw new RuntimeException("CONFLICT: A category named '" + newName + "' already exists");
                            }
                        });
                        existing.setName(newName);
                    }
                    if (req.getDescription()            != null) existing.setDescription(req.getDescription());
                    if (req.getDamageMultiplierBase()   != null) existing.setDamageMultiplierBase(req.getDamageMultiplierBase());
                    if (req.getTypicalReplacementCost() != null) existing.setTypicalReplacementCost(req.getTypicalReplacementCost());

                    EquipmentCategory updated = categoryRepository.save(existing);
                    response.put("success",   true);
                    response.put("message",   "Category updated successfully");
                    response.put("timestamp", LocalDateTime.now());
                    response.put("data",      toMap(updated));
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success",   false);
                    response.put("message",   "Category not found with ID: " + id);
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(404).body(response);
                });
    }

    // ────────────────────────────────────────────────────────────
    //  Private helpers
    // ────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(EquipmentCategory c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",                    c.getCategoryId());
        m.put("name",                  c.getName());
        m.put("description",           c.getDescription());
        m.put("damageMultiplierBase",  c.getDamageMultiplierBase());
        m.put("typicalReplacementCost",
                c.getTypicalReplacementCost() != null ? c.getTypicalReplacementCost() : BigDecimal.ZERO);
        return m;
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data, Object count) {
        Map<String, Object> r = new HashMap<>();
        r.put("success",   true);
        r.put("message",   message);
        r.put("timestamp", LocalDateTime.now());
        r.put("data",      data);
        if (count != null) r.put("count", count);
        return ResponseEntity.ok(r);
    }
}
