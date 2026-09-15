package org.edwin.bekal.domain.master.service;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.application.repository.RoleMenuAccessRepository;
import org.edwin.bekal.domain.master.dto.UserMenuResponse;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DynamicMenuService {

    private final RoleMenuAccessRepository roleMenuAccessRepository;
    private final InternalUserRepository internalUserRepository;

    public List<UserMenuResponse> getMenusForCurrentUser(Authentication authentication) {
        // 1. Ambil email user dari token/session
        String email = authentication.getName();

        // 2. Cari data InternalUser di database untuk mendapatkan roleId
        InternalUser user = internalUserRepository
                .findByInternalUserEmailAndInternalUserIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + email));

        UUID roleId = user.getRole().getId();

        // 3. Ambil daftar menu dari database
        List<RoleMenuAccess> accessList = roleMenuAccessRepository.findAllowedMenusByRoleId(roleId);

        // 4. Transformasi daftar flat ke hirarki Tree (parent-children)
        Map<UUID, UserMenuResponse> dtoMap = new HashMap<>();
        List<UserMenuResponse> rootMenus = new ArrayList<>();

        for (RoleMenuAccess access : accessList) {
            var menu = access.getMenu();
            UserMenuResponse dto = UserMenuResponse.builder()
                    .id(menu.getId())
                    .parentId(menu.getMenuParent() != null ? menu.getMenuParent().getId() : null)
                    .name(menu.getMenuName())
                    .path(menu.getMenuPath())
                    .icon(menu.getMenuIcon())
                    .sortOrder(menu.getMenuSortOrder())
                    .canCreate(access.getRoleMenuCanCreate())
                    .canEdit(access.getRoleMenuCanEdit())
                    .canDelete(access.getRoleMenuCanDelete())
                    .canApprove(access.getRoleMenuCanApprove())
                    .children(new ArrayList<>())
                    .build();

            dtoMap.put(dto.getId(), dto);

            if (dto.getParentId() == null) {
                rootMenus.add(dto);
            }
        }

        for (UserMenuResponse dto : dtoMap.values()) {
            if (dto.getParentId() != null) {
                UserMenuResponse parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootMenus;
    }

    private Long extractRoleIdFromAuth(Authentication authentication) {
        // Implement based on your UserPrincipal/JWT Custom Claims
        // e.g., ((UserPrincipal) authentication.getPrincipal()).getRoleId();
        return 1L; // Temporary fallback for testing
    }
}
