package org.edwin.bekal.domain.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.service.BranchService;
import org.edwin.bekal.enums.BranchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request){
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID" + id));

        if (!branch.getBranchCode().equalsIgnoreCase(request.getBranchCode())) {
            if (branchRepository.existsByBranchCode(request.getBranchCode())) {
                throw new IllegalArgumentException("Branch code " + request.getBranchCode() + " already exists!");
            }
            branch.setBranchCode(request.getBranchCode());
        }

        branch.setBranchName(request.getBranchName());
        branch.setBranchCity(request.getBranchCity());
        branch.setBranchAddress(request.getBranchAddress());

        Branch updated = branchRepository.saveAndFlush(branch);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id){
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID" + id));

        branch.setBranchStatus(BranchStatus.INACTIVE);
    }

    @Override
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request){
        if(branchRepository.existsByBranchCode(request.getBranchCode())){
            throw new IllegalArgumentException("Branch code already exists!");
        }
        Branch branch = new Branch();
        branch.setBranchCode(request.getBranchCode());
        branch.setBranchName(request.getBranchName());
        branch.setBranchAddress(request.getBranchAddress());
        branch.setBranchCity(request.getBranchCity());
        branch.setBranchStatus(BranchStatus.ACTIVE);
        Branch saved = branchRepository.saveAndFlush(branch);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches(){
        return branchRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BranchResponse mapToResponse(Branch branch){
        return BranchResponse.builder()
                .id(branch.getId())
                .branchCode(branch.getBranchCode())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getBranchAddress())
                .branchCity(branch.getBranchCity())
                .branchStatus(branch.getBranchStatus().name())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
