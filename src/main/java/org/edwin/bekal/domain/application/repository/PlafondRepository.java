package org.edwin.bekal.domain.application.repository;

import org.edwin.bekal.domain.application.entity.Plafond;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlafondRepository extends JpaRepository<Plafond, UUID> {
    Optional<Plafond> findFirstByCustomer_IdAndStatusOrderByValidFromDesc(UUID customerId, String status);
    Optional<Plafond> findFirstByCustomer_IdAndStatus(UUID customerId, String status);
}