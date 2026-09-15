package org.edwin.bekal.domain.application.service;

import org.edwin.bekal.domain.application.dto.CreateRoleMenuAccessRequest;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleMenuAccessService {
    List<RoleMenuAccess> getByIds(List<UUID> ids);
    Optional<RoleMenuAccess> getByRoleIdAndMenuId(UUID roleId, UUID menuId);
    List<RoleMenuAccess> getActiveMenusByRoleIds(List<UUID> roleIds);
    void deleteByRoleId(UUID roleId);
    boolean existsByRoleIdAndMenuId(UUID roleId, UUID menuId);
    Page<RoleMenuAccess> getByRoleId(UUID roleId, Pageable pageable);
    RoleMenuAccess save(RoleMenuAccess roleMenuAccess);
    List<RoleMenuAccessResponse> assignMenusToRole(UUID roleId, List<CreateRoleMenuAccessRequest> requests);
    Page<RoleMenuAccessResponse> getAccess(int page, int size);
    Page<RoleMenuAccessResponse> getMatrixByRoleId(UUID roleId, int page, int size);
}
