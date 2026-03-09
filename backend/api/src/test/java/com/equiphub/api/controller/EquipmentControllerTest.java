package com.equiphub.api.controller;

import com.equiphub.api.dto.equipment.*;
import com.equiphub.api.service.EquipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EquipmentController.class)
@DisplayName("EquipmentController Tests")
class EquipmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EquipmentService equipmentService;

    private static final UUID EQUIP_ID = UUID.randomUUID();

    private CreateEquipmentRequest buildCreateRequest() {
        CreateEquipmentRequest req = new CreateEquipmentRequest();
        req.setName("Oscilloscope");
        req.setCurrentLocation("Lab A");
        req.setTotalQuantity(2);
        req.setDepartmentId(UUID.randomUUID().toString());
        req.setCategoryId(1);
        return req;
    }

    @Test
    @DisplayName("GET /api/v1/equipment — returns list")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void getAllEquipment_Returns200() throws Exception {
        when(equipmentService.getAllEquipment()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/equipment/{id} — found → 200")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void getEquipmentById_Found_Returns200() throws Exception {
        EquipmentResponse resp = new EquipmentResponse();
        when(equipmentService.getEquipmentById(EQUIP_ID)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/equipment/{id}", EQUIP_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/equipment/{id} — not found → 404")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void getEquipmentById_NotFound_Returns404() throws Exception {
        when(equipmentService.getEquipmentById(EQUIP_ID))
                .thenThrow(new RuntimeException("Equipment not found"));

        mockMvc.perform(get("/api/v1/equipment/{id}", EQUIP_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/equipment — TO creates equipment → 201")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void createEquipment_Returns201() throws Exception {
        CreateEquipmentRequest req = buildCreateRequest();
        EquipmentResponse resp = new EquipmentResponse();
        when(equipmentService.createEquipment(any(CreateEquipmentRequest.class), any(UUID.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/v1/equipment/{id} — update → 200")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void updateEquipment_Returns200() throws Exception {
        UpdateEquipmentRequest req = new UpdateEquipmentRequest();
        req.setName("Updated Oscilloscope");

        EquipmentResponse resp = new EquipmentResponse();
        when(equipmentService.updateEquipment(any(UUID.class),
                any(UpdateEquipmentRequest.class), any(UUID.class)))
             .thenReturn(resp);

        mockMvc.perform(put("/api/v1/equipment/{id}", EQUIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/equipment/{id} — retire → 200")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void retireEquipment_Returns200() throws Exception {
        doNothing().when(equipmentService).retireEquipment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/equipment/{id}", EQUIP_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/equipment/department/{id} — by department")
    @WithMockUser(roles = "TECHNICALOFFICER")
    void getEquipmentByDepartment_Returns200() throws Exception {
        UUID deptId = UUID.randomUUID();
        when(equipmentService.getEquipmentByDepartment(deptId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/equipment/department/{id}", deptId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/equipment/available — available borrowable items")
    @WithMockUser(roles = "STUDENT")
    void getAvailableEquipment_Returns200() throws Exception {
        when(equipmentService.getAvailableEquipment()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/equipment/available"))
                .andExpect(status().isOk());
    }
}
