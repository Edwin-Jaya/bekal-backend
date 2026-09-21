package org.edwin.bekal.domain.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RegisterTokenRequest {
    private UUID customerId;
    private String fcmToken;
    private String deviceInfo;
}