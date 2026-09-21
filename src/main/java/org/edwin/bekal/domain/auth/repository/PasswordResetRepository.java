package org.edwin.bekal.domain.auth.repository;

import org.edwin.bekal.domain.auth.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {

    Optional<PasswordReset> findTopByEmailAndTokenAndUsedAtIsNullOrderByCreatedAtDesc(String email, String token);

    void deleteByEmailAndUsedAtIsNull(String email);
}