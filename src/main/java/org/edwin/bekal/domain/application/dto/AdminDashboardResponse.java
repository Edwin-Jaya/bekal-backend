package org.edwin.bekal.domain.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    private MetricsDto metrics;
    private ChartsDto charts;
    private List<RecentActivityDto> recentActivities; // <-- Field baru untuk tabel log

    @Getter
    @Builder
    public static class MetricsDto {
        private MetricCardDto totalPengajuan;
        private MetricCardDto antreanReview;
        private MetricCardDto antreanApproval;
        private MetricCardDto antreanPencairan;
        private MetricCardDto totalDicairkan;
    }

    @Getter
    @Builder
    public static class MetricCardDto {
        private Object value;
        private String subtext;
        private String subtextColor;
    }

    @Getter
    @Builder
    public static class ChartsDto {
        private List<?> operationalTrend;
        private List<?> bottleneckStatus;
    }

    // Static inner class baru untuk memetakan item aktivitas terbaru
    @Getter
    @Builder
    public static class RecentActivityDto {
        private String date;
        private String applicant;
        private Object amount; // Bisa BigDecimal atau String
        private String status;
        private String responsible;
    }
}