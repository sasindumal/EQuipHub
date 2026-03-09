package com.equiphub.api.controller;

import com.equiphub.api.dto.department.CreateDepartmentRequest;
import com.equiphub.api.dto.department.DepartmentResponse;
import com.equiphub.api.dto.department.UpdateDepartmentRequest;
import com.equiphub.api.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
@DisplayName("DepartmentController Tests")
class DepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DepartmentService departmentService;

    private static final UUID DEPT_ID = UUID.randomUUID();

    @Test
    @DisplayName("GET /admin/departments — SYSTEMADMIN → 200")
    @WithMockUser(roles = "SYSTEMADMIN")
    void getAllDepartments_Returns200() throws Exception {
        DepartmentResponse dept = new DepartmentResponse();
        dept.setDepartmentId(DEPT_ID);
        dept.setCode("CSE");

        when(departmentService.getAllDepartments()).thenReturn(List.of(dept));

        mockMvc.perform(get("/admin/departments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/departments/active — public → 200")
    @WithMockUser(roles = "STUDENT")
    void getActiveDepartments_Returns200() throws Exception {
        when(departmentService.getActiveDepartments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/departments/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/departments/{id} — found → 200")
    @WithMockUser(roles = "SYSTEMADMIN")
    void getDepartmentById_Found_Returns200() throws Exception {
        DepartmentResponse dept = new DepartmentResponse();
        dept.setDepartmentId(DEPT_ID);
        dept.setCode("CSE");

        when(departmentService.getDepartmentById(DEPT_ID)).thenReturn(dept);

        mockMvc.perform(get("/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/departments/{id} — not found → 404")
    @WithMockUser(roles = "SYSTEMADMIN")
    void getDepartmentById_NotFound_Returns404() throws Exception {
        when(departmentService.getDepartmentById(DEPT_ID))
                .thenThrow(new RuntimeException("Department not found"));

        mockMvc.perform(get("/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /admin/departments/{id} — SYSTEMADMIN → 200")
    @WithMockUser(roles = "SYSTEMADMIN")
    void deactivateDepartment_Returns200() throws Exception {
        doNothing().when(departmentService)
                   .deactivateDepartment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/departments/{id} — active users exist → 400")
    @WithMockUser(roles = "SYSTEMADMIN")
    void deactivateDepartment_WithUsers_Returns400() throws Exception {
        doThrow(new RuntimeException("Cannot deactivate department with active users"))
                .when(departmentService)
                .deactivateDepartment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/admin/departments/{id}", DEPT_ID))
                .andExpect(status().isBadRequest());
    }
}
