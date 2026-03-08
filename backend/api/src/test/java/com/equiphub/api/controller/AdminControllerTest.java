package com.equiphub.api.controller;

import com.equiphub.api.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Mock  private DepartmentService departmentService;
    @Mock  private UserManagementService userManagementService;
    @Mock  private EquipmentService equipmentService;
    @Mock  private RequestService requestService;

    private static final String ADMIN_UUID = "00000000-0000-0000-0000-000000000005";

    // ── SYSTEM DASHBOARD ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/admin/dashboard — system dashboard")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void getSystemDashboard_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk());
    }

    // ── ALL DEPARTMENTS ─────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/admin/departments")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void getAllDepartments_Success() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/departments"))
                .andExpect(status().isOk());
    }

    // ── ALL USERS ───────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/admin/users")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void getAllUsers_Success() throws Exception {
        when(userManagementService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }

    // ── FORBIDDEN FOR NON-ADMIN ─────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/admin/dashboard — forbidden for STUDENT")
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = "STUDENT")
    void dashboard_ForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}