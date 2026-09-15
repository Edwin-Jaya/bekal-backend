package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.AdminDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl {
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardOverview() {
        var rawMetrics = loanApplicationRepository.getDashboardMetrics();
        var trendData = loanApplicationRepository.getOperationalTrend();
        var bottleneckData = loanApplicationRepository.getBottleneckStatus();

        AdminDashboardResponse.MetricsDto metrics = AdminDashboardResponse.MetricsDto.builder()
                .totalPengajuan(AdminDashboardResponse.MetricCardDto.builder()
                        .value(formatCurrency(rawMetrics.getTotalPengajuanAmount()))
                        .subtext(rawMetrics.getTotalFiles() + " Files")
                        .subtextColor("purple")
                        .build())
                .antreanReview(AdminDashboardResponse.MetricCardDto.builder()
                        .value(rawMetrics.getAntreanReview())
                        .subtext("Marketing Team")
                        .subtextColor("gray")
                        .build())
                .antreanApproval(AdminDashboardResponse.MetricCardDto.builder()
                        .value(rawMetrics.getAntreanApproval())
                        .subtext("Branch Manager")
                        .subtextColor("gray")
                        .build())
                .antreanPencairan(AdminDashboardResponse.MetricCardDto.builder()
                        .value(rawMetrics.getAntreanPencairan())
                        .subtext("Ready")
                        .subtextColor("green")
                        .build())
                .totalDicairkan(AdminDashboardResponse.MetricCardDto.builder()
                        .value(formatCurrency(rawMetrics.getTotalDicairkanAmount()))
                        .subtext("Accumulative")
                        .subtextColor("gray")
                        .build())
                .build();

        AdminDashboardResponse.ChartsDto charts = AdminDashboardResponse.ChartsDto.builder()
                .operationalTrend(trendData)
                .bottleneckStatus(bottleneckData)
                .build();

        return AdminDashboardResponse.builder()
                .metrics(metrics)
                .charts(charts)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<LoanApplication> getRecentActivities(int page, int size) {
        return loanApplicationRepository.findAllByOrderByUpdatedAtDesc(PageRequest.of(page, size));
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "Rp 0";

        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal thousand = new BigDecimal("1000");

        if (amount.compareTo(billion) >= 0) {
            return "Rp " + amount.divide(billion, 1, RoundingMode.HALF_UP) + "B";
        } else if (amount.compareTo(million) >= 0) {
            return "Rp " + amount.divide(million, 1, RoundingMode.HALF_UP) + "M";
        } else if (amount.compareTo(thousand) >= 0) {
            return "Rp " + amount.divide(thousand, 1, RoundingMode.HALF_UP) + "K";
        }
        return "Rp " + amount.toString();
    }
}
