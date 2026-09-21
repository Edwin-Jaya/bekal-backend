package org.edwin.bekal.domain.application.controller;

import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.service.PlafondService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlafondControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlafondService plafondService;

    @InjectMocks
    private PlafondController plafondController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(plafondController).build();
    }

    @Test
    @DisplayName("GET /api/v1/plafonds/customer/{customerId}/active - Success")
    void getActivePlafond_Success() throws Exception {
        UUID customerId = UUID.randomUUID();
        PlafondResponse mockResponse = Mockito.mock(PlafondResponse.class);
        given(plafondService.getActivePlafond(customerId)).willReturn(mockResponse);

        mockMvc.perform(get("/api/v1/plafonds/customer/{customerId}/active", customerId))
                .andExpect(status().isOk());

        verify(plafondService).getActivePlafond(customerId);
    }
}