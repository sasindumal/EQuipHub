package com.equiphub.api.controller;

import com.equiphub.api.dto.inspection.*;
import com.equiphub.api.service.InspectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.bean.MockBean;
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
@DisplayName("InspectionController Tests")
class InspectionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private InspectionService inspectionService;

    private static final String TO_UUID = "00000000-0000-0000-0000-000000000003";
    private static final String STUDENT_UUID = "00000000-0000-0000-0000-000000000001";
    private UUID departmentId;
    private InspectionResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        sampleResponse = InspectionResponseDTO.builder()
                .inspectionId(1)
                .requestId("REQ-2026-00001")
                .requestItemId(1)
                .inspectionType(com.equiphub.api.model.InspectionType.PREISSUANCE)
                .inspectionTypeName("Pre-Issuance Inspection")
                .inspectorName("Tech Officer")
                .equipmentName("Oscilloscope")
                .conditionBefore(95)
                .conditionBeforeLabel("EXCELLENT")
                .penaltyApplicable(false)
                .studentAcknowledged(false)
                .inspectedAt(LocalDateTime.now())
                .build();
    }

    // ── ISSUE EQUIPMENT ─────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/inspections/issue — issue equipment")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void issueEquipment_Success() throws Exception {
        IssueEquipmentDTO dto = IssueEquipmentDTO.builder()
                .requestId("REQ-2026-00001")
                .items(List.of(IssueEquipmentDTO.ItemIssue.builder()
                        .requestItemId(1)
                        .quantityToIssue(1)
                        .conditionBefore(95)
                        .notes("Good condition")
                        .build()))
                .build();

        when(inspectionService.issueEquipment(any(), any(UUID.class)))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(post("/api/v1/inspections/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipmentName").value("Oscilloscope"));
    }

    // ── PROCESS RETURN ──────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/inspections/return — return equipment")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void processReturn_Success() throws Exception {
        ReturnInspectionDTO dto = ReturnInspectionDTO.builder()
                .requestId("REQ-2026-00001")
                .items(List.of(ReturnInspectionDTO.ItemReturnInspection.builder()
                        .requestItemId(1)
                        .quantityReturned(1)
                        .conditionAfter(90)
                        .damageLevel(0)
                        .build()))
                .build();

        InspectionResponseDTO returnResp = sampleResponse.toBuilder()
                .inspectionType(com.equiphub.api.model.InspectionType.POSTRETURN)
                .conditionAfter(90)
                .conditionDelta(5)
                .build();

        when(inspectionService.processReturn(any(), any(UUID.class)))
                .thenReturn(List.of(returnResp));

        mockMvc.perform(post("/api/v1/inspections/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conditionAfter").value(90));
    }

    // ── STUDENT ACKNOWLEDGE ─────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/inspections/{id}/acknowledge — student acknowledges")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void acknowledgeInspection_Success() throws Exception {
        sampleResponse.setStudentAcknowledged(true);
        when(inspectionService.acknowledgeInspection(eq(1), any(UUID.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/inspections/{id}/acknowledge", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentAcknowledged").value(true));
    }

    // ── GET BY REQUEST ──────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/inspections/requests/{reqId}")
    void getByRequest_Success() throws Exception {
        when(inspectionService.getInspectionsByRequest("REQ-2026-00001"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/inspections/requests/{reqId}", "REQ-2026-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inspectionId").value(1));
    }

    // ── MY INSPECTIONS (TO dashboard) ───────────────────────────
    @Test
    @DisplayName("GET /api/v1/inspections/my — TO dashboard")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void getMyInspections_Success() throws Exception {
        when(inspectionService.getMyInspections(any(UUID.class)))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/inspections/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── DAMAGE REPORT ───────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/inspections/departments/{deptId}/damage-report")
    void getDamageReport_Success() throws Exception {
        when(inspectionService.getDamageReport(departmentId, 30))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/inspections/departments/{deptId}/damage-report", departmentId)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── UNACKNOWLEDGED DAMAGE ───────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/inspections/unacknowledged-damage")
    void getUnacknowledgedDamage_Success() throws Exception {
        when(inspectionService.getUnacknowledgedDamage()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/inspections/unacknowledged-damage"))
                .andExpect(status().isOk());
    }

    // ── DEPARTMENT STATS ────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/inspections/departments/{deptId}/stats")
    void getDepartmentStats_Success() throws Exception {
        InspectionSummaryDTO stats = InspectionSummaryDTO.builder()
                .totalInspections(100)
                .preIssuanceCount(50)
                .postReturnCount(50)
                .damageDetectedCount(5)
                .penaltiesTriggered(3)
                .averageConditionBefore(92.5)
                .averageConditionAfter(87.3)
                .averageConditionDelta(5.2)
                .build();

        when(inspectionService.getDepartmentInspectionStats(departmentId)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/inspections/departments/{deptId}/stats", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInspections").value(100));
    }
}