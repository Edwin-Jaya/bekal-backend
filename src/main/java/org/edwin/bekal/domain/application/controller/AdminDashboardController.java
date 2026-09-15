package org.edwin.bekal.domain.application.controller;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.AdminDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.service.impl.AdminDashboardServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardServiceImpl adminDashboardServiceimpl;

    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardResponse> getOverview() {
        return ResponseEntity.ok(adminDashboardServiceimpl.getDashboardOverview());
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<Page<LoanApplication>> getRecentActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminDashboardServiceimpl.getRecentActivities(page, size));
    }
}
