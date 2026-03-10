package com.equiphub.api.controller;

import com.equiphub.api.dto.equipment.*;
import com.equiphub.api.model.Equipment;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.EquipmentService;
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

@WebMvcTest(EquipmentController.class)
class EquipmentControllerTest extends BaseControllerTest{

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean EquipmentService equipmentService;

    // If your JWT filter requires it, add: @MockBean JwtUtils jwtUtils;

    static final UUID USER_ID  = UUID.randomUUID();
    static final UUID DEPT_ID  = UUID.randomUUID();
    static final UUID EQUIP_ID = UUID.randomUUID();

    CustomUserDetails toUser;
    CustomUserDetails adminUser;

    @BeforeEach
    void setUp() {
        toUser = mock(CustomUserDetails.class);
        when(toUser.getUserId()).thenReturn(USER_ID);
        when(toUser.getRole()).thenReturn(User.Role.TECHNICALOFFICER);
        when(toUser.getDepartmentId()).thenReturn(DEPT_ID.toString());
        when(toUser.getEmail()).thenReturn("to@test.com");
        when(toUser.getUsername()).thenReturn(USER_ID.toString());
        doReturn(List.of(new SimpleGrantedAuthority("TECHNICALOFFICER"))).when(toUser).getAuthorities();
        when(toUser.isEnabled()).thenReturn(true);
        when(toUser.isAccountNonExpired()).thenReturn(true);
        when(toUser.isAccountNonLocked()).thenReturn(true);
        when(toUser.isCredentialsNonExpired()).thenReturn(true);

        adminUser = mock(CustomUserDetails.class);
        when(adminUser.getUserId()).thenReturn(UUID.randomUUID());
        when(adminUser.getRole()).thenReturn(User.Role.SYSTEMADMIN);
        when(adminUser.getDepartmentId()).thenReturn(null);
        when(adminUser.getEmail()).thenReturn("admin@test.com");
        when(adminUser.getUsername()).thenReturn(UUID.randomUUID().toString());
        doReturn(List.of(new SimpleGrantedAuthority("SYSTEMADMIN"))).when(adminUser).getAuthorities();
        when(adminUser.isEnabled()).thenReturn(true);
        when(adminUser.isAccountNonExpired()).thenReturn(true);
        when(adminUser.isAccountNonLocked()).thenReturn(true);
        when(adminUser.isCredentialsNonExpired()).thenReturn(true);
    }

    EquipmentResponse sample() {
        return EquipmentResponse.builder()
                .equipmentId(EQUIP_ID.toString())
                .name("Oscilloscope")
                .departmentId(DEPT_ID)
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .currentCondition(90)
                .retired(false)
                .build();
    }

    // ── 1. CREATE ────────────────────────────────────────────────
    @Test @DisplayName("POST /equipment — own dept → 201")
    void createEquipment_Success() throws Exception {
        CreateEquipmentRequest req = new CreateEquipmentRequest();
        req.setEquipmentId(EQUIP_ID);
        req.setName("Oscilloscope");
        req.setDepartmentId(DEPT_ID.toString());
        req.setTotalQuantity(1);

        when(equipmentService.createEquipment(any(), any(UUID.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/equipment")
                        .with(user(toUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Oscilloscope"));
    }

    @Test @DisplayName("POST /equipment — wrong dept → 403")
    void createEquipment_WrongDept_Forbidden() throws Exception {
        CreateEquipmentRequest req = new CreateEquipmentRequest();
        req.setEquipmentId(EQUIP_ID);
        req.setName("Oscilloscope");
        req.setDepartmentId(UUID.randomUUID().toString());   // different dept
        req.setTotalQuantity(1);

        mockMvc.perform(post("/api/v1/equipment")
                        .with(user(toUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── 2. GET BY ID ─────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/{id} → 200")
    void getById_Success() throws Exception {
        when(equipmentService.getById(any(UUID.class))).thenReturn(sample());

        mockMvc.perform(get("/api/v1/equipment/{id}", EQUIP_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.equipmentId").value(EQUIP_ID.toString()));
    }

    // ── 3. AVAILABILITY ──────────────────────────────────────────
    @Test @DisplayName("GET /equipment/{id}/availability → 200")
    void checkAvailability_Success() throws Exception {
        when(equipmentService.checkAvailability(any(UUID.class)))
                .thenReturn(Map.of("available", true, "status", "AVAILABLE"));

        mockMvc.perform(get("/api/v1/equipment/{id}/availability", EQUIP_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));
    }

    // ── 4. BY DEPARTMENT ─────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId} — own dept → 200")
    void getByDepartment_Success() throws Exception {
        when(equipmentService.getByDepartment(any(UUID.class), anyBoolean()))
                .thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test @DisplayName("GET /equipment/department/{deptId} — wrong dept → 403")
    void getByDepartment_WrongDept_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/department/{deptId}", UUID.randomUUID()).with(user(toUser)))
                .andExpect(status().isForbidden());
    }

    // ── 5. AVAILABLE ─────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/available → 200")
    void getAvailable_Success() throws Exception {
        when(equipmentService.getAvailableByDepartment(any(UUID.class))).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/available", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 6. BY STATUS ─────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/status/AVAILABLE → 200")
    void getByStatus_Success() throws Exception {
        when(equipmentService.getByDepartmentAndStatus(any(UUID.class), any()))
                .thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/status/AVAILABLE", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test @DisplayName("GET /equipment/department/{deptId}/status/GARBAGE → 400")
    void getByStatus_InvalidStatus_Bad() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/status/GARBAGE", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isBadRequest());
    }

    // ── 7. BY TYPE ───────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/type/BORROWABLE → 200")
    void getByType_Success() throws Exception {
        when(equipmentService.getByDepartmentAndType(any(UUID.class), any()))
                .thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/type/BORROWABLE", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /equipment/department/{deptId}/type/GARBAGE → 400")
    void getByType_Invalid_Bad() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/type/GARBAGE", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isBadRequest());
    }

    // ── 8. MAINTENANCE DUE ───────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/maintenance-due → 200")
    void getMaintenanceDue_Success() throws Exception {
        when(equipmentService.getMaintenanceDue(any(UUID.class))).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/maintenance-due", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 9. LOW CONDITION ─────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/low-condition → 200")
    void getLowCondition_DefaultThreshold_Success() throws Exception {
        when(equipmentService.getLowCondition(any(UUID.class), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/low-condition", DEPT_ID)
                        .with(user(toUser)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /equipment/department/{deptId}/low-condition?threshold=200 → 400")
    void getLowCondition_InvalidThreshold_Bad() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/low-condition", DEPT_ID)
                        .param("threshold", "200")
                        .with(user(toUser)))
                .andExpect(status().isBadRequest());
    }

    // ── 10. SEARCH ───────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/search?keyword=osc → 200")
    void search_Success() throws Exception {
        when(equipmentService.search(any(UUID.class), anyString())).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/search", DEPT_ID)
                        .param("keyword", "osc")
                        .with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 11. GLOBAL SEARCH ────────────────────────────────────────
    @Test @DisplayName("GET /equipment/search?keyword=osc — SYSTEMADMIN → 200")
    void searchGlobal_Admin_Success() throws Exception {
        when(equipmentService.searchGlobal(anyString())).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/search")
                        .param("keyword", "osc")
                        .with(user(adminUser)))
                .andExpect(status().isOk());
    }

    // ── 12. UPDATE ───────────────────────────────────────────────
    @Test @DisplayName("PUT /equipment/{id} — own dept → 200")
    void updateEquipment_Success() throws Exception {
        UpdateEquipmentRequest req = new UpdateEquipmentRequest();
        req.setName("New Name");

        when(equipmentService.getById(any(UUID.class))).thenReturn(sample());
        when(equipmentService.updateEquipment(any(UUID.class), any(), any(UUID.class))).thenReturn(sample());

        mockMvc.perform(put("/api/v1/equipment/{id}", EQUIP_ID)
                        .with(user(toUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 13. UPDATE STATUS ────────────────────────────────────────
    @Test @DisplayName("PATCH /equipment/{id}/status → MAINTENANCE → 200")
    void updateStatus_Success() throws Exception {
        EquipmentStatusUpdateRequest req = new EquipmentStatusUpdateRequest();
        req.setStatus(Equipment.EquipmentStatus.MAINTENANCE);
        req.setReason("Scheduled maintenance");

        when(equipmentService.getById(any(UUID.class))).thenReturn(sample());
        when(equipmentService.updateStatus(any(UUID.class), any(), any(UUID.class))).thenReturn(sample());

        mockMvc.perform(patch("/api/v1/equipment/{id}/status", EQUIP_ID)
                        .with(user(toUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── 14. RETIRE ───────────────────────────────────────────────
    @Test @DisplayName("DELETE /equipment/{id}?reason=... → 200")
    void retireEquipment_Success() throws Exception {
        EquipmentResponse retired = EquipmentResponse.builder()
                .equipmentId(EQUIP_ID.toString()).retired(true).build();

        when(equipmentService.getById(any(UUID.class))).thenReturn(sample());
        when(equipmentService.retireEquipment(any(UUID.class), anyString(), any(UUID.class)))
                .thenReturn(retired);

        mockMvc.perform(delete("/api/v1/equipment/{id}", EQUIP_ID)
                        .param("reason", "End of life")
                        .with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.retired").value(true));
    }

    // ── 15. DEPT STATS ───────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/stats → 200")
    void getDeptStats_Success() throws Exception {
        when(equipmentService.getDepartmentStats(any(UUID.class))).thenReturn(Map.of("total", 5));

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/stats", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk());
    }

    // ── 16. RETIRED LIST ─────────────────────────────────────────
    @Test @DisplayName("GET /equipment/department/{deptId}/retired → 200")
    void getRetired_Success() throws Exception {
        when(equipmentService.getRetiredByDepartment(any(UUID.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/equipment/department/{deptId}/retired", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    // ── 17. MY DEPT ──────────────────────────────────────────────
    @Test @DisplayName("GET /equipment/my-department → 200")
    void getMyDeptEquipment_Success() throws Exception {
        when(equipmentService.getByDepartment(any(UUID.class), anyBoolean()))
                .thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/equipment/my-department").with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 18. MY DEPT STATS ────────────────────────────────────────
    @Test @DisplayName("GET /equipment/my-department/stats → 200")
    void getMyDeptStats_Success() throws Exception {
        when(equipmentService.getDepartmentStats(any(UUID.class))).thenReturn(Map.of("total", 3));

        mockMvc.perform(get("/api/v1/equipment/my-department/stats").with(user(toUser)))
                .andExpect(status().isOk());
    }

    // ── 19. MY DEPT MAINTENANCE DUE ──────────────────────────────
    @Test @DisplayName("GET /equipment/my-department/maintenance-due → 200")
    void getMyMaintenanceDue_Success() throws Exception {
        when(equipmentService.getMaintenanceDue(any(UUID.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/equipment/my-department/maintenance-due").with(user(toUser)))
                .andExpect(status().isOk());
    }
}
