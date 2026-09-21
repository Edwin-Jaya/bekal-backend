package org.edwin.bekal.domain.customer.service;

import java.util.UUID;

public interface CustomerRollbackService {
    void rollbackIncompleteRegistration(UUID customerId);
}
