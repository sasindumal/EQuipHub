package com.equiphub.api.controller;

import com.equiphub.api.dto.equipment.*;
import com.equiphub.api.model.Equipment;
import com.equiphub.api.service.EquipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipmentController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentController Tests")
class EquipmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Mock  private EquipmentService equipmentService;

    private UUID equipmentId;
    private UUID departmentId;
    private EquipmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        equipmentId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        sampleResponse = EquipmentResponse.builder()
                .equipmentId(equipmentId.toString())
                .name("Oscilloscope")
                .type(Equipment.EquipmentType.BORROWABLE)
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .currentCondition(95)
                .conditionLabel("EXCELLENT")
                .totalQuantity(1)
                .retired(false)
                .build();
    }

    // ── CREATE ──────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/equipment — create equipment (TO)")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "TECHNICALOFFICER")
    void createEquipment_Success() throws Exception {
        CreateEquipmentRequest req = CreateEquipmentRequest.builder()
                .equipmentId(equipmentId)
                .name("Oscilloscope")
                .categoryId(1)
                .type(Equipment.EquipmentType.BORROWABLE)
                .departmentId(departmentId.toString())
                .totalQuantity(1)
                .build();

        when(equipmentService.createEquipment(any(), any(UUID.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Oscilloscope"));
    }

    // ── GET BY ID ───────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/{id} — get single")
    void getById_Success() throws Exception {
        when(equipmentService.getById(equipmentId)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/equipment/{id}", equipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oscilloscope"));
    }

    // ── GET BY DEPARTMENT ───────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId} — get all for department")
    void getByDepartment_Success() throws Exception {
        when(equipmentService.getByDepartment(departmentId, false))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Oscilloscope"));
    }

    // ── GET AVAILABLE ───────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId}/available")
    void getAvailable_Success() throws Exception {
        when(equipmentService.getAvailableByDepartment(departmentId))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}/available", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── UPDATE ──────────────────────────────────────────────────
    @Test
    @DisplayName("PUT /api/v1/equipment/{id} — update metadata")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "TECHNICALOFFICER")
    void updateEquipment_Success() throws Exception {
        UpdateEquipmentRequest req = UpdateEquipmentRequest.builder()
                .name("Updated Oscilloscope")
                .build();

        when(equipmentService.updateEquipment(any(UUID.class), any(), any(UUID.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/equipment/{id}", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── STATUS UPDATE ───────────────────────────────────────────
    @Test
    @DisplayName("PATCH /api/v1/equipment/{id}/status — change status")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "TECHNICALOFFICER")
    void updateStatus_Success() throws Exception {
        EquipmentStatusUpdateRequest req = EquipmentStatusUpdateRequest.builder()
                .status(Equipment.EquipmentStatus.MAINTENANCE)
                .reason("Annual calibration")
                .build();

        when(equipmentService.updateStatus(any(UUID.class), any(), any(UUID.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/v1/equipment/{id}/status", equipmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── RETIRE ──────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/equipment/{id}/retire — retire equipment")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "TECHNICALOFFICER")
    void retireEquipment_Success() throws Exception {
        when(equipmentService.retireEquipment(any(UUID.class), anyString(), any(UUID.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/equipment/{id}/retire", equipmentId)
                        .param("reason", "End of life"))
                .andExpect(status().isOk());
    }

    // ── CHECK AVAILABILITY ──────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/{id}/availability — check availability")
    void checkAvailability_Success() throws Exception {
        Map<String, Object> result = Map.of(
                "equipmentId", equipmentId.toString(),
                "isAvailable", true);
        when(equipmentService.checkAvailability(equipmentId)).thenReturn(result);

        mockMvc.perform(get("/api/v1/equipment/{id}/availability", equipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(true));
    }

    // ── SEARCH ──────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId}/search — search equipment")
    void searchEquipment_Success() throws Exception {
        when(equipmentService.search(departmentId, "Oscil"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}/search", departmentId)
                        .param("keyword", "Oscil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Oscilloscope"));
    }

    // ── DEPARTMENT STATS ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId}/stats — dashboard stats")
    void getDepartmentStats_Success() throws Exception {
        Map<String, Object> stats = Map.of("totalActive", 50L, "available", 40L);
        when(equipmentService.getDepartmentStats(departmentId)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}/stats", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActive").value(50));
    }

    // ── MAINTENANCE DUE ─────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId}/maintenance-due")
    void getMaintenanceDue_Success() throws Exception {
        when(equipmentService.getMaintenanceDue(departmentId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}/maintenance-due", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── LOW CONDITION ───────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/equipment/departments/{deptId}/low-condition")
    void getLowCondition_Success() throws Exception {
        when(equipmentService.getLowCondition(eq(departmentId), anyInt()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/equipment/departments/{deptId}/low-condition", departmentId)
                        .param("threshold", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}