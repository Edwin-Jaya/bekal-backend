package org.edwin.bekal.domain.customer.repository;

import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document,Integer> {
    Optional<Document> findById(UUID id);

    List<DocumentResponse> findAllByCustomerAndIsLatest(Customer customer, Boolean isLatest);

    List<Document> findByCustomerIdAndIsLatestTrue(UUID customerId);
}
