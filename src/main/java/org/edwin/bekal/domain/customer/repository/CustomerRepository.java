package org.edwin.bekal.domain.customer.repository;

import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByCustomerEmail(String customerEmail);
    boolean existsByCustomerNik(String customerNik);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByCustomerEmail(String customerEmail);
//    Optional<Customer> findByCustomerEmail(String customerEmail);

}
