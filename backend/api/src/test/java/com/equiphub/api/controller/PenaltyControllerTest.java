package com.equiphub.api.controller;

import com.equiphub.api.dto.penalty.*;
import com.equiphub.api.model.Penalty;
import com.equiphub.api.service.PenaltyService;
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
@DisplayName("PenaltyController Tests")
class PenaltyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private PenaltyService penaltyService;

    private static final String TO_UUID = "00000000-0000-0000-0000-000000000003";
    private static final String STUDENT_UUID = "00000000-0000-0000-0000-000000000001";
    private static final String HOD_UUID = "00000000-0000-0000-0000-000000000004";
    private UUID studentId;
    private UUID departmentId;
    private PenaltyResponseDTO samplePenalty;

    @BeforeEach
    void setUp() {
        studentId = UUID.fromString(STUDENT_UUID);
        departmentId = UUID.randomUUID();

        samplePenalty = PenaltyResponseDTO.builder()
                .penaltyId(1)
                .requestId("REQ-2026-00001")
                .studentId(studentId)
                .studentName("John Doe")
                .penaltyType(Penalty.PenaltyType.LATERETURN)
                .points(10)
                .reason("Returned 3 days late")
                .status(Penalty.PenaltyStatus.PENDING)
                .totalPointsAfter(10)
                .statusLevel("YELLOW")
                .appealed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── CREATE PENALTY ──────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/penalties — create penalty (TO)")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void createPenalty_Success() throws Exception {
        CreatePenaltyDTO dto = CreatePenaltyDTO.builder()
                .requestId("REQ-2026-00001")
                .studentId(studentId)
                .penaltyType(Penalty.PenaltyType.LATERETURN)
                .points(10)
                .reason("Returned 3 days late")
                .build();

        when(penaltyService.createPenalty(any(), any(UUID.class))).thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.penaltyId").value(1))
                .andExpect(jsonPath("$.points").value(10));
    }

    // ── APPROVE PENALTY ─────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/penalties/{id}/approve — approve (HOD)")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void approvePenalty_Success() throws Exception {
        samplePenalty.setStatus(Penalty.PenaltyStatus.APPROVED);
        when(penaltyService.approvePenalty(eq(1), any(UUID.class))).thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties/{id}/approve", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // ── WAIVE PENALTY ───────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/penalties/{id}/waive — waive (HOD)")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void waivePenalty_Success() throws Exception {
        samplePenalty.setStatus(Penalty.PenaltyStatus.WAIVED);
        when(penaltyService.waivePenalty(eq(1), any(UUID.class), anyString()))
                .thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties/{id}/waive", 1)
                        .param("reason", "First offence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"));
    }

    // ── GET STUDENT PENALTIES ───────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/students/{studentId}")
    void getStudentPenalties_Success() throws Exception {
        when(penaltyService.getStudentPenalties(studentId)).thenReturn(List.of(samplePenalty));

        mockMvc.perform(get("/api/v1/penalties/students/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].penaltyId").value(1));
    }

    // ── GET STUDENT SUMMARY ─────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/students/{studentId}/summary")
    void getStudentSummary_Success() throws Exception {
        StudentPenaltySummaryDTO summary = StudentPenaltySummaryDTO.builder()
                .studentId(studentId)
                .studentName("John Doe")
                .totalActivePoints(10)
                .currentLevel("YELLOW")
                .totalPenalties(2)
                .lateReturnCount(1)
                .damageCount(1)
                .borrowingRestricted(false)
                .build();

        when(penaltyService.getStudentSummary(studentId)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/penalties/students/{studentId}/summary", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value("YELLOW"))
                .andExpect(jsonPath("$.borrowingRestricted").value(false));
    }

    // ── CAN BORROW ──────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/students/{studentId}/can-borrow")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void canBorrow_Allowed() throws Exception {
        when(penaltyService.canStudentBorrow(studentId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/penalties/students/{studentId}/can-borrow", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/penalties/students/{studentId}/can-borrow — restricted")
    @WithMockUser(username = TO_UUID, roles = "TECHNICALOFFICER")
    void canBorrow_Restricted() throws Exception {
        when(penaltyService.canStudentBorrow(studentId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/penalties/students/{studentId}/can-borrow", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ── GET DEPARTMENT PENALTIES ─────────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/departments/{deptId}")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void getDepartmentPenalties_Success() throws Exception {
        when(penaltyService.getDepartmentPenalties(departmentId)).thenReturn(List.of(samplePenalty));

        mockMvc.perform(get("/api/v1/penalties/departments/{deptId}", departmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── SUBMIT APPEAL ───────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/penalties/appeals — student appeals")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void submitAppeal_Success() throws Exception {
        AppealRequestDTO dto = AppealRequestDTO.builder()
                .penaltyId(1)
                .appealReason("I returned it on time, log is wrong")
                .build();

        samplePenalty.setStatus(Penalty.PenaltyStatus.APPEALED);
        samplePenalty.setAppealed(true);
        when(penaltyService.submitAppeal(any(), any(UUID.class))).thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties/appeals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPEALED"))
                .andExpect(jsonPath("$.appealed").value(true));
    }

    // ── DECIDE APPEAL ───────────────────────────────────────────
    @Test
    @DisplayName("POST /api/v1/penalties/appeals/{penaltyId}/decide — approve appeal")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void decideAppeal_Approved() throws Exception {
        AppealDecisionDTO dto = AppealDecisionDTO.builder()
                .decision(com.equiphub.api.model.PenaltyAppeal.AppealDecision.APPROVED)
                .decisionReason("Evidence supports the claim")
                .build();

        samplePenalty.setStatus(Penalty.PenaltyStatus.WAIVED);
        when(penaltyService.decideAppeal(eq(1), any(), any(UUID.class))).thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties/appeals/{penaltyId}/decide", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAIVED"));
    }

    @Test
    @DisplayName("POST /api/v1/penalties/appeals/{penaltyId}/decide — reject appeal")
    @WithMockUser(username = HOD_UUID, roles = "HEADOFDEPARTMENT")
    void decideAppeal_Rejected() throws Exception {
        AppealDecisionDTO dto = AppealDecisionDTO.builder()
                .decision(com.equiphub.api.model.PenaltyAppeal.AppealDecision.REJECTED)
                .decisionReason("No supporting evidence")
                .build();

        samplePenalty.setStatus(Penalty.PenaltyStatus.APPROVED);
        when(penaltyService.decideAppeal(eq(1), any(), any(UUID.class))).thenReturn(samplePenalty);

        mockMvc.perform(post("/api/v1/penalties/appeals/{penaltyId}/decide", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // ── MY PENALTIES (student self) ─────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/my — my penalties")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void getMyPenalties_Success() throws Exception {
        when(penaltyService.getStudentPenalties(any(UUID.class)))
                .thenReturn(List.of(samplePenalty));

        mockMvc.perform(get("/api/v1/penalties/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("John Doe"));
    }

    // ── MY SUMMARY (student self) ───────────────────────────────
    @Test
    @DisplayName("GET /api/v1/penalties/my/summary")
    @WithMockUser(username = STUDENT_UUID, roles = "STUDENT")
    void getMySummary_Success() throws Exception {
        StudentPenaltySummaryDTO summary = StudentPenaltySummaryDTO.builder()
                .studentId(studentId)
                .totalActivePoints(10)
                .currentLevel("YELLOW")
                .borrowingRestricted(false)
                .build();

        when(penaltyService.getStudentSummary(any(UUID.class))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/penalties/my/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLevel").value("YELLOW"));
    }
}