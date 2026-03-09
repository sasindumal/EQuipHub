package com.equiphub.api.controller;

import com.equiphub.api.dto.approval.*;
import com.equiphub.api.model.Request;
import com.equiphub.api.model.RequestApproval;
import com.equiphub.api.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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

@WebMvcTest(ApprovalController.class)
@DisplayName("ApprovalController Tests")
class ApprovalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ApprovalService approvalService;

    private static final String REQUEST_ID = "REQ-2026-00001";
    private static final UUID   DEPT_ID    = UUID.randomUUID();
    private static final UUID   USER_UUID  = UUID.randomUUID();

    @Test
    @DisplayName("POST /api/v1/approvals/requests/{id}/auto-approve — TO → 200")
    @WithMockUser(username = "2021E001@eng.jfn.ac.lk", roles = "TECHNICALOFFICER")
    void autoApprove_Returns200() throws Exception {
        AutoApprovalResultDTO result = new AutoApprovalResultDTO();
        when(approvalService.attemptAutoApproval(REQUEST_ID)).thenReturn(result);

        mockMvc.perform(post("/api/v1/approvals/requests/{id}/auto-approve", REQUEST_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/approvals/requests/{id}/auto-approve — STUDENT → 403")
    @WithMockUser(roles = "STUDENT")
    void autoApprove_AsStudent_Returns403() throws Exception {
        mockMvc.perform(post("/api/v1/approvals/requests/{id}/auto-approve", REQUEST_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/approvals/requests/{id}/decide — LECTURER → 200")
    @WithMockUser(username = "2021E001@eng.jfn.ac.lk", roles = "LECTURER")
    void processDecision_Returns200() throws Exception {
        ApprovalDecisionDTO dto = new ApprovalDecisionDTO();
        dto.setAction(RequestApproval.ApprovalAction.APPROVE);
        dto.setComments("Looks good");

        ApprovalResponseDTO response = new ApprovalResponseDTO();
        when(approvalService.processDecision(anyString(),
                any(RequestApproval.ApprovalStage.class),
                any(ApprovalDecisionDTO.class),
                any(UUID.class)))
             .thenReturn(response);

        mockMvc.perform(post("/api/v1/approvals/requests/{id}/decide", REQUEST_ID)
                        .param("stage", "LECTURERAPPROVAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/approvals/my-queue — LECTURER → 200")
    @WithMockUser(username = "2021E001@eng.jfn.ac.lk", roles = "LECTURER")
    void getMyQueue_Returns200() throws Exception {
        when(approvalService.getMyApprovalQueue(any(UUID.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/approvals/my-queue"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/approvals/departments/{id}/queue — DEPARTMENTADMIN → 200")
    @WithMockUser(roles = "DEPARTMENTADMIN")
    void getDepartmentQueue_Returns200() throws Exception {
        when(approvalService.getDepartmentApprovalQueue(DEPT_ID))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/approvals/departments/{id}/queue", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/approvals/requests/{id}/history — STUDENT → 200")
    @WithMockUser(username = "2021E001@eng.jfn.ac.lk", roles = "STUDENT")
    void getApprovalHistory_Returns200() throws Exception {
        when(approvalService.getApprovalHistory(REQUEST_ID))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/approvals/requests/{id}/history", REQUEST_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/approvals/departments/{id}/stats — HOD → 200")
    @WithMockUser(roles = "HEADOFDEPARTMENT")
    void getDepartmentStats_Returns200() throws Exception {
        ApprovalStatsDTO stats = new ApprovalStatsDTO();
        when(approvalService.getDepartmentApprovalStats(DEPT_ID)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/approvals/departments/{id}/stats", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/approvals/requests/{id}/next-stage — TO → 200")
    @WithMockUser(username = "2021E001@eng.jfn.ac.lk", roles = "TECHNICALOFFICER")
    void getNextStage_Returns200() throws Exception {
        Request req = new Request();
        when(approvalService.findRequestPublic(REQUEST_ID)).thenReturn(req);
        when(approvalService.determineNextStage(any(Request.class)))
                .thenReturn(RequestApproval.ApprovalStage.LECTURERAPPROVAL);

        mockMvc.perform(get("/api/v1/approvals/requests/{id}/next-stage", REQUEST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStage").value("LECTURERAPPROVAL"));
    }
}
