package com.equiphub.api.controller;

import com.equiphub.api.dto.department.*;
import com.equiphub.api.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)

@DisplayName("DepartmentController Tests")
class DepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Mock  private DepartmentService departmentService;

    private static final String ADMIN_UUID = "00000000-0000-0000-0000-000000000005";
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
    }

    // ── GET ALL DEPARTMENTS ─────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/departments — get all")
    void getAllDepartments_Success() throws Exception {
        DepartmentResponse dept = DepartmentResponse.builder()
                .departmentId(departmentId)
                .name("Computer Engineering")
                .code("CE")
                .build();

        when(departmentService.getAllDepartments()).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Computer Engineering"));
    }

    // ── GET BY ID ───────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/departments/{id}")
    void getById_Success() throws Exception {
        DepartmentResponse dept = DepartmentResponse.builder()
                .departmentId(departmentId)
                .name("Computer Engineering")
                .code("CE")
                .build();

        when(departmentService.getDepartmentById(departmentId)).thenReturn(dept);

        mockMvc.perform(get("/api/v1/departments/{id}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CE"));
    }

    // ── CREATE DEPARTMENT ───────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/departments — create")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void createDepartment_Success() throws Exception {
        CreateDepartmentRequest req = CreateDepartmentRequest.builder()
                .name("Electrical Engineering")
                .code("EE")
                .build();

        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(UUID.randomUUID())
                .name("Electrical Engineering")
                .code("EE")
                .build();

        when(departmentService.createDepartment(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electrical Engineering"));
    }

    // ── UPDATE DEPARTMENT ───────────────────────────────────────
    @Test
    @DisplayName("PUT /api/v1/departments/{id} — update")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void updateDepartment_Success() throws Exception {
        UpdateDepartmentRequest req = UpdateDepartmentRequest.builder()
                .name("Updated Department")
                .build();

        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(departmentId)
                .name("Updated Department")
                .code("CE")
                .build();

        when(departmentService.updateDepartment(any(UUID.class), any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/departments/{id}", departmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Department"));
    }
}