package org.edwin.bekal.domain.application.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.edwin.bekal.domain.application.dto.CreateRoleMenuAccessRequest;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.application.repository.RoleMenuAccessRepository;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.entity.Role;
import org.edwin.bekal.domain.master.repository.MenuRepository;
import org.edwin.bekal.domain.master.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleMenuAccessServiceImplTest {

    @Mock
    private RoleMenuAccessRepository roleMenuAccessRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private RoleMenuAccessServiceImpl roleMenuAccessService;

    @Nested
    @DisplayName("Query & Fetch Tests")
    class QueryAndFetchTests {

        @Test
        @DisplayName("getMatrixByRoleId - Success")
        void getMatrixByRoleId_success() {
            UUID roleId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("menuName").ascending());
            Page<RoleMenuAccessResponse> page = new PageImpl<>(List.of());

            given(roleMenuAccessRepository.findMatrixByRoleId(roleId, pageable)).willReturn(page);

            Page<RoleMenuAccessResponse> result = roleMenuAccessService.getMatrixByRoleId(roleId, 0, 10);

            assertThat(result).isNotNull();
            verify(roleMenuAccessRepository).findMatrixByRoleId(roleId, pageable);
        }

        @Test
        @DisplayName("getAccess - Success returning mapped page")
        void getAccess_success() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
            Menu menu = new Menu();
            menu.setId(UUID.randomUUID());

            RoleMenuAccess access = new RoleMenuAccess();
            access.setMenu(menu);
            access.setRoleMenuCanView(true);

            Page<RoleMenuAccess> accessPage = new PageImpl<>(List.of(access));
            given(roleMenuAccessRepository.findAll(pageable)).willReturn(accessPage);

            Page<RoleMenuAccessResponse> result = roleMenuAccessService.getAccess(0, 10);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getRoleMenuCanView()).isTrue();
        }

        @Test
        @DisplayName("getByIds - Returns empty list when null or empty")
        void getByIds_nullOrEmpty_returnsEmptyList() {
            assertThat(roleMenuAccessService.getByIds(null)).isEmpty();
            assertThat(roleMenuAccessService.getByIds(List.of())).isEmpty();
            verifyNoInteractions(roleMenuAccessRepository);
        }

        @Test
        @DisplayName("getByIds - Success with valid IDs")
        void getByIds_validIds_returnsList() {
            List<UUID> ids = List.of(UUID.randomUUID());
            given(roleMenuAccessRepository.findAllById(ids)).willReturn(List.of(new RoleMenuAccess()));

            List<RoleMenuAccess> result = roleMenuAccessService.getByIds(ids);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getActiveMenusByRoleIds - Returns empty list when null or empty")
        void getActiveMenusByRoleIds_nullOrEmpty_returnsEmptyList() {
            assertThat(roleMenuAccessService.getActiveMenusByRoleIds(null)).isEmpty();
            assertThat(roleMenuAccessService.getActiveMenusByRoleIds(List.of())).isEmpty();
            verifyNoInteractions(roleMenuAccessRepository);
        }

        @Test
        @DisplayName("getByRoleIdAndMenuId - Success")
        void getByRoleIdAndMenuId_success() {
            UUID roleId = UUID.randomUUID();
            UUID menuId = UUID.randomUUID();
            given(roleMenuAccessRepository.findByRoleIdAndMenuId(roleId, menuId)).willReturn(Optional.of(new RoleMenuAccess()));

            Optional<RoleMenuAccess> result = roleMenuAccessService.getByRoleIdAndMenuId(roleId, menuId);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("existsByRoleIdAndMenuId - Success")
        void existsByRoleIdAndMenuId_success() {
            UUID roleId = UUID.randomUUID();
            UUID menuId = UUID.randomUUID();
            given(roleMenuAccessRepository.existsByRoleIdAndMenuId(roleId, menuId)).willReturn(true);

            boolean exists = roleMenuAccessService.existsByRoleIdAndMenuId(roleId, menuId);

            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("Mutation Tests")
    class MutationTests {

        @Test
        @DisplayName("deleteByRoleId - Executed successfully")
        void deleteByRoleId_success() {
            UUID roleId = UUID.randomUUID();

            roleMenuAccessService.deleteByRoleId(roleId);

            verify(roleMenuAccessRepository).deleteByRoleId(roleId);
        }

        @Test
        @DisplayName("save - Success")
        void save_success() {
            RoleMenuAccess access = new RoleMenuAccess();
            given(roleMenuAccessRepository.save(access)).willReturn(access);

            RoleMenuAccess saved = roleMenuAccessService.save(access);

            assertThat(saved).isNotNull();
            verify(roleMenuAccessRepository).save(access);
        }
    }

    @Nested
    @DisplayName("assignMenusToRole Tests")
    class AssignMenusToRoleTests {

        @Test
        @DisplayName("Should throw EntityNotFoundException when role is not found")
        void assignMenusToRole_roleNotFound_throwsException() {
            UUID roleId = UUID.randomUUID();
            given(roleRepository.findById(roleId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> roleMenuAccessService.assignMenusToRole(roleId, List.of()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Role not found: " + roleId);
        }

        @Test
        @DisplayName("Should return empty list when request list is null or empty")
        void assignMenusToRole_nullOrEmptyRequests_returnsEmptyList() {
            UUID roleId = UUID.randomUUID();
            Role role = new Role();

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));

            List<RoleMenuAccessResponse> resultNull = roleMenuAccessService.assignMenusToRole(roleId, null);
            List<RoleMenuAccessResponse> resultEmpty = roleMenuAccessService.assignMenusToRole(roleId, List.of());

            assertThat(resultNull).isEmpty();
            assertThat(resultEmpty).isEmpty();
            verify(roleMenuAccessRepository, times(2)).deleteByRoleId(roleId);
            verify(roleMenuAccessRepository, times(2)).flush();
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when one or more menus are missing")
        void assignMenusToRole_missingMenus_throwsException() {
            UUID roleId = UUID.randomUUID();
            UUID menuId1 = UUID.randomUUID();

            Role role = new Role();
            CreateRoleMenuAccessRequest req1 = new CreateRoleMenuAccessRequest();
            req1.setMenuId(menuId1);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(menuRepository.findAllById(List.of(menuId1))).willReturn(List.of());

            assertThatThrownBy(() -> roleMenuAccessService.assignMenusToRole(roleId, List.of(req1)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Menu(s) not found:");
        }

        @Test
        @DisplayName("Should deduplicate requests, assign permissions, save and map responses")
        void assignMenusToRole_success_withDeduplication() {
            UUID roleId = UUID.randomUUID();
            UUID menuId = UUID.randomUUID();

            Role role = new Role();
            role.setId(roleId);

            Menu menu = new Menu();
            menu.setId(menuId);

            CreateRoleMenuAccessRequest req1 = new CreateRoleMenuAccessRequest();
            req1.setMenuId(menuId);
            req1.setRoleMenuCanView(false);

            // Request duplikat untuk menuId yang sama (akan di-override oleh req2)
            CreateRoleMenuAccessRequest req2 = new CreateRoleMenuAccessRequest();
            req2.setMenuId(menuId);
            req2.setRoleMenuCanView(true);
            req2.setRoleMenuCanCreate(true);

            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(menuRepository.findAllById(List.of(menuId))).willReturn(List.of(menu));

            given(roleMenuAccessRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            List<RoleMenuAccessResponse> responses = roleMenuAccessService.assignMenusToRole(roleId, List.of(req1, req2));

            assertThat(responses).hasSize(1);
            RoleMenuAccessResponse resp = responses.get(0);
            assertThat(resp.getMenuId()).isEqualTo(menuId);
            assertThat(resp.getRoleMenuCanView()).isTrue();
            assertThat(resp.getRoleMenuCanCreate()).isTrue();

            verify(roleMenuAccessRepository).deleteByRoleId(roleId);
            verify(roleMenuAccessRepository).flush();
            verify(roleMenuAccessRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("mapToResponse Tests")
    class MapToResponseTests {

        @Test
        @DisplayName("Should map RoleMenuAccess entity to RoleMenuAccessResponse accurately")
        void mapToResponse_success() {
            UUID menuId = UUID.randomUUID();
            Menu menu = new Menu();
            menu.setId(menuId);

            RoleMenuAccess access = new RoleMenuAccess();
            access.setMenu(menu);
            access.setRoleMenuCanView(true);
            access.setRoleMenuCanCreate(false);
            access.setRoleMenuCanEdit(true);
            access.setRoleMenuCanDelete(false);
            access.setRoleMenuCanApprove(true);

            RoleMenuAccessResponse response = roleMenuAccessService.mapToResponse(access);

            assertThat(response).isNotNull();
            assertThat(response.getMenuId()).isEqualTo(menuId);
            assertThat(response.getRoleMenuCanView()).isTrue();
            assertThat(response.getRoleMenuCanCreate()).isFalse();
            assertThat(response.getRoleMenuCanEdit()).isTrue();
            assertThat(response.getRoleMenuCanDelete()).isFalse();
            assertThat(response.getRoleMenuCanApprove()).isTrue();
        }
    }
}