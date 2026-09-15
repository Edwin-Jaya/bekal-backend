package org.edwin.bekal.domain.customer.repository;

import org.edwin.bekal.domain.customer.entity.Employment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmploymentRepository extends JpaRepository<Employment, Integer> {
    Optional<Employment> findById(UUID id);

    Optional<Employment> findByCustomerId(UUID customerId);

    Optional<Employment> findByCustomer_IdAndCustomerIsCurrentTrue(UUID customerId);
}
