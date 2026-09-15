package org.edwin.bekal.domain.master.service;

import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchResponse createBranch(CreateBranchRequest request);
    List<BranchResponse> getAllBranches();
    BranchResponse updateBranch(UUID id, UpdateBranchRequest request);
    void deleteBranch(UUID id);
}
