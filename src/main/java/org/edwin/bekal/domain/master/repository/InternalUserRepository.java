package org.edwin.bekal.domain.master.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternalUserRepository extends JpaRepository<InternalUser, Integer> {

    @Query("SELECT u.internalUserEmployeeCode FROM InternalUser u " +
            "WHERE u.internalUserEmployeeCode LIKE CONCAT('EMP-', :year, '-%') " +
            "ORDER BY u.internalUserEmployeeCode DESC LIMIT 1")
    Optional<String> findLastEmployeeCodeByYear(@Param("year") String year);
    boolean existsByInternalUserEmployeeCode(String internalUserEmployeeCode);
    Optional<InternalUser> findByInternalUserEmployeeCode(String internalUserEmployeeCode);
    boolean existsByInternalUserEmail(String internalUserEmail);
    Optional<InternalUser> findById(UUID id);
    Page<InternalUser> findByInternalUserIsActive(boolean status, Pageable pageable);
    Optional<InternalUser> findByInternalUserEmailAndInternalUserIsActiveTrue(String internalUserEmail);
    Optional<InternalUser> findByInternalUserEmail(String internalUserEmail);
}
