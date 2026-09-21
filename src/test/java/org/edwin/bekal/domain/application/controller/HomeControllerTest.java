package org.edwin.bekal.domain.application.controller;

import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.service.HomeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HomeService homeService;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    @DisplayName("GET /api/v1/home/dashboard - Success without customerId parameter")
    void getDashboard_WithoutCustomerId_Success() throws Exception {
        HomeDashboardResponse mockResponse = Mockito.mock(HomeDashboardResponse.class);
        given(homeService.getDashboardData(null)).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/home/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dashboard fetched successfully"));

        verify(homeService).getDashboardData(null);
    }

    @Test
    @DisplayName("GET /api/v1/home/dashboard - Success with customerId parameter")
    void getDashboard_WithCustomerId_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        HomeDashboardResponse mockResponse = Mockito.mock(HomeDashboardResponse.class);
        given(homeService.getDashboardData(eq(customerId))).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/home/dashboard")
                        .param("customerId", customerId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dashboard fetched successfully"));

        verify(homeService).getDashboardData(eq(customerId));
    }
}