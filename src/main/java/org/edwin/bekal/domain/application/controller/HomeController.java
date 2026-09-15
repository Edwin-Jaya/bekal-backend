package org.edwin.bekal.domain.application.controller;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;
import org.edwin.bekal.domain.application.service.HomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<HomeDashboardResponse>> getDashboard(
            @RequestParam(required = false) UUID customerId) {
        HomeDashboardResponse response = homeService.getDashboardData(customerId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched successfully", response));
    }
}
