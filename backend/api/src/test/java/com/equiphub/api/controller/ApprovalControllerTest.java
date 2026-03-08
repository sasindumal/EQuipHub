package com.equiphub.api.controller;

import com.equiphub.api.dto.approval.*;
import com.equiphub.api.model.Request;
import com.equiphub.api.model.RequestApproval;
import com.equiphub.api.model.RequestApproval.ApprovalDecision;
import com.equiphub.api.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovalController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalController Tests")
class ApprovalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Mock  private ApprovalService approvalService;

    private static final String LECTURER_UUID = "00000000-0000-0000-0000-000000000002";
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
    }

    // ── AUTO-APPROVAL ───────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/approvals/{id}/auto-approve — auto-approve coursework")
    @WithMockUser(username = LECTURER_UUID, roles = "TECHNICALOFFICER")
    void autoApprove_Success() throws Exception {
        AutoApprovalResultDTO result = AutoApprovalResultDTO.builder()
                .autoApproved(true)
                .requestId("REQ-2026-00001")
                .conditionChecks(List.of(
                        new AutoApprovalResultDTO.ConditionCheck("Equipment available", true, "All clear")))
                .build();

        when(approvalService.attemptAutoApproval("REQ-2026-00001")).thenReturn(result);

        mockMvc.perform(post("/api/v1/approvals/{id}/auto-approve", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoApproved").value(true));
    }

    // ── PROCESS DECISION ────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/approvals/{id}/stages/{stage}/decide — approve")
    @WithMockUser(username = LECTURER_UUID, roles = "LECTURER")
    void processDecision_Approve() throws Exception {
        ApprovalDecisionDTO dto = ApprovalDecisionDTO.builder()
                .action(RequestApproval.ApprovalAction.APPROVE)
                .reason("Looks good")
                .build();

        ApprovalResponseDTO response = ApprovalResponseDTO.builder()
                .approvalId(1)
                .requestId("REQ-2026-00001")
                .decision(ApprovalDecision.APPROVED)
                .actorName("Dr. Smith")
                .build();

        when(approvalService.processDecision(anyString(), any(), any(), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/approvals/{id}/stages/{stage}/decide",
                        "REQ-2026-00001", "LECTURERAPPROVAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/approvals/{id}/stages/{stage}/decide — reject")
    @WithMockUser(username = LECTURER_UUID, roles = "LECTURER")
    void processDecision_Reject() throws Exception {
        ApprovalDecisionDTO dto = ApprovalDecisionDTO.builder()
                .action(RequestApproval.ApprovalAction.REJECT)
                .reason("Not appropriate equipment")
                .build();

        ApprovalResponseDTO response = ApprovalResponseDTO.builder()
                .approvalId(2)
                .requestId("REQ-2026-00001")
                .decision(ApprovalDecision.REJECTED)
                .build();

        when(approvalService.processDecision(anyString(), any(), any(), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/approvals/{id}/stages/{stage}/decide",
                        "REQ-2026-00001", "LECTURERAPPROVAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("REJECTED"));
    }

    // ── MY APPROVAL QUEUE ───────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/approvals/my-queue")
    @WithMockUser(username = LECTURER_UUID, roles = "LECTURER")
    void getMyQueue_Success() throws Exception {
        ApprovalQueueItemDTO item = ApprovalQueueItemDTO.builder()
                .requestId("REQ-2026-00001")
                .requestType(Request.RequestType.COURSEWORK)
                .studentName("John Doe")
                .pendingStage("LECTURERAPPROVAL")
                .build();

        when(approvalService.getMyApprovalQueue(any(UUID.class))).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/approvals/my-queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value("REQ-2026-00001"));
    }

    // ── DEPARTMENT QUEUE ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/approvals/departments/{deptId}/queue")
    @WithMockUser(username = LECTURER_UUID, roles = "HEADOFDEPARTMENT")
    void getDepartmentQueue_Success() throws Exception {
        when(approvalService.getDepartmentApprovalQueue(departmentId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/approvals/departments/{deptId}/queue", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── APPROVAL HISTORY ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/approvals/{id}/history")
    void getApprovalHistory_Success() throws Exception {
        ApprovalResponseDTO resp = ApprovalResponseDTO.builder()
                .approvalId(1)
                .requestId("REQ-2026-00001")
                .decision(ApprovalDecision.APPROVED)
                .build();

        when(approvalService.getApprovalHistory("REQ-2026-00001")).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/approvals/{id}/history", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].decision").value("APPROVED"));
    }

    // ── DEPARTMENT STATS ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/approvals/departments/{deptId}/stats")
    @WithMockUser(username = LECTURER_UUID, roles = "HEADOFDEPARTMENT")
    void getDepartmentStats_Success() throws Exception {
        ApprovalStatsDTO stats = ApprovalStatsDTO.builder()
                .totalPending(5)
                .totalApproved(20)
                .totalRejected(3)
                .slaBreached(1)
                .emergencyPending(2)
                .pendingByStage(Map.of("LECTURERAPPROVAL", 3L))
                .build();

        when(approvalService.getDepartmentApprovalStats(departmentId)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/approvals/departments/{deptId}/stats", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPending").value(5));
    }
}