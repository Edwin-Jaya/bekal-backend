package org.edwin.bekal.domain.master.service;

import org.edwin.bekal.domain.master.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);
    List<RoleResponse> getAllRole();
    RoleResponse updateRole(UUID id, UpdateRoleRequest request);
    void deleteRole(UUID id);
    Page<RoleResponse> getRole(int page, int size, Boolean status);
}
