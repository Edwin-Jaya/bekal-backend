package org.edwin.bekal.domain.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
import org.edwin.bekal.domain.application.dto.RegisterTokenRequest;
import org.edwin.bekal.domain.application.service.impl.DeviceTokenServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "Endpoints for managing Firebase Cloud Messaging (FCM) device tokens and mobile alerts")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class NotificationController {

    private final DeviceTokenServiceImpl deviceTokenService;

    @Operation(summary = "Register FCM Device Token", description = "Registers or updates a Firebase Cloud Messaging device token for push notifications.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device token registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(
            @RequestBody RegisterTokenRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        deviceTokenService.registerToken(request.getCustomerId(), request.getFcmToken(), request.getDeviceInfo());
        return ResponseEntity.ok().build();
    }
}