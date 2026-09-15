package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.HomeDashboardResponse;

import java.util.UUID;

public interface HomeService {
    HomeDashboardResponse getDashboardData(UUID customerId);
}
