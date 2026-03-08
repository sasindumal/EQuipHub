package com.equiphub.api.controller;

import com.equiphub.api.dto.request.*;
import com.equiphub.api.model.Request;
import com.equiphub.api.service.RequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RequestController Tests")
class RequestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private RequestService requestService;

    private static final String STUDENT_UUID = "00000000-0000-0000-0000-000000000001";
    private UUID departmentId;
    private RequestResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        sampleResponse = RequestResponseDTO.builder()
                .requestId("REQ-2026-00001")
                .requestType(Request.RequestType.COURSEWORK)
                .status(Request.RequestStatus.DRAFT)
                .studentName("John Doe")
                .items(Collections.emptyList())
                .approvals(Collections.emptyList())
                .build();
    }

    // ── CREATE ──────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/requests — create request")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void createRequest_Success() throws Exception {
        CreateRequestDTO dto = CreateRequestDTO.builder()
                .requestType(Request.RequestType.COURSEWORK)
                .studentId(UUID.fromString(STUDENT_UUID))
                .departmentId(departmentId)
                .fromDateTime(LocalDateTime.now().plusDays(1))
                .toDateTime(LocalDateTime.now().plusDays(3))
                .description("Lab experiment")
                .priorityLevel(2)
                .slaHours(24)
                .courseId(UUID.randomUUID())
                .items(List.of(RequestItemDTO.builder()
                        .equipmentId(UUID.randomUUID())
                        .quantityRequested(1)
                        .build()))
                .build();

        when(requestService.createRequest(any(), any(UUID.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value("REQ-2026-00001"));
    }

    // ── SUBMIT ──────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/requests/{id}/submit — submit request")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void submitRequest_Success() throws Exception {
        sampleResponse.setStatus(Request.RequestStatus.PENDINGRECOMMENDATION);
        when(requestService.submitRequest(anyString(), any(UUID.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/requests/{id}/submit", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDINGRECOMMENDATION"));
    }

    // ── GET BY ID ───────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/{id}")
    void getById_Success() throws Exception {
        when(requestService.getRequestById("REQ-2026-00001")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/requests/{id}", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ-2026-00001"));
    }

    // ── GET MY REQUESTS ─────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/my — paginated")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void getMyRequests_Success() throws Exception {
        Page<RequestResponseDTO> page = new PageImpl<>(List.of(sampleResponse));
        when(requestService.getMyRequests(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/requests/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].requestId").value("REQ-2026-00001"));
    }

    // ── GET DEPARTMENT REQUESTS ─────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/departments/{deptId}")
    void getDepartmentRequests_Success() throws Exception {
        Page<RequestResponseDTO> page = new PageImpl<>(List.of(sampleResponse));
        when(requestService.getDepartmentRequests(any(UUID.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/requests/departments/{deptId}", departmentId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    // ── UPDATE (DRAFT only) ─────────────────────────────────────
    @Test
    @DisplayName("PUT /api/v1/requests/{id} — update draft")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void updateRequest_Success() throws Exception {
        UpdateRequestDTO dto = UpdateRequestDTO.builder()
                .description("Updated description")
                .build();

        when(requestService.updateRequest(anyString(), any(), any(UUID.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/requests/{id}", "REQ-2026-00001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ── CANCEL ──────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/requests/{id}/cancel — cancel request")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void cancelRequest_Success() throws Exception {
        sampleResponse.setStatus(Request.RequestStatus.CANCELLED);
        when(requestService.cancelRequest(anyString(), any(UUID.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/requests/{id}/cancel", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ── EMERGENCY REQUESTS ──────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/departments/{deptId}/emergency")
    void getEmergencyRequests_Success() throws Exception {
        when(requestService.getEmergencyRequests(departmentId)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/requests/departments/{deptId}/emergency", departmentId))
                .andExpect(status().isOk());
    }

    // ── SLA BREACHED ────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/sla-breached")
    void getSlaBreached_Success() throws Exception {
        when(requestService.getSlaBreachedRequests()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/requests/sla-breached"))
                .andExpect(status().isOk());
    }

    // ── DEPARTMENT STATS ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/requests/departments/{deptId}/stats")
    void getDepartmentStats_Success() throws Exception {
        Map<String, Object> stats = Map.of("totalRequests", 42L);
        when(requestService.getDepartmentRequestStats(departmentId)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/requests/departments/{deptId}/stats", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(42));
    }
}