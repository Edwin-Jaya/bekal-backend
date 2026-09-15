package org.edwin.bekal.domain.master.repository;

import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.enums.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    boolean existsByBranchCode(String branchCode);
    Optional<Branch> findBranchesByBranchCode(String branchCode);
    Optional<Branch> findById(UUID id);
    List<Branch> findByBranchStatus(BranchStatus status);
}
