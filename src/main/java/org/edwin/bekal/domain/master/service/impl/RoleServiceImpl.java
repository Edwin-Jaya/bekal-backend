package org.edwin.bekal.domain.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.entity.Role;
import org.edwin.bekal.domain.master.repository.RoleRepository;
import org.edwin.bekal.domain.master.service.RoleService;
import org.edwin.bekal.enums.BranchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request){
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID" + id));

        if (!role.getRoleName().equalsIgnoreCase(request.getRoleName())) {
            if (roleRepository.existsByRoleName(request.getRoleName())) {
                throw new IllegalArgumentException("Role name " + request.getRoleName() + " already exists!");
            }
            role.setRoleName(request.getRoleName());
        }

        role.setRoleDescription(request.getRoleDescription());
        role.setRoleIsActive(request.getRoleIsActive() == null || request.getRoleIsActive());

        Role updated = roleRepository.saveAndFlush(role);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRole(UUID id){
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with ID" + id));

        role.setRoleIsActive(false);
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request){
        if(roleRepository.existsByRoleName(request.getRoleName())){
            throw new IllegalArgumentException("Role name already exists!");
        }
        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setRoleDescription(request.getRoleDescription());
        role.setRoleIsActive(request.getRoleIsActive() == null || request.getRoleIsActive());

        Role saved = roleRepository.saveAndFlush(role);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRole(){
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public Page<RoleResponse> getRole(int page, int size, Boolean status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Role> rolePage;
        if (status != null) {
            rolePage = roleRepository.findByRoleIsActive(status, pageable);
        } else {
            rolePage = roleRepository.findAll(pageable);
        }

        return rolePage.map(this::mapToResponse);
    }


    public RoleResponse mapToResponse(Role role){
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .roleIsActive(role.getRoleIsActive() != null ? role.getRoleIsActive() : true)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
