package com.equiphub.api.controller;

import com.equiphub.api.dto.request.*;
import com.equiphub.api.model.Request;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest extends BaseControllerTest{

        @MockBean JwtUtils jwtUtils;    
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean RequestService requestService;
    @MockBean CustomUserDetailsService userDetailsService;

    static final UUID STUDENT_ID = UUID.randomUUID();
    static final UUID TO_ID      = UUID.randomUUID();
    static final UUID DEPT_ID    = UUID.randomUUID();
    static final String REQ_ID   = "REQ-2026-00001";

    CustomUserDetails studentUser;
    CustomUserDetails toUser;

    @BeforeEach
    void setUp() {
        studentUser = mock(CustomUserDetails.class);
        when(studentUser.getUserId()).thenReturn(STUDENT_ID);
        when(studentUser.getRole()).thenReturn(User.Role.STUDENT);
        when(studentUser.getDepartmentId()).thenReturn(DEPT_ID.toString());
        when(studentUser.getUsername()).thenReturn(STUDENT_ID.toString());
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))).when(studentUser).getAuthorities();
        when(studentUser.isEnabled()).thenReturn(true);
        when(studentUser.isAccountNonExpired()).thenReturn(true);
        when(studentUser.isAccountNonLocked()).thenReturn(true);
        when(studentUser.isCredentialsNonExpired()).thenReturn(true);
        when(studentUser.isAdmin()).thenReturn(false);

        toUser = mock(CustomUserDetails.class);
        when(toUser.getUserId()).thenReturn(TO_ID);
        when(toUser.getRole()).thenReturn(User.Role.TECHNICALOFFICER);
        when(toUser.getDepartmentId()).thenReturn(DEPT_ID.toString());
        when(toUser.getUsername()).thenReturn(TO_ID.toString());
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_TECHNICALOFFICER"))).when(toUser).getAuthorities();
        when(toUser.isEnabled()).thenReturn(true);
        when(toUser.isAccountNonExpired()).thenReturn(true);
        when(toUser.isAccountNonLocked()).thenReturn(true);
        when(toUser.isCredentialsNonExpired()).thenReturn(true);
        when(toUser.isAdmin()).thenReturn(false);
    }

        RequestResponseDTO sample() {
        return RequestResponseDTO.builder()
                .requestId(REQ_ID)
                .studentId(STUDENT_ID)
                .departmentId(DEPT_ID)
                .status(Request.RequestStatus.DRAFT)
                .build();
        }

    // ── 1. CREATE ────────────────────────────────────────────────
    @Test @DisplayName("POST /requests — student for self → 201")
    void create_StudentForSelf_201() throws Exception {
        CreateRequestDTO req = new CreateRequestDTO();
        req.setStudentId(STUDENT_ID);
        req.setDepartmentId(DEPT_ID);
        when(requestService.createRequest(any(), any(UUID.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/requests")
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test @DisplayName("POST /requests — student for other → 403")
    void create_StudentForOther_403() throws Exception {
        CreateRequestDTO req = new CreateRequestDTO();
        req.setStudentId(UUID.randomUUID());
        req.setDepartmentId(DEPT_ID);

        mockMvc.perform(post("/api/v1/requests")
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /requests — TO for wrong dept → 403")
    void create_TOWrongDept_403() throws Exception {
        CreateRequestDTO req = new CreateRequestDTO();
        req.setStudentId(UUID.randomUUID());
        req.setDepartmentId(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/requests")
                        .with(user(toUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── 2. SUBMIT ────────────────────────────────────────────────
    @Test @DisplayName("POST /requests/{id}/submit → 200")
    void submit_200() throws Exception {
        RequestResponseDTO submitted = sample();
        submitted.setStatus(Request.RequestStatus.PENDINGAPPROVAL);
        when(requestService.submitRequest(anyString(), any(UUID.class))).thenReturn(submitted);

        mockMvc.perform(post("/api/v1/requests/{id}/submit", REQ_ID).with(user(studentUser)))
                .andExpect(status().isOk());
    }

    // ── 3. GET BY ID ─────────────────────────────────────────────
    @Test @DisplayName("GET /requests/{id} — student views own → 200")
    void getById_Own_200() throws Exception {
        when(requestService.getRequestById(anyString())).thenReturn(sample());

        mockMvc.perform(get("/api/v1/requests/{id}", REQ_ID).with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(REQ_ID));
    }

    @Test @DisplayName("GET /requests/{id} — student views other's → 403")
    void getById_Other_403() throws Exception {
        RequestResponseDTO other = sample();
        other.setStudentId(UUID.randomUUID());
        when(requestService.getRequestById(anyString())).thenReturn(other);

        mockMvc.perform(get("/api/v1/requests/{id}", REQ_ID).with(user(studentUser)))
                .andExpect(status().isForbidden());
    }

    // ── 4. MY REQUESTS ───────────────────────────────────────────
    @Test @DisplayName("GET /requests/my → 200 paginated")
    void getMyRequests_200() throws Exception {
        when(requestService.getMyRequests(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/requests/my").with(user(studentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // ── 5. DEPT REQUESTS ─────────────────────────────────────────
    @Test @DisplayName("GET /requests/department/{deptId} — own dept → 200")
    void getDeptRequests_Own_200() throws Exception {
        when(requestService.getDepartmentRequests(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/requests/department/{deptId}", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /requests/department/{deptId} — wrong dept → 403")
    void getDeptRequests_WrongDept_403() throws Exception {
        mockMvc.perform(get("/api/v1/requests/department/{deptId}", UUID.randomUUID()).with(user(toUser)))
                .andExpect(status().isForbidden());
    }

    // ── 6. BY STATUS ─────────────────────────────────────────────
    @Test @DisplayName("GET /requests/status/DRAFT → 200")
    void getByStatus_200() throws Exception {
        when(requestService.getRequestsByStatus(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/requests/status/DRAFT").with(user(toUser)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /requests/status/GARBAGE → 400")
    void getByStatus_Invalid_400() throws Exception {
        mockMvc.perform(get("/api/v1/requests/status/GARBAGE").with(user(toUser)))
                .andExpect(status().isBadRequest());
    }

    // ── 7. UPDATE ────────────────────────────────────────────────
    @Test @DisplayName("PUT /requests/{id} — own draft → 200")
    void update_OwnDraft_200() throws Exception {
        UpdateRequestDTO body = new UpdateRequestDTO();
        when(requestService.getRequestById(anyString())).thenReturn(sample());
        when(requestService.updateRequest(anyString(), any(), any(UUID.class))).thenReturn(sample());

        mockMvc.perform(put("/api/v1/requests/{id}", REQ_ID)
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /requests/{id} — other's request → 403")
    void update_Other_403() throws Exception {
        RequestResponseDTO other = sample();
        other.setStudentId(UUID.randomUUID());
        when(requestService.getRequestById(anyString())).thenReturn(other);

        mockMvc.perform(put("/api/v1/requests/{id}", REQ_ID)
                        .with(user(studentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateRequestDTO())))
                .andExpect(status().isForbidden());
    }

    // ── 8. CANCEL ────────────────────────────────────────────────
    @Test @DisplayName("POST /requests/{id}/cancel — own → 200")
    void cancel_Own_200() throws Exception {
        RequestResponseDTO cancelled = sample();
        cancelled.setStatus(Request.RequestStatus.CANCELLED);
        when(requestService.getRequestById(anyString())).thenReturn(sample());
        when(requestService.cancelRequest(anyString(), any(UUID.class))).thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/requests/{id}/cancel", REQ_ID).with(user(studentUser)))
                .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /requests/{id}/cancel — other's → 403")
    void cancel_Other_403() throws Exception {
        RequestResponseDTO other = sample();
        other.setStudentId(UUID.randomUUID());
        when(requestService.getRequestById(anyString())).thenReturn(other);

        mockMvc.perform(post("/api/v1/requests/{id}/cancel", REQ_ID).with(user(studentUser)))
                .andExpect(status().isForbidden());
    }

    // ── 9. EMERGENCY ─────────────────────────────────────────────
    @Test @DisplayName("GET /requests/department/{deptId}/emergency — own → 200")
    void getEmergency_Own_200() throws Exception {
        when(requestService.getEmergencyRequests(any(UUID.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/requests/department/{deptId}/emergency", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test @DisplayName("GET /requests/department/{deptId}/emergency — wrong dept → 403")
    void getEmergency_WrongDept_403() throws Exception {
        mockMvc.perform(get("/api/v1/requests/department/{deptId}/emergency", UUID.randomUUID())
                        .with(user(toUser)))
                .andExpect(status().isForbidden());
    }

    // ── 10. SLA BREACHED ─────────────────────────────────────────
    @Test @DisplayName("GET /requests/sla-breached → 200")
    void getSlaBreached_200() throws Exception {
        when(requestService.getSlaBreachedRequests()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/requests/sla-breached").with(user(toUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));
    }

    // ── 11. DEPT PENDING ─────────────────────────────────────────
        @Test @DisplayName("GET /requests/department/{deptId}/pending → 200")
        void getDeptPending_200() throws Exception {
        when(requestService.getDepartmentRequests(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        mockMvc.perform(get("/api/v1/requests/department/{deptId}/pending", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk());
        }
    // ── 12. REQUEST STATS ────────────────────────────────────────
    @Test @DisplayName("GET /requests/department/{deptId}/stats → 200")
    void getDeptStats_200() throws Exception {
        when(requestService.getDepartmentRequestStats(any(UUID.class)))
                .thenReturn(Map.of("total", 5, "pending", 2));

        mockMvc.perform(get("/api/v1/requests/department/{deptId}/stats", DEPT_ID).with(user(toUser)))
                .andExpect(status().isOk());
    }
}
