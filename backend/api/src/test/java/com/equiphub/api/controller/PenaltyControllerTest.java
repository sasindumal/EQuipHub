package com.equiphub.api.controller;

import com.equiphub.api.dto.penalty.*;
import com.equiphub.api.model.Penalty.PenaltyStatus;
import com.equiphub.api.model.Penalty.PenaltyType;
import com.equiphub.api.model.PenaltyAppeal;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.security.CustomUserDetailsService;
import com.equiphub.api.security.jwt.JwtUtils;
import com.equiphub.api.service.PenaltyService;
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

@WebMvcTest(PenaltyController.class)
class PenaltyControllerTest extends BaseControllerTest{ 

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  PenaltyService penaltyService;

    static final UUID TO_ID      = UUID.randomUUID();
    static final UUID HOD_ID     = UUID.randomUUID();
    static final UUID STUDENT_ID = UUID.randomUUID();
    static final UUID DEPT_ID    = UUID.randomUUID();
    static final int  PENALTY_ID = 1;

    CustomUserDetails toUser;
    CustomUserDetails hodUser;
    CustomUserDetails studentUser;

    @BeforeEach
    void setUp() {
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

        hodUser = mock(CustomUserDetails.class);
        when(hodUser.getUserId()).thenReturn(HOD_ID);
        when(hodUser.getRole()).thenReturn(User.Role.HEADOFDEPARTMENT);
        when(hodUser.getDepartmentId()).thenReturn(DEPT_ID.toString());
        when(hodUser.getUsername()).thenReturn(HOD_ID.toString());
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_HOD"))).when(hodUser).getAuthorities();
        when(hodUser.isEnabled()).thenReturn(true);
        when(hodUser.isAccountNonExpired()).thenReturn(true);
        when(hodUser.isAccountNonLocked()).thenReturn(true);
        when(hodUser.isCredentialsNonExpired()).thenReturn(true);

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
    }

    // ── 1. CREATE PENALTY ────────────────────────────────────────────────────────

    @Test @DisplayName("POST /penalties — TO creates penalty → 201")
    void createPenalty_TO_201() throws Exception {
        PenaltyResponseDTO response = mock(PenaltyResponseDTO.class);
        when(penaltyService.createPenalty(any(CreatePenaltyDTO.class), any(UUID.class)))
            .thenReturn(response);

        CreatePenaltyDTO dto = new CreatePenaltyDTO();
        dto.setStudentId(STUDENT_ID);
        dto.setRequestId("REQ-2026-00001");
        dto.setPenaltyType(PenaltyType.LATERETURN);
        dto.setPoints(5);
        dto.setReason("Equipment returned 2 days late");

        mockMvc.perform(post("/api/v1/penalties")
                .with(user(toUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test @DisplayName("POST /penalties — student cannot create → 403")
    void createPenalty_Student_403() throws Exception {
        mockMvc.perform(post("/api/v1/penalties")
                .with(user(studentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test @DisplayName("POST /penalties — invalid data → 400")
    void createPenalty_InvalidData_400() throws Exception {
        when(penaltyService.createPenalty(any(CreatePenaltyDTO.class), any(UUID.class)))
            .thenThrow(new IllegalArgumentException("Student not found"));

        mockMvc.perform(post("/api/v1/penalties")
                .with(user(toUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ── 2. APPROVE PENALTY ───────────────────────────────────────────────────────

    @Test @DisplayName("PUT /penalties/{id}/approve — HOD approves → 200")
    void approvePenalty_HOD_200() throws Exception {
        PenaltyResponseDTO response = mock(PenaltyResponseDTO.class);
        when(penaltyService.approvePenalty(anyInt(), any(UUID.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/penalties/{id}/approve", PENALTY_ID)
                .with(user(hodUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /penalties/{id}/approve — penalty not found → 404")
    void approvePenalty_NotFound_404() throws Exception {
        when(penaltyService.approvePenalty(anyInt(), any(UUID.class)))
            .thenThrow(new NoSuchElementException("Penalty not found"));

        mockMvc.perform(put("/api/v1/penalties/{id}/approve", 999)
                .with(user(hodUser)))
            .andExpect(status().isNotFound());
    }

    // ── 3. WAIVE PENALTY ─────────────────────────────────────────────────────────

    @Test @DisplayName("PUT /penalties/{id}/waive — HOD waives with reason → 200")
    void waivePenalty_HOD_200() throws Exception {
        PenaltyResponseDTO response = mock(PenaltyResponseDTO.class);
        when(penaltyService.waivePenalty(anyInt(), any(UUID.class), anyString()))
            .thenReturn(response);

        mockMvc.perform(put("/api/v1/penalties/{id}/waive", PENALTY_ID)
                .with(user(hodUser))
                .param("reason", "Equipment damage was pre-existing"))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /penalties/{id}/waive — student cannot waive → 403")
    void waivePenalty_Student_403() throws Exception {
        mockMvc.perform(put("/api/v1/penalties/{id}/waive", PENALTY_ID)
                .with(user(studentUser))
                .param("reason", "Please waive"))
            .andExpect(status().isForbidden());
    }

    // ── 4. GET STUDENT PENALTIES ─────────────────────────────────────────────────

    @Test @DisplayName("GET /penalties/student/{studentId} — TO views student → 200")
    void getStudentPenalties_TO_200() throws Exception {
        when(penaltyService.getStudentPenalties(any(UUID.class)))
            .thenReturn(List.of(mock(PenaltyResponseDTO.class)));

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}", STUDENT_ID)
                .with(user(toUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/student/{studentId} — student views own → 200")
    void getStudentPenalties_OwnRecord_200() throws Exception {
        when(penaltyService.getStudentPenalties(any(UUID.class)))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}", STUDENT_ID)
                .with(user(studentUser)))
            .andExpect(status().isOk());
    }

    // ── 5. GET STUDENT SUMMARY ───────────────────────────────────────────────────

    @Test @DisplayName("GET /penalties/student/{studentId}/summary → 200")
    void getStudentSummary_200() throws Exception {
        StudentPenaltySummaryDTO summary = mock(StudentPenaltySummaryDTO.class);
        when(penaltyService.getStudentSummary(any(UUID.class))).thenReturn(summary);

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}/summary", STUDENT_ID)
                .with(user(toUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/student/{studentId}/summary — not found → 404")
    void getStudentSummary_NotFound_404() throws Exception {
        when(penaltyService.getStudentSummary(any(UUID.class)))
            .thenThrow(new NoSuchElementException("Student not found"));

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}/summary", UUID.randomUUID())
                .with(user(toUser)))
            .andExpect(status().isNotFound());
    }

    // ── 6. GET DEPARTMENT PENALTIES ──────────────────────────────────────────────

    @Test @DisplayName("GET /penalties/department/{deptId} — own dept → 200")
    void getDepartmentPenalties_OwnDept_200() throws Exception {
        when(penaltyService.getDepartmentPenalties(any(UUID.class)))
            .thenReturn(List.of(mock(PenaltyResponseDTO.class)));

        mockMvc.perform(get("/api/v1/penalties/department/{deptId}", DEPT_ID)
                .with(user(toUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/department/{deptId} — wrong dept → 403")
    void getDepartmentPenalties_WrongDept_403() throws Exception {
        mockMvc.perform(get("/api/v1/penalties/department/{deptId}", UUID.randomUUID())
                .with(user(toUser)))
            .andExpect(status().isForbidden());
    }

    // ── 7. GET DEPARTMENT PENDING PENALTIES ──────────────────────────────────────

    @Test @DisplayName("GET /penalties/department/{deptId}/pending → 200")
    void getDepartmentPendingPenalties_200() throws Exception {
        when(penaltyService.getDepartmentPendingPenalties(any(UUID.class)))
            .thenReturn(List.of(mock(PenaltyResponseDTO.class)));

        mockMvc.perform(get("/api/v1/penalties/department/{deptId}/pending", DEPT_ID)
                .with(user(hodUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/department/{deptId}/pending — empty → 200")
    void getDepartmentPendingPenalties_Empty_200() throws Exception {
        when(penaltyService.getDepartmentPendingPenalties(any(UUID.class)))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/penalties/department/{deptId}/pending", DEPT_ID)
                .with(user(hodUser)))
            .andExpect(status().isOk());
    }

    // ── 8. SUBMIT APPEAL ─────────────────────────────────────────────────────────

    @Test @DisplayName("POST /penalties/appeal — student submits appeal → 200")
    void submitAppeal_Student_200() throws Exception {
        PenaltyResponseDTO response = mock(PenaltyResponseDTO.class);
        when(penaltyService.submitAppeal(any(AppealRequestDTO.class), any(UUID.class)))
            .thenReturn(response);

        AppealRequestDTO dto = new AppealRequestDTO();
        dto.setPenaltyId(PENALTY_ID);
        dto.setAppealReason("The damage was pre-existing before my borrowing period");

        mockMvc.perform(post("/api/v1/penalties/appeal")
                .with(user(studentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("POST /penalties/appeal — penalty not found → 400")
    void submitAppeal_NotFound_400() throws Exception {
        when(penaltyService.submitAppeal(any(AppealRequestDTO.class), any(UUID.class)))
            .thenThrow(new IllegalArgumentException("Penalty not found or not eligible for appeal"));

        mockMvc.perform(post("/api/v1/penalties/appeal")
                .with(user(studentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ── 9. DECIDE APPEAL ─────────────────────────────────────────────────────────

    @Test @DisplayName("PUT /penalties/{id}/appeal/decide — HOD approves appeal → 200")
    void decideAppeal_Approve_200() throws Exception {
        PenaltyResponseDTO response = mock(PenaltyResponseDTO.class);
        when(penaltyService.decideAppeal(anyInt(), any(AppealDecisionDTO.class), any(UUID.class)))
            .thenReturn(response);

        AppealDecisionDTO dto = new AppealDecisionDTO();
        dto.setDecisionReason("Evidence accepted");
        dto.setPointsWaived(5);

        mockMvc.perform(put("/api/v1/penalties/{id}/appeal/decide", PENALTY_ID)
                .with(user(hodUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("PUT /penalties/{id}/appeal/decide — student cannot decide → 403")
    void decideAppeal_Student_403() throws Exception {
        mockMvc.perform(put("/api/v1/penalties/{id}/appeal/decide", PENALTY_ID)
                .with(user(studentUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }

    // ── 10. GET PENDING APPEALS ──────────────────────────────────────────────────

    @Test @DisplayName("GET /penalties/department/{deptId}/appeals/pending — HOD → 200")
    void getPendingAppeals_HOD_200() throws Exception {
        List<PenaltyAppeal> appeals = List.of(mock(PenaltyAppeal.class));
        when(penaltyService.getPendingAppeals(any(UUID.class))).thenReturn(appeals);

        mockMvc.perform(get("/api/v1/penalties/department/{deptId}/appeals/pending", DEPT_ID)
                .with(user(hodUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/department/{deptId}/appeals/pending — wrong dept → 403")
    void getPendingAppeals_WrongDept_403() throws Exception {
        mockMvc.perform(get("/api/v1/penalties/department/{deptId}/appeals/pending", UUID.randomUUID())
                .with(user(hodUser)))
            .andExpect(status().isForbidden());
    }

    // ── 11. CAN STUDENT BORROW ───────────────────────────────────────────────────

    @Test @DisplayName("GET /penalties/student/{studentId}/can-borrow — eligible → 200 true")
    void canStudentBorrow_Eligible_200() throws Exception {
        when(penaltyService.canStudentBorrow(any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}/can-borrow", STUDENT_ID)
                .with(user(toUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/student/{studentId}/can-borrow — restricted → 200 false")
    void canStudentBorrow_Restricted_200() throws Exception {
        when(penaltyService.canStudentBorrow(any(UUID.class))).thenReturn(false);

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}/can-borrow", STUDENT_ID)
                .with(user(toUser)))
            .andExpect(status().isOk());
    }

    @Test @DisplayName("GET /penalties/student/{studentId}/can-borrow — student checks own → 200")
    void canStudentBorrow_OwnCheck_200() throws Exception {
        when(penaltyService.canStudentBorrow(any(UUID.class))).thenReturn(true);

        mockMvc.perform(get("/api/v1/penalties/student/{studentId}/can-borrow", STUDENT_ID)
                .with(user(studentUser)))
            .andExpect(status().isOk());
    }
}
