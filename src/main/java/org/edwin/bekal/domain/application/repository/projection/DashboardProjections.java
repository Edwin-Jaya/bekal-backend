package org.edwin.bekal.domain.application.repository.projection;

import java.math.BigDecimal;

public class DashboardProjections {

    public interface Metrics {
        Long getTotalFiles();
        BigDecimal getTotalPengajuanAmount();
        Long getAntreanReview();
        Long getAntreanApproval();
        Long getAntreanPencairan();
        BigDecimal getTotalDicairkanAmount();
    }

    public interface OperationalTrend {
        String getMonth();
        Integer getMonthNum();
        BigDecimal getSubmissionAmount();
        BigDecimal getDisbursedAmount();
    }

    public interface BottleneckStatus {
        String getStage();
        Long getCount();
    }

    public interface RecentActivity {
        String getDate();
        String getApplicant();
        BigDecimal getAmount();
        String getStatus();
    }
}
