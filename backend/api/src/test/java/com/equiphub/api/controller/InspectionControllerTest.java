package com.equiphub.api.controller;

import com.equiphub.api.dto.inspection.IssueEquipmentDTO;
import com.equiphub.api.dto.inspection.ReturnInspectionDTO;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.dto.inspection.InspectionResponseDTO;
import com.equiphub.api.dto.inspection.InspectionSummaryDTO;
import com.equiphub.api.service.InspectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InspectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InspectionControllerTest extends BaseControllerTest{
 
    @Autowired private MockMvc mockMvc;
    @MockBean  private InspectionService inspectionService;
    @Autowired private ObjectMapper objectMapper;

    private UUID testId;

    @BeforeEach
    void setUp() { testId = UUID.randomUUID(); }

    // ─── ISSUE EQUIPMENT ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/inspection/issue - issues equipment successfully")
    void issueEquipment_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> response = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.issueEquipment(any(IssueEquipmentDTO.class), any(UUID.class)))
            .thenReturn(response);

        Map<String, Object> body = Map.of(
            "requestId", testId.toString(),
            "issuanceNotes", "Pre-issue inspection passed"
        );

        mockMvc.perform(post("/api/inspection/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/inspection/issue - service throws IllegalArgument returns 400")
    void issueEquipment_Error_ShouldReturn400() throws Exception {
        when(inspectionService.issueEquipment(any(IssueEquipmentDTO.class), any(UUID.class)))
            .thenThrow(new IllegalArgumentException("Request not found"));

        mockMvc.perform(post("/api/inspection/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ─── PROCESS RETURN ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/inspection/return - records return successfully")
    void processReturn_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> response = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.processReturn(any(ReturnInspectionDTO.class), any(UUID.class)))
            .thenReturn(response);

        Map<String, Object> body = Map.of(
            "requestId", testId.toString(),
            "conditionAfter", "GOOD"
        );

        mockMvc.perform(post("/api/inspection/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/inspection/return - with damage noted")
    void processReturn_WithDamage_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> response = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.processReturn(any(ReturnInspectionDTO.class), any(UUID.class)))
            .thenReturn(response);

        Map<String, Object> body = new HashMap<>();
        body.put("requestId", testId.toString());
        body.put("conditionAfter", "DAMAGED");
        body.put("damageSeverity", "MINOR");
        body.put("damageNotes", "Screen cracked");

        mockMvc.perform(post("/api/inspection/return")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk());
    }

    // ─── ACKNOWLEDGE INSPECTION ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/inspection/{id}/acknowledge - student acknowledges damage")
    void acknowledgeInspection_ShouldReturn200() throws Exception {
        InspectionResponseDTO response = mock(InspectionResponseDTO.class);
        when(inspectionService.acknowledgeInspection(anyInt(), any(UUID.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/inspection/1/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/inspection/{id}/acknowledge - not found returns 400")
    void acknowledgeInspection_NotFound_ShouldReturn400() throws Exception {
        when(inspectionService.acknowledgeInspection(anyInt(), any(UUID.class)))
            .thenThrow(new IllegalArgumentException("Inspection not found"));

        mockMvc.perform(post("/api/inspection/999/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ─── UNACKNOWLEDGED DAMAGE ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inspection/damage/unacknowledged - returns damage list")
    void getUnacknowledgedDamage_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> result = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.getUnacknowledgedDamage()).thenReturn(result);

        mockMvc.perform(get("/api/inspection/damage/unacknowledged"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/inspection/damage/unacknowledged - empty list returns 200")
    void getUnacknowledgedDamage_Empty_ShouldReturn200() throws Exception {
        when(inspectionService.getUnacknowledgedDamage()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/inspection/damage/unacknowledged"))
            .andExpect(status().isOk());
    }

    // ─── DAMAGE REPORT ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inspection/damage/report/{deptId}?days=30 - returns report")
    void getDamageReport_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> report = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.getDamageReport(any(UUID.class), anyInt()))
            .thenReturn(report);

        mockMvc.perform(get("/api/inspection/damage/report/" + testId)
                .param("days", "30"))
            .andExpect(status().isOk());
    }

    // ─── DEPARTMENT INSPECTION STATS ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inspection/stats/{deptId} - returns summary DTO")
    void getDepartmentInspectionStats_ShouldReturn200() throws Exception {
        InspectionSummaryDTO summary = mock(InspectionSummaryDTO.class);
        when(inspectionService.getDepartmentInspectionStats(any(UUID.class)))
            .thenReturn(summary);

        mockMvc.perform(get("/api/inspection/stats/" + testId))
            .andExpect(status().isOk());
    }

    // ─── INSPECTIONS BY REQUEST ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/inspection/request/{requestId} - returns inspections for request")
    void getInspectionsByRequest_ShouldReturn200() throws Exception {
        List<InspectionResponseDTO> result = List.of(mock(InspectionResponseDTO.class));
        when(inspectionService.getInspectionsByRequest(anyString()))
            .thenReturn(result);

        mockMvc.perform(get("/api/inspection/request/" + testId))
            .andExpect(status().isOk());
    }
}
