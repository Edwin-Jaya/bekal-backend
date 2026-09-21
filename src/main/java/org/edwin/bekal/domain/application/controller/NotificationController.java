package org.edwin.bekal.domain.application.controller;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.RegisterTokenRequest;
import org.edwin.bekal.domain.application.service.impl.DeviceTokenServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final DeviceTokenServiceImpl deviceTokenService;

    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(
            @RequestBody RegisterTokenRequest request,
            @AuthenticationPrincipal /* sesuaikan dengan UserDetails/JWT principal kamu */ Object principal
    ) {
        // Ambil customerId dari JWT/security context, bukan dari body request (security!)
        deviceTokenService.registerToken(request.getCustomerId(), request.getFcmToken(), request.getDeviceInfo());
        return ResponseEntity.ok().build();
    }
}