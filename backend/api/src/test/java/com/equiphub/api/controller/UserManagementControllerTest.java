package com.equiphub.api.controller;

import com.equiphub.api.dto.user.*;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.UserManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserManagementController.class)
class UserManagementControllerTest extends BaseControllerTest{   
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserManagementService userManagementService;
    // @MockBean CustomUserDetailsService userDetailsService;

    static final UUID ADMIN_ID = UUID.randomUUID();
    static final UUID DEPT_ID  = UUID.randomUUID();
    static final UUID USER_ID  = UUID.randomUUID();

    CustomUserDetails adminUser;
    CustomUserDetails deptAdminUser;

    @BeforeEach
    void setUp() {
        adminUser = mock(CustomUserDetails.class);
        when(adminUser.getUserId()).thenReturn(ADMIN_ID);
        when(adminUser.getRole()).thenReturn(User.Role.SYSTEMADMIN);
        when(adminUser.getDepartmentId()).thenReturn(null);
        when(adminUser.getUsername()).thenReturn(ADMIN_ID.toString());
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_SYSTEMADMIN"))).when(adminUser).getAuthorities();
        when(adminUser.isEnabled()).thenReturn(true);
        when(adminUser.isAccountNonExpired()).thenReturn(true);
        when(adminUser.isAccountNonLocked()).thenReturn(true);
        when(adminUser.isCredentialsNonExpired()).thenReturn(true);

        UUID daId = UUID.randomUUID();
        deptAdminUser = mock(CustomUserDetails.class);
        when(deptAdminUser.getUserId()).thenReturn(daId);
        when(deptAdminUser.getRole()).thenReturn(User.Role.DEPARTMENTADMIN);
        when(deptAdminUser.getDepartmentId()).thenReturn(DEPT_ID.toString());
        when(deptAdminUser.getUsername()).thenReturn(daId.toString());
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DEPARTMENTADMIN"))).when(deptAdminUser).getAuthorities();
        when(deptAdminUser.isEnabled()).thenReturn(true);
        when(deptAdminUser.isAccountNonExpired()).thenReturn(true);
        when(deptAdminUser.isAccountNonLocked()).thenReturn(true);
        when(deptAdminUser.isCredentialsNonExpired()).thenReturn(true);
    }

    // ── UserResponse: @Builder only → use builder, not new UserResponse()
    // ── role field is String, status field is String (no setActive, no setRole(User.Role))
    UserResponse sampleUser() {
        return UserResponse.builder()
            .userId(USER_ID)
            .email("staff@test.com")
            .role("TECHNICALOFFICER")   // String, not User.Role
            .status("ACTIVE")           // String — no setActive(boolean)
            .departmentId(DEPT_ID)
            .build();
    }

    // ── 1. CREATE STAFF ──────────────────────────────────────────────────────────
    // createStaff(CreateStaffRequest, UUID) — 2 args
    // CreateStaffRequest.departmentId is String, not UUID

    @Test @DisplayName("POST /users/staff — SYSTEMADMIN → 201")
    void createStaff_Admin_201() throws Exception {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setEmail("new.to@dept.com");
        req.setRole(User.Role.TECHNICALOFFICER);
        req.setDepartmentId(DEPT_ID.toString());   // String field

        when(userManagementService.createStaff(any(CreateStaffRequest.class), any(UUID.class)))
            .thenReturn(sampleUser());

        mockMvc.perform(post("/api/v1/users/staff")
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.email").value("staff@test.com"));
    }

    @Test @DisplayName("POST /users/staff — DEPT_ADMIN own dept → 201")
    void createStaff_DeptAdmin_OwnDept_201() throws Exception {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setEmail("new.to@dept.com");
        req.setRole(User.Role.TECHNICALOFFICER);
        req.setDepartmentId(DEPT_ID.toString());   // String field

        when(userManagementService.createStaff(any(CreateStaffRequest.class), any(UUID.class)))
            .thenReturn(sampleUser());

        mockMvc.perform(post("/api/v1/users/staff")
                .with(user(deptAdminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /users/staff — DEPT_ADMIN wrong dept → 403")
    void createStaff_DeptAdmin_WrongDept_403() throws Exception {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setEmail("other@dept.com");
        req.setRole(User.Role.TECHNICALOFFICER);
        req.setDepartmentId(UUID.randomUUID().toString()); // different dept as String

        mockMvc.perform(post("/api/v1/users/staff")
                .with(user(deptAdminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    // ── 2. GET ALL USERS ─────────────────────────────────────────────────────────
    // getAllUsers() takes NO args — no Pageable overload

    @Test @DisplayName("GET /users — SYSTEMADMIN → 200")
    void getAllUsers_Admin_200() throws Exception {
        when(userManagementService.getAllUsers())
            .thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/v1/users").with(user(adminUser)))
            .andExpect(status().isOk());
    }

    // ── 3. GET USER BY ID ────────────────────────────────────────────────────────

    @Test @DisplayName("GET /users/{userId} → 200")
    void getUserById_200() throws Exception {
        when(userManagementService.getUserById(any(UUID.class))).thenReturn(sampleUser());

        mockMvc.perform(get("/api/v1/users/{id}", USER_ID).with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()));
    }

    @Test @DisplayName("GET /users/{userId} — not found → 404")
    void getUserById_NotFound_404() throws Exception {
        when(userManagementService.getUserById(any(UUID.class)))
            .thenThrow(new NoSuchElementException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()).with(user(adminUser)))
            .andExpect(status().isNotFound());
    }

    // ── 4. GET BY DEPARTMENT ─────────────────────────────────────────────────────

    @Test @DisplayName("GET /users/department/{deptId} — own dept → 200")
    void getByDept_OwnDept_200() throws Exception {
        when(userManagementService.getUsersByDepartment(any(UUID.class)))
            .thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/v1/users/department/{deptId}", DEPT_ID).with(user(deptAdminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test @DisplayName("GET /users/department/{deptId} — wrong dept → 403")
    void getByDept_WrongDept_403() throws Exception {
        mockMvc.perform(get("/api/v1/users/department/{deptId}", UUID.randomUUID())
                .with(user(deptAdminUser)))
            .andExpect(status().isForbidden());
    }

    // ── 5. SEARCH USERS ──────────────────────────────────────────────────────────

    @Test @DisplayName("GET /users/search?keyword=john — SYSTEMADMIN → 200")
    void searchUsers_Admin_200() throws Exception {
        when(userManagementService.searchUsers(anyString()))
            .thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/v1/users/search").param("keyword", "john").with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 6. UPDATE USER ───────────────────────────────────────────────────────────
    // updateUser(UUID, UpdateUserRequest, UUID) — 3 args
    // UpdateUserRequest has: firstName, lastName, phone, status, departmentId (String) — NO email

    @Test @DisplayName("PUT /users/{userId} — SYSTEMADMIN → 200")
    void updateUser_Admin_200() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Updated");
        req.setLastName("Name");
        req.setPhone("0771234567");
        req.setStatus("ACTIVE");
        // departmentId is String if reassigning dept
        // req.setDepartmentId(DEPT_ID.toString());

        when(userManagementService.updateUser(any(UUID.class), any(UpdateUserRequest.class), any(UUID.class)))
            .thenReturn(sampleUser());

        mockMvc.perform(put("/api/v1/users/{id}", USER_ID)
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    // ── 7. SUSPEND / ACTIVATE ────────────────────────────────────────────────────
    // toggleActive() doesn't exist → service has suspendUser(UUID, UUID) and activateUser(UUID, UUID)

    @Test @DisplayName("PUT /users/{userId}/suspend → 200")
    void suspendUser_200() throws Exception {
        UserResponse suspended = UserResponse.builder()
            .userId(USER_ID)
            .email("staff@test.com")
            .role("TECHNICALOFFICER")
            .status("SUSPENDED")
            .departmentId(DEPT_ID)
            .build();

        when(userManagementService.suspendUser(any(UUID.class), any(UUID.class)))
            .thenReturn(suspended);

        mockMvc.perform(put("/api/v1/users/{id}/suspend", USER_ID).with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
    }

    @Test @DisplayName("PUT /users/{userId}/activate → 200")
    void activateUser_200() throws Exception {
        when(userManagementService.activateUser(any(UUID.class), any(UUID.class)))
            .thenReturn(sampleUser());

        mockMvc.perform(put("/api/v1/users/{id}/activate", USER_ID).with(user(adminUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ── 8. GET BY ROLE ───────────────────────────────────────────────────────────
    // getUsersByRole(String role) — String, not User.Role enum

    @Test @DisplayName("GET /users/role/TECHNICALOFFICER → 200")
    void getUsersByRole_200() throws Exception {
        when(userManagementService.getUsersByRole(anyString()))
            .thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/v1/users/role/TECHNICALOFFICER").with(user(adminUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /users/role/GARBAGE → 400")
    void getUsersByRole_InvalidRole_400() throws Exception {
        mockMvc.perform(get("/api/v1/users/role/GARBAGE").with(user(adminUser)))
            .andExpect(status().isBadRequest());
    }

    // ── 9. DEPARTMENT STATS ──────────────────────────────────────────────────────
    // getUserStats() doesn't exist → service has getDepartmentStats(UUID)

    @Test @DisplayName("GET /users/department/{deptId}/stats — SYSTEMADMIN → 200")
    void getDeptStats_200() throws Exception {
        when(userManagementService.getDepartmentStats(any(UUID.class)))
            .thenReturn(Map.of("totalUsers", 50, "activeUsers", 45));

        mockMvc.perform(get("/api/v1/users/department/{deptId}/stats", DEPT_ID).with(user(adminUser)))
            .andExpect(status().isOk());
    }
}
