package org.edwin.bekal.domain.application.controller;

import org.edwin.bekal.domain.application.service.impl.DeviceTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeviceTokenServiceImpl deviceTokenService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    @DisplayName("POST /api/v1/notifications/register-token - Success")
    void registerToken_Success() throws Exception {
        UUID customerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String fcmToken = "fcm_token_sample_123";
        String deviceInfo = "Android Pixel 7";

        String jsonPayload = String.format("""
                {
                    "customerId": "%s",
                    "fcmToken": "%s",
                    "deviceInfo": "%s"
                }
                """, customerId, fcmToken, deviceInfo);

        mockMvc.perform(post("/api/v1/notifications/register-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        verify(deviceTokenService).registerToken(customerId, fcmToken, deviceInfo);
    }
}