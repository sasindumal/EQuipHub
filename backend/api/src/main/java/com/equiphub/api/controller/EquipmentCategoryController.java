package com.equiphub.api.controller;

import com.equiphub.api.model.EquipmentCategory;
import com.equiphub.api.repository.EquipmentCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Equipment Categories", description = "Read-only lookup for equipment categories used in equipment registration forms")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentCategoryController {

    private final EquipmentCategoryRepository categoryRepository;

    // ═══════════════════════════════════════════════════════════
    //  GET /api/v1/equipment-categories
    //  Returns all equipment categories with id, name, description,
    //  damageMultiplierBase, and typicalReplacementCost.
    //  Used by the frontend equipment registration form to populate
    //  the category dropdown dynamically instead of hardcoded IDs.
    // ═══════════════════════════════════════════════════════════
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all equipment categories",
        description = "Returns all categories from the equipmentcategories table. " +
                      "Used by the equipment registration form to populate the category dropdown. " +
                      "Always fetch this dynamically — never hardcode category IDs on the frontend."
    )
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<EquipmentCategory> categories = categoryRepository.findAll();

        List<Map<String, Object>> data = categories.stream()
            .map(c -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id",                    c.getCategoryId());
                item.put("name",                  c.getName());
                item.put("description",           c.getDescription());
                item.put("damageMultiplierBase",  c.getDamageMultiplierBase());
                item.put("typicalReplacementCost",
                    c.getTypicalReplacementCost() != null
                        ? c.getTypicalReplacementCost()
                        : BigDecimal.ZERO);
                return item;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success",    true);
        response.put("message",    "Equipment categories retrieved successfully");
        response.put("timestamp",  LocalDateTime.now());
        response.put("data",       data);
        response.put("count",      data.size());

        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  GET /api/v1/equipment-categories/{id}
    //  Returns a single category by its numeric ID.
    // ═══════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get equipment category by ID",
        description = "Returns a single equipment category by its numeric database ID."
    )
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        return categoryRepository.findById(id)
            .map(c -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id",                    c.getCategoryId());
                item.put("name",                  c.getName());
                item.put("description",           c.getDescription());
                item.put("damageMultiplierBase",  c.getDamageMultiplierBase());
                item.put("typicalReplacementCost",
                    c.getTypicalReplacementCost() != null
                        ? c.getTypicalReplacementCost()
                        : BigDecimal.ZERO);

                response.put("success",   true);
                response.put("message",   "Category retrieved successfully");
                response.put("timestamp", LocalDateTime.now());
                response.put("data",      item);
                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> {
                response.put("success",   false);
                response.put("message",   "Category not found with ID: " + id);
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(404).body(response);
            });
    }
}
