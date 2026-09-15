package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.edwin.bekal.domain.application.dto.CreateRoleMenuAccessRequest;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.application.repository.RoleMenuAccessRepository;
import org.edwin.bekal.domain.application.service.RoleMenuAccessService;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.edwin.bekal.domain.master.dto.RoleResponse;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.repository.MenuRepository;
import org.edwin.bekal.domain.master.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RoleMenuAccessServiceImpl implements RoleMenuAccessService {

    private final RoleMenuAccessRepository roleMenuAccessRepository;
    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    public RoleMenuAccessServiceImpl(
            RoleMenuAccessRepository roleMenuAccessRepository,
            RoleRepository roleRepository,
            MenuRepository menuRepository
    ) {
        this.roleMenuAccessRepository = roleMenuAccessRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
    }

    public Page<RoleMenuAccessResponse> getMatrixByRoleId(UUID roleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("menuName").ascending());
        return roleMenuAccessRepository.findMatrixByRoleId(roleId, pageable);
    }
    @Override
    @Transactional
    public Page<RoleMenuAccessResponse> getAccess(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<RoleMenuAccess> roleMenuAccessPage;
        roleMenuAccessPage = roleMenuAccessRepository.findAll(pageable);

        return roleMenuAccessPage.map(this::mapToResponse);
    }

    @Override
    public List<RoleMenuAccess> getByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return roleMenuAccessRepository.findAllById(ids);
    }

    @Override
    public Optional<RoleMenuAccess> getByRoleIdAndMenuId(UUID roleId, UUID menuId) {
        return roleMenuAccessRepository.findByRoleIdAndMenuId(roleId, menuId);
    }

    @Override
    public List<RoleMenuAccess> getActiveMenusByRoleIds(List<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleMenuAccessRepository.findActiveMenusByRoleIds(roleIds);
    }

    @Override
    @Transactional
    public void deleteByRoleId(UUID roleId) {
        roleMenuAccessRepository.deleteByRoleId(roleId);
    }

    @Override
    public boolean existsByRoleIdAndMenuId(UUID roleId, UUID menuId) {
        return roleMenuAccessRepository.existsByRoleIdAndMenuId(roleId, menuId);
    }

    @Override
    public Page<RoleMenuAccess> getByRoleId(UUID roleId, Pageable pageable) {
        return roleMenuAccessRepository.findByRoleId(roleId, pageable);
    }

    @Override
    @Transactional
    public RoleMenuAccess save(RoleMenuAccess roleMenuAccess) {
        return roleMenuAccessRepository.save(roleMenuAccess);
    }

    @Override
    @Transactional
    public List<RoleMenuAccessResponse> assignMenusToRole(UUID roleId, List<CreateRoleMenuAccessRequest> requests) {
        // 1. Validate role
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));

        // 2. Clear old access dan paksa flush agar delete langsung dieksekusi ke DB
        roleMenuAccessRepository.deleteByRoleId(roleId);
        roleMenuAccessRepository.flush(); // <--- PENTING: Mencegah bentrok urutan query Hibernate

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        // 3. Deduplikasi request berdasarkan menuId untuk mencegah duplikat dari frontend
        Map<UUID, CreateRoleMenuAccessRequest> uniqueRequestsMap = requests.stream()
                .collect(Collectors.toMap(
                        CreateRoleMenuAccessRequest::getMenuId,
                        req -> req,
                        (existing, replacement) -> replacement // Jika ada duplikat, ambil yang terakhir
                ));

        List<CreateRoleMenuAccessRequest> distinctRequests = new ArrayList<>(uniqueRequestsMap.values());

        // 4. Extract IDs and validate menus
        List<UUID> menuIds = distinctRequests.stream().map(CreateRoleMenuAccessRequest::getMenuId).toList();
        var menus = menuRepository.findAllById(menuIds);
        if (menus.size() != Set.copyOf(menuIds).size()) {
            var foundIds = menus.stream().map(Menu::getId).collect(Collectors.toSet());
            var missing = menuIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new EntityNotFoundException("Menu(s) not found: " + missing);
        }

        Map<UUID, Menu> menuMap = menus.stream().collect(Collectors.toMap(Menu::getId, m -> m));

        // 5. Map DTO requests to Entity with permissions
        List<RoleMenuAccess> newAccesses = distinctRequests.stream().map(req -> {
            RoleMenuAccess access = new RoleMenuAccess();
            access.setRole(role);
            access.setMenu(menuMap.get(req.getMenuId()));
            access.setRoleMenuCanView(req.getRoleMenuCanView() != null ? req.getRoleMenuCanView() : false);
            access.setRoleMenuCanCreate(req.getRoleMenuCanCreate() != null ? req.getRoleMenuCanCreate() : false);
            access.setRoleMenuCanEdit(req.getRoleMenuCanEdit() != null ? req.getRoleMenuCanEdit() : false);
            access.setRoleMenuCanDelete(req.getRoleMenuCanDelete() != null ? req.getRoleMenuCanDelete() : false);
            access.setRoleMenuCanApprove(req.getRoleMenuCanApprove() != null ? req.getRoleMenuCanApprove() : false);
            return access;
        }).toList();

        // 6. Save batch and return response DTO list
        List<RoleMenuAccess> savedAccesses = roleMenuAccessRepository.saveAll(newAccesses);
        return savedAccesses.stream().map(this::mapToResponse).toList();
    }

    public RoleMenuAccessResponse mapToResponse(RoleMenuAccess roleAccess){
        return RoleMenuAccessResponse.builder()
                .menuId(roleAccess.getMenu().getId())
                .roleMenuCanCreate(roleAccess.getRoleMenuCanCreate())
                .roleMenuCanDelete(roleAccess.getRoleMenuCanDelete())
                .roleMenuCanView(roleAccess.getRoleMenuCanView())
                .roleMenuCanEdit(roleAccess.getRoleMenuCanEdit())
                .roleMenuCanApprove(roleAccess.getRoleMenuCanApprove())
                .createdAt(roleAccess.getCreatedAt())
                .updatedAt(roleAccess.getUpdatedAt())
                .build();
    }

    private RoleMenuAccessResponse mapToMatrixResponse(Menu menu, RoleMenuAccess roleAccess) {
        return RoleMenuAccessResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getMenuName())
                .roleMenuCanView(roleAccess != null && Boolean.TRUE.equals(roleAccess.getRoleMenuCanView()))
                .roleMenuCanCreate(roleAccess != null && Boolean.TRUE.equals(roleAccess.getRoleMenuCanCreate()))
                .roleMenuCanEdit(roleAccess != null && Boolean.TRUE.equals(roleAccess.getRoleMenuCanEdit()))
                .roleMenuCanDelete(roleAccess != null && Boolean.TRUE.equals(roleAccess.getRoleMenuCanDelete()))
                .roleMenuCanApprove(roleAccess != null && Boolean.TRUE.equals(roleAccess.getRoleMenuCanApprove()))
                .createdAt(roleAccess != null ? roleAccess.getCreatedAt() : null)
                .updatedAt(roleAccess != null ? roleAccess.getUpdatedAt() : null)
                .build();
    }
}