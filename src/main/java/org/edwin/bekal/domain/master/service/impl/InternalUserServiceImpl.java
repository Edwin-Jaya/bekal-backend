package org.edwin.bekal.domain.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.entity.Role;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.edwin.bekal.domain.master.repository.RoleRepository;
import org.edwin.bekal.domain.master.service.InternalUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalUserServiceImpl implements InternalUserService {

    private final InternalUserRepository internalUserRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String generateEmployeeCode() {
        String currentYear = String.valueOf(Year.now().getValue());
        Optional<String> lastCodeOpt = internalUserRepository.findLastEmployeeCodeByYear(currentYear);

        int nextNumber = 1;

        if (lastCodeOpt.isPresent()) {
            String lastCode = lastCodeOpt.get();
            String lastSequence = lastCode.substring(lastCode.lastIndexOf('-') + 1);
            nextNumber = Integer.parseInt(lastSequence) + 1;
        }

        return String.format("EMP-%s-%03d", currentYear, nextNumber);
    }

    @Override
    @Transactional
    public InternalUserResponse createInternalUser(CreateInternalUserRequest request) {
        // 1. Validasi Keunikan Email & Employee Code
        if (internalUserRepository.existsByInternalUserEmail(request.getInternalUserEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }
        if (internalUserRepository.existsByInternalUserEmployeeCode(request.getInternalUserEmployeeCode())) {
            throw new IllegalArgumentException("Employee code already exists!");
        }

        // 2. Fetch Relasi Branch & Role
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + request.getBranchId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + request.getRoleId()));

        // 3. Mapping Request ke Entity
        InternalUser user = new InternalUser();
        user.setBranch(branch);
        user.setRole(role);
        user.setInternalUserEmployeeCode(generateEmployeeCode());
        user.setInternalUserFullName(request.getInternalUserFullName());
        user.setInternalUserEmail(request.getInternalUserEmail());
        user.setInternalUserPasswordHash(passwordEncoder.encode(request.getInternalUserPasswordHash()));
        user.setInternalUserPhoneNumber(request.getInternalUserPhoneNumber());
        user.setInternalUserIsActive(request.getInternalUserIsActive() == null || request.getInternalUserIsActive());

        InternalUser saved = internalUserRepository.saveAndFlush(user);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InternalUserResponse updateInternalUser(UUID id, UpdateInternalUserRequest request) {
        InternalUser user = internalUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internal User not found with ID: " + id));

        // Validasi Email jika berubah
        if (!user.getInternalUserEmail().equalsIgnoreCase(request.getInternalUserEmail())) {
            if (internalUserRepository.existsByInternalUserEmail(request.getInternalUserEmail())) {
                throw new IllegalArgumentException("Email " + request.getInternalUserEmail() + " already exists!");
            }
            user.setInternalUserEmail(request.getInternalUserEmail());
        }

        // Validasi Employee Code jika berubah
        if (!user.getInternalUserEmployeeCode().equalsIgnoreCase(request.getInternalUserEmployeeCode())) {
            if (internalUserRepository.existsByInternalUserEmployeeCode(request.getInternalUserEmployeeCode())) {
                throw new IllegalArgumentException("Employee code " + request.getInternalUserEmployeeCode() + " already exists!");
            }
            user.setInternalUserEmployeeCode(request.getInternalUserEmployeeCode());
        }

        // Update Relasi Branch & Role jika berubah
        if (!user.getBranch().getId().equals(request.getBranchId())) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + request.getBranchId()));
            user.setBranch(branch);
        }

        if (!user.getRole().getId().equals(request.getRoleId())) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + request.getRoleId()));
            user.setRole(role);
        }

        user.setInternalUserFullName(request.getInternalUserFullName());
        user.setInternalUserPhoneNumber(request.getInternalUserPhoneNumber());
        user.setInternalUserIsActive(request.getInternalUserIsActive() == null || request.getInternalUserIsActive());

        InternalUser updated = internalUserRepository.saveAndFlush(user);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInternalUser(UUID id) {
        InternalUser user = internalUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internal User not found with ID: " + id));

        // Menggunakan fitur Soft Delete dari BaseFullEntity
        user.setInternalUserIsActive(false);
        user.setDeletedAt(Instant.now());
        internalUserRepository.save(user);
    }

    @Override
    @Transactional
    public void activateInternalUser(UUID id) {
        InternalUser user = internalUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internal User not found with ID: " + id));

        // Mengembalikan status menjadi aktif
        user.setInternalUserIsActive(true);
        user.setDeletedAt(null); // Clear timestamp soft delete
        internalUserRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalUserResponse> getAllInternalUser() {
        // Ambil data yang belum di-soft-delete (isDeleted = false)
        return internalUserRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<InternalUserResponse> getInternalUser(int page, int size, Boolean status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<InternalUser> userPage;
        if (status != null) {
            userPage = internalUserRepository.findByInternalUserIsActive(status, pageable);
        } else {
            userPage = internalUserRepository.findAll(pageable);
        }

        return userPage.map(this::mapToResponse);
    }

    public InternalUserResponse mapToResponse(InternalUser user) {
        return InternalUserResponse.builder()
                .id(user.getId())
                .branch(user.getBranch())
                .role(user.getRole())
                .internalUserEmployeeCode(user.getInternalUserEmployeeCode())
                .internalUserFullName(user.getInternalUserFullName())
                .internalUserEmail(user.getInternalUserEmail())
                .internalUserPasswordHash(user.getInternalUserPasswordHash())
                .internalUserPhoneNumber(user.getInternalUserPhoneNumber())
                .internalUserIsActive(user.getInternalUserIsActive() != null ? user.getInternalUserIsActive() : true)
                .internalUserLastLoginAt(user.getInternalUserLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}