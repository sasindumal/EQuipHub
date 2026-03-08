package com.equiphub.api.controller;

import com.equiphub.api.dto.user.*;
import com.equiphub.api.model.User;
import com.equiphub.api.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserManagementController Tests")
class UserManagementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UserManagementService userManagementService;

    private static final String ADMIN_UUID = "00000000-0000-0000-0000-000000000005";
    private UUID userId;
    private UUID departmentId;
    private UserResponse sampleUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        sampleUser = UserResponse.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@eng.pdn.ac.lk")
                .role(User.Role.STUDENT)
                .isActive(true)
                .build();
    }

    // ── CREATE STAFF ────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/users/staff — create staff user")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void createStaff_Success() throws Exception {
        CreateStaffRequest req = CreateStaffRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@eng.pdn.ac.lk")
                .role(User.Role.TECHNICALOFFICER)
                .departmentId(departmentId)
                .build();

        when(userManagementService.createStaffUser(any())).thenReturn(sampleUser);

        mockMvc.perform(post("/api/v1/users/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    // ── GET ALL USERS ───────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/users — get all users")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void getAllUsers_Success() throws Exception {
        when(userManagementService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    // ── GET USER BY ID ──────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/users/{id}")
    void getUserById_Success() throws Exception {
        when(userManagementService.getUserById(userId)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@eng.pdn.ac.lk"));
    }

    // ── GET BY DEPARTMENT ───────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/users/departments/{deptId}")
    void getByDepartment_Success() throws Exception {
        when(userManagementService.getUsersByDepartment(departmentId))
                .thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users/departments/{deptId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET BY ROLE ─────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/users/roles/{role}")
    void getByRole_Success() throws Exception {
        when(userManagementService.getUsersByRole(User.Role.STUDENT))
                .thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users/roles/{role}", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("STUDENT"));
    }

    // ── UPDATE USER ─────────────────────────────────────────────
    @Test
    @DisplayName("PUT /api/v1/users/{id} — update user")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void updateUser_Success() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .firstName("Johnny")
                .build();

        when(userManagementService.updateUser(any(UUID.class), any())).thenReturn(sampleUser);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── TOGGLE ACTIVE STATUS ────────────────────────────────────
    @Test
    @DisplayName("PATCH /api/v1/users/{id}/toggle-active")
    @WithMockUser(username = ADMIN_UUID, roles = "SYSTEMADMIN")
    void toggleActive_Success() throws Exception {
        when(userManagementService.toggleUserActive(userId)).thenReturn(sampleUser);

        mockMvc.perform(patch("/api/v1/users/{id}/toggle-active", userId))
                .andExpect(status().isOk());
    }

    // ── SEARCH USERS ────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/users/search")
    void searchUsers_Success() throws Exception {
        when(userManagementService.searchUsers(anyString())).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }
}