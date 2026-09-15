package org.edwin.bekal.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.PlafondResponse;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.application.repository.PlafondRepository;
import org.edwin.bekal.domain.application.service.PlafondService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlafondServiceImpl implements PlafondService {

    private final PlafondRepository plafondRepository;

    @Override
    public PlafondResponse getActivePlafond(UUID customerId) {
        Plafond plafond = plafondRepository
                .findFirstByCustomer_IdAndStatusOrderByValidFromDesc(customerId, "active")
                .orElseThrow(() -> new IllegalArgumentException("Tidak ada plafond aktif untuk customer ini"));

        return PlafondResponse.builder()
                .id(plafond.getId())
                .plafondAmount(plafond.getPlafondAmount())
                .usedAmount(plafond.getUsedAmount())
                .availableAmount(plafond.getPlafondAmount().subtract(plafond.getUsedAmount()))
                .interestRate(plafond.getInterestRate())
                .maxTenorMonths(plafond.getMaxTenorMonths())
                .status(plafond.getStatus())
                .validFrom(plafond.getValidFrom())
                .validUntil(plafond.getValidUntil())
                .build();
    }
}