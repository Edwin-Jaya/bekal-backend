package org.edwin.bekal.domain.master.repository;

import org.edwin.bekal.domain.master.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    boolean existsByRoleName(String roleName);
    Optional<Role> findRolesByRoleName(String roleName);
    Optional<Role> findById(UUID id);
    Page<Role> findByRoleIsActive(boolean status, Pageable pageable);
}
