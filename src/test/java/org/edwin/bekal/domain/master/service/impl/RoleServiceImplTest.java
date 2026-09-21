package org.edwin.bekal.domain.master.service.impl;

import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.CreateRoleRequest;
import org.edwin.bekal.domain.master.dto.RoleResponse;
import org.edwin.bekal.domain.master.dto.UpdateRoleRequest;
import org.edwin.bekal.domain.master.entity.Role;
import org.edwin.bekal.domain.master.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully when role name is unique")
        void createRole_success() {
            CreateRoleRequest request = new CreateRoleRequest();
            request.setRoleName("ADMIN");
            request.setRoleDescription("Administrator role");

            given(roleRepository.existsByRoleName("ADMIN")).willReturn(false);
            given(roleRepository.saveAndFlush(any(Role.class))).willAnswer(inv -> inv.getArgument(0));

            RoleResponse response = roleService.createRole(request);

            assertThat(response).isNotNull();
            assertThat(response.getRoleName()).isEqualTo("ADMIN");
            assertThat(response.getRoleIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when role name already exists")
        void createRole_duplicateName_throwsException() {
            CreateRoleRequest request = new CreateRoleRequest();
            request.setRoleName("ADMIN");

            given(roleRepository.existsByRoleName("ADMIN")).willReturn(true);

            assertThatThrownBy(() -> roleService.createRole(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Role name already exists!");
        }
    }

    @Nested
    @DisplayName("updateRole Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role successfully")
        void updateRole_success() {
            UUID id = UUID.randomUUID();
            UpdateRoleRequest request = new UpdateRoleRequest();
            request.setRoleName("SUPER_ADMIN");
            request.setRoleDescription("Super admin role");

            Role existing = new Role();
            existing.setId(id);
            existing.setRoleName("ADMIN");

            given(roleRepository.findById(id)).willReturn(Optional.of(existing));
            given(roleRepository.existsByRoleName("SUPER_ADMIN")).willReturn(false);
            given(roleRepository.saveAndFlush(any(Role.class))).willAnswer(inv -> inv.getArgument(0));

            RoleResponse response = roleService.updateRole(id, request);

            assertThat(response).isNotNull();
            assertThat(response.getRoleName()).isEqualTo("SUPER_ADMIN");
        }

        @Test
        @DisplayName("Should throw exception when role not found for update")
        void updateRole_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            UpdateRoleRequest request = new UpdateRoleRequest();

            given(roleRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.updateRole(id, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating to an existing role name")
        void updateRole_duplicateName_throwsException() {
            UUID id = UUID.randomUUID();
            UpdateRoleRequest request = new UpdateRoleRequest();
            request.setRoleName("EXISTING_ROLE");

            Role existing = new Role();
            existing.setId(id);
            existing.setRoleName("ADMIN");

            given(roleRepository.findById(id)).willReturn(Optional.of(existing));
            given(roleRepository.existsByRoleName("EXISTING_ROLE")).willReturn(true);

            assertThatThrownBy(() -> roleService.updateRole(id, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists!");
        }
    }

    @Nested
    @DisplayName("Role Queries and Delete Tests")
    class RoleQueriesAndActionsTests {

        @Test
        @DisplayName("Should return all roles")
        void getAllRole_success() {
            Role r1 = new Role();
            r1.setRoleName("ADMIN");
            Role r2 = new Role();
            r2.setRoleName("USER");

            given(roleRepository.findAll()).willReturn(List.of(r1, r2));

            List<RoleResponse> responses = roleService.getAllRole();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getRoleName()).isEqualTo("ADMIN");
            assertThat(responses.get(1).getRoleName()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should return paginated roles with status filter")
        void getRole_page_withStatus_success() {
            Role role = new Role();
            role.setRoleName("MANAGER");
            Page<Role> page = new PageImpl<>(List.of(role));

            given(roleRepository.findByRoleIsActive(any(Boolean.class), any(Pageable.class))).willReturn(page);

            CacheablePage<RoleResponse> result = roleService.getRole(0, 10, true);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return paginated roles with null status")
        void getRole_page_nullStatus_success() {
            Role role = new Role();
            role.setRoleName("MANAGER");
            Page<Role> page = new PageImpl<>(List.of(role));

            given(roleRepository.findAll(any(Pageable.class))).willReturn(page);

            CacheablePage<RoleResponse> result = roleService.getRole(0, 10, null);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should set roleIsActive to false on delete")
        void deleteRole_success() {
            UUID id = UUID.randomUUID();
            Role role = new Role();
            role.setId(id);
            role.setRoleIsActive(true);

            given(roleRepository.findById(id)).willReturn(Optional.of(role));

            roleService.deleteRole(id);

            assertThat(role.getRoleIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent role")
        void deleteRole_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            given(roleRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> roleService.deleteRole(id))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}