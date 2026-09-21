package org.edwin.bekal.domain.application.service.impl;

import org.edwin.bekal.domain.application.dto.AdminDashboardResponse;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.application.repository.LoanApplicationRepository;
import org.edwin.bekal.domain.application.repository.projection.DashboardProjections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    // ==================================================================== //
    //  getDashboardOverview() Test Cases                                   //
    // ==================================================================== //

    @Test
    @DisplayName("getDashboardOverview - Success with Billions (B) and Millions (M) currency formatting")
    void getDashboardOverview_success_billionAndMillion() {
        var rawMetrics = mock(DashboardProjections.Metrics.class);
        given(rawMetrics.getTotalPengajuanAmount()).willReturn(new BigDecimal("2500000000")); // 2.5B
        given(rawMetrics.getTotalFiles()).willReturn(120L);
        given(rawMetrics.getAntreanReview()).willReturn(15L);
        given(rawMetrics.getAntreanApproval()).willReturn(8L);
        given(rawMetrics.getAntreanPencairan()).willReturn(5L);
        given(rawMetrics.getTotalDicairkanAmount()).willReturn(new BigDecimal("15000000")); // 15.0M

        given(loanApplicationRepository.getDashboardMetrics()).willReturn(rawMetrics);
        given(loanApplicationRepository.getOperationalTrend()).willReturn(Collections.emptyList());
        given(loanApplicationRepository.getBottleneckStatus()).willReturn(Collections.emptyList());

        AdminDashboardResponse response = adminDashboardService.getDashboardOverview();

        assertThat(response).isNotNull();
        assertThat(response.getMetrics()).isNotNull();

        // Verifikasi Total Pengajuan (Format Miliaran / B)
        assertThat(response.getMetrics().getTotalPengajuan().getValue()).isEqualTo("Rp 2.5B");
        assertThat(response.getMetrics().getTotalPengajuan().getSubtext()).isEqualTo("120 Files");
        assertThat(response.getMetrics().getTotalPengajuan().getSubtextColor()).isEqualTo("purple");

        // Verifikasi Antrean
        assertThat(response.getMetrics().getAntreanReview().getValue()).isEqualTo(15L);
        assertThat(response.getMetrics().getAntreanReview().getSubtext()).isEqualTo("Marketing Team");
        assertThat(response.getMetrics().getAntreanApproval().getValue()).isEqualTo(8L);
        assertThat(response.getMetrics().getAntreanPencairan().getValue()).isEqualTo(5L);

        // Verifikasi Total Dicairkan (Format Jutaan / M)
        assertThat(response.getMetrics().getTotalDicairkan().getValue()).isEqualTo("Rp 15.0M");

        // Verifikasi Charts
        assertThat(response.getCharts()).isNotNull();
        assertThat(response.getCharts().getOperationalTrend()).isEmpty();
        assertThat(response.getCharts().getBottleneckStatus()).isEmpty();

        verify(loanApplicationRepository).getDashboardMetrics();
        verify(loanApplicationRepository).getOperationalTrend();
        verify(loanApplicationRepository).getBottleneckStatus();
    }

    @Test
    @DisplayName("getDashboardOverview - Success with Thousands (K) and Small values currency formatting")
    void getDashboardOverview_success_thousandAndSmallValue() {
        var rawMetrics = mock(DashboardProjections.Metrics.class);
        given(rawMetrics.getTotalPengajuanAmount()).willReturn(new BigDecimal("50000")); // 50.0K
        given(rawMetrics.getTotalFiles()).willReturn(10L);
        given(rawMetrics.getAntreanReview()).willReturn(2L);
        given(rawMetrics.getAntreanApproval()).willReturn(1L);
        given(rawMetrics.getAntreanPencairan()).willReturn(0L);
        given(rawMetrics.getTotalDicairkanAmount()).willReturn(new BigDecimal("500")); // 500

        given(loanApplicationRepository.getDashboardMetrics()).willReturn(rawMetrics);
        given(loanApplicationRepository.getOperationalTrend()).willReturn(Collections.emptyList());
        given(loanApplicationRepository.getBottleneckStatus()).willReturn(Collections.emptyList());

        AdminDashboardResponse response = adminDashboardService.getDashboardOverview();

        // Verifikasi Format Ribuan (K) dan Angka Kecil (< 1000)
        assertThat(response.getMetrics().getTotalPengajuan().getValue()).isEqualTo("Rp 50.0K");
        assertThat(response.getMetrics().getTotalDicairkan().getValue()).isEqualTo("Rp 500");
    }

    @Test
    @DisplayName("getDashboardOverview - Success with Null and Zero currency values")
    void getDashboardOverview_success_nullAndZeroAmount() {
        var rawMetrics = mock(DashboardProjections.Metrics.class);
        given(rawMetrics.getTotalPengajuanAmount()).willReturn(null);
        given(rawMetrics.getTotalFiles()).willReturn(0L);
        given(rawMetrics.getAntreanReview()).willReturn(0L);
        given(rawMetrics.getAntreanApproval()).willReturn(0L);
        given(rawMetrics.getAntreanPencairan()).willReturn(0L);
        given(rawMetrics.getTotalDicairkanAmount()).willReturn(BigDecimal.ZERO);

        given(loanApplicationRepository.getDashboardMetrics()).willReturn(rawMetrics);
        given(loanApplicationRepository.getOperationalTrend()).willReturn(Collections.emptyList());
        given(loanApplicationRepository.getBottleneckStatus()).willReturn(Collections.emptyList());

        AdminDashboardResponse response = adminDashboardService.getDashboardOverview();

        // Verifikasi kondisi Null & Zero bernilai "Rp 0"
        assertThat(response.getMetrics().getTotalPengajuan().getValue()).isEqualTo("Rp 0");
        assertThat(response.getMetrics().getTotalDicairkan().getValue()).isEqualTo("Rp 0");
    }

    // ==================================================================== //
    //  getRecentActivities() Test Cases                                    //
    // ==================================================================== //

    @Test
    @DisplayName("getRecentActivities - Success returning page of loan applications")
    void getRecentActivities_success() {
        int page = 0;
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        LoanApplication mockLoan = mock(LoanApplication.class);
        Page<LoanApplication> mockPage = new PageImpl<>(List.of(mockLoan), pageRequest, 1L);

        given(loanApplicationRepository.findAllByOrderByUpdatedAtDesc(pageRequest)).willReturn(mockPage);

        Page<LoanApplication> result = adminDashboardService.getRecentActivities(page, size);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockLoan);

        verify(loanApplicationRepository).findAllByOrderByUpdatedAtDesc(pageRequest);
    }
}