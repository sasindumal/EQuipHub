package com.equiphub.api.controller;

import com.equiphub.api.dto.config.DepartmentConfigurationRequest;
import com.equiphub.api.dto.config.DepartmentConfigurationResponse;
import com.equiphub.api.dto.department.CreateDepartmentRequest;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.repository.DepartmentRepository;
import com.equiphub.api.repository.UserRepository;
import com.equiphub.api.service.DepartmentConfigurationService;
import com.equiphub.api.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest extends BaseControllerTest {
          

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DepartmentService              departmentService;
    @MockBean private DepartmentConfigurationService configService;
    @MockBean private DepartmentRepository           departmentRepository;
    @MockBean private UserRepository                 userRepository;

    private static final UUID DEPT_ID = UUID.randomUUID();

    // ── Dashboard ────────────────────────────────────────────────
    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/dashboard — success")
    void getDashboard_Success() throws Exception {
        when(departmentRepository.count()).thenReturn(3L);
        when(departmentRepository.findAllByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.findByRole(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("GET /api/v1/admin/dashboard — forbidden for STUDENT")
    void getDashboard_ForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // ── Departments ──────────────────────────────────────────────
    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("POST /api/v1/admin/departments — create success")
    void createDepartment_Success() throws Exception {
        CreateDepartmentRequest req = new CreateDepartmentRequest();
        req.setCode("CSE");
        req.setName("Computer Science");

        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(DEPT_ID)
                .code("CSE")
                .build();

        DepartmentConfigurationResponse config = DepartmentConfigurationResponse.builder()
                .build();

        when(departmentService.createDepartment(any(), any(UUID.class))).thenReturn(resp);
        when(configService.initializeConfiguration(any(UUID.class), any(UUID.class))).thenReturn(config);

        mockMvc.perform(post("/api/v1/admin/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/departments — list all")
    void getAllDepartments_Success() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.departments").isArray());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/departments — activeOnly=true")
    void getAllDepartments_ActiveOnly() throws Exception {
        when(departmentService.getActiveDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/departments").param("activeOnly", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/departments/{id} — found")
    void getDepartmentById_Success() throws Exception {
        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(DEPT_ID)
                .build();
        when(departmentService.getDepartmentById(DEPT_ID)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("PUT /api/v1/admin/departments/{id} — update")
    void updateDepartment_Success() throws Exception {
        UpdateDepartmentRequest req = new UpdateDepartmentRequest();
        req.setName("Updated Name");

        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(DEPT_ID)
                .code("CSE")
                .build();
        when(departmentService.updateDepartment(any(UUID.class), any(), any(UUID.class))).thenReturn(resp);

        mockMvc.perform(put("/api/v1/admin/departments/{id}", DEPT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("DELETE /api/v1/admin/departments/{id} — deactivate")
    void deactivateDepartment_Success() throws Exception {
        doNothing().when(departmentService).deactivateDepartment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/v1/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── Configurations ───────────────────────────────────────────
    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/configurations — all configs")
    void getAllConfigurations_Success() throws Exception {
        when(configService.getAllConfigurations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/configurations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configurations").isArray());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/configurations/defaults")
    void getSystemDefaults_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/configurations/defaults"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.autoApprovalEnabled").exists());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("POST /api/v1/admin/departments/{id}/config — init config")
    void initializeConfig_Success() throws Exception {
        DepartmentConfigurationResponse config = DepartmentConfigurationResponse.builder()
                .build();
        when(configService.initializeConfiguration(any(UUID.class), any(UUID.class))).thenReturn(config);

        mockMvc.perform(post("/api/v1/admin/departments/{id}/config", DEPT_ID))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("GET /api/v1/admin/departments/{id}/config")
    void getConfig_Success() throws Exception {
        DepartmentConfigurationResponse config = DepartmentConfigurationResponse.builder()
                .build();
        when(configService.getByDepartmentId(DEPT_ID)).thenReturn(config);

        mockMvc.perform(get("/api/v1/admin/departments/{id}/config", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("PUT /api/v1/admin/departments/{id}/config — update config")
    void updateConfig_Success() throws Exception {
        DepartmentConfigurationRequest req = new DepartmentConfigurationRequest();
        DepartmentConfigurationResponse config = DepartmentConfigurationResponse.builder()
                .build();
        when(configService.updateConfiguration(any(UUID.class), any(), any(UUID.class))).thenReturn(config);

        mockMvc.perform(put("/api/v1/admin/departments/{id}/config", DEPT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEMADMIN")
    @DisplayName("POST /api/v1/admin/departments/{id}/config/reset — reset config")
    void resetConfig_Success() throws Exception {
        DepartmentConfigurationResponse config = DepartmentConfigurationResponse.builder()
                .build();
        when(configService.resetToDefaults(any(UUID.class), any(UUID.class))).thenReturn(config);

        mockMvc.perform(post("/api/v1/admin/departments/{id}/config/reset", DEPT_ID))
                .andExpect(status().isOk());
    }
}
