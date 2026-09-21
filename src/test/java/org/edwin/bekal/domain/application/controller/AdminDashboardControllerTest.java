package org.edwin.bekal.domain.application.controller;

import org.edwin.bekal.domain.application.dto.AdminDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.service.impl.AdminDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminDashboardServiceImpl adminDashboardService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminDashboardController).build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/overview - Success")
    void getOverview_ShouldReturnOverviewData() throws Exception {
        // Given
        AdminDashboardResponse mockResponse = new AdminDashboardResponse();
        given(adminDashboardService.getDashboardOverview()).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/dashboard/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminDashboardService).getDashboardOverview();
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/recent-activities - Success with default params")
    void getRecentActivities_WithDefaultParams_ShouldReturnPagedData() throws Exception {
        // Given
        Page<LoanApplication> mockPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        given(adminDashboardService.getRecentActivities(0, 10)).willReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/dashboard/recent-activities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(adminDashboardService).getRecentActivities(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/admin/dashboard/recent-activities - Success with custom page and size")
    void getRecentActivities_WithCustomParams_ShouldReturnPagedData() throws Exception {
        // Given
        Page<LoanApplication> mockPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(2, 20), 0);
        given(adminDashboardService.getRecentActivities(2, 20)).willReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/dashboard/recent-activities")
                        .param("page", "2")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(20));

        verify(adminDashboardService).getRecentActivities(2, 20);
    }
}