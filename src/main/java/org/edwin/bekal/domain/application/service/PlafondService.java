package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.PlafondResponse;

import java.util.UUID;

public interface PlafondService {
    PlafondResponse getActivePlafond(UUID customerId);
}