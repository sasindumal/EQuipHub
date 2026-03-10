package com.equiphub.api.controller;

import com.equiphub.api.dto.department.*;
import com.equiphub.api.dto.user.UserResponse;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;


import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentAdminController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentAdminController Tests")
class DepartmentAdminControllerTest extends BaseControllerTest{
   

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean DepartmentService departmentService;
    @MockBean private UserManagementService userManagementService;
    @MockBean private EquipmentService equipmentService;
    @MockBean private RequestService requestService;
    @MockBean private DepartmentConfigurationService departmentConfigurationService;
    
    
    private static final String HOD_UUID = "00000000-0000-0000-0000-000000000004";
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
    }

    // ── GET MY DEPARTMENT ───────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/department-admin/my-department")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void getMyDepartment_Success() throws Exception {
        DepartmentResponse resp = DepartmentResponse.builder()
                .departmentId(departmentId)
                .name("Computer Engineering")
                .code("CE")
                .build();

        when(departmentService.getDepartmentById(any(UUID.class))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/department-admin/my-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Computer Engineering"));
    }

    // ── GET DEPARTMENT MEMBERS ───────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/department-admin/members")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void getDepartmentMembers_Success() throws Exception {
        UserResponse user = UserResponse.builder()
                .userId(UUID.randomUUID())
                .firstName("Staff")
                .lastName("Member")
                .role(User.Role.TECHNICALOFFICER.name())
                .build();

        when(userManagementService.getUsersByDepartment(any(UUID.class)))
                .thenReturn(List.of(user));

        // Need department context - controller resolves from user
        mockMvc.perform(get("/api/v1/department-admin/members"))
                .andExpect(status().isOk());
    }

    // ── DEPARTMENT EQUIPMENT STATS ──────────────────────────────
    @Test
    @DisplayName("GET /api/v1/department-admin/equipment-stats")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void getEquipmentStats_Success() throws Exception {
        Map<String, Object> stats = Map.of("totalActive", 50L);
        when(equipmentService.getDepartmentStats(any(UUID.class))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/department-admin/equipment-stats"))
                .andExpect(status().isOk());
    }
}