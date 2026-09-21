package org.edwin.bekal.domain.master.service.impl;

import org.edwin.bekal.domain.master.dto.CreateInternalUserRequest;
import org.edwin.bekal.domain.master.dto.InternalUserResponse;
import org.edwin.bekal.domain.master.dto.UpdateInternalUserRequest;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.edwin.bekal.domain.master.entity.Role;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InternalUserServiceImplTest {

    @Mock
    private InternalUserRepository internalUserRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InternalUserServiceImpl internalUserService;

    @Nested
    @DisplayName("createInternalUser Tests")
    class CreateInternalUserTests {

        @Test
        @DisplayName("Should create internal user successfully")
        void createInternalUser_success() {
            UUID branchId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("test@bekal.org");
            request.setInternalUserEmployeeCode("EMP-2026-001");
            request.setBranchId(branchId);
            request.setRoleId(roleId);
            request.setInternalUserPasswordHash("password123");
            request.setInternalUserFullName("Test User");

            Branch branch = new Branch();
            branch.setId(branchId);
            Role role = new Role();
            role.setId(roleId);

            given(internalUserRepository.existsByInternalUserEmail("test@bekal.org")).willReturn(false);
            given(internalUserRepository.existsByInternalUserEmployeeCode("EMP-2026-001")).willReturn(false);
            given(branchRepository.findById(branchId)).willReturn(Optional.of(branch));
            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(passwordEncoder.encode("password123")).willReturn("encodedHash");
            given(internalUserRepository.findLastEmployeeCodeByYear(anyString())).willReturn(Optional.empty());
            given(internalUserRepository.saveAndFlush(any(InternalUser.class))).willAnswer(inv -> inv.getArgument(0));

            InternalUserResponse response = internalUserService.createInternalUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getInternalUserEmail()).isEqualTo("test@bekal.org");
            assertThat(response.getInternalUserIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createInternalUser_emailExists_throwsException() {
            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("existing@bekal.org");

            given(internalUserRepository.existsByInternalUserEmail("existing@bekal.org")).willReturn(true);

            assertThatThrownBy(() -> internalUserService.createInternalUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already exists!");
        }

        @Test
        @DisplayName("Should throw exception when employee code already exists")
        void createInternalUser_employeeCodeExists_throwsException() {
            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("test@bekal.org");
            request.setInternalUserEmployeeCode("EMP-2026-001");

            given(internalUserRepository.existsByInternalUserEmail("test@bekal.org")).willReturn(false);
            given(internalUserRepository.existsByInternalUserEmployeeCode("EMP-2026-001")).willReturn(true);

            assertThatThrownBy(() -> internalUserService.createInternalUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Employee code already exists!");
        }

        @Test
        @DisplayName("Should generate employee code sequentially when last code exists")
        void generateEmployeeCode_withExistingCode_success() {
            UUID branchId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("seq@bekal.org");
            request.setInternalUserEmployeeCode(null); // Trigger auto-generation branch
            request.setBranchId(branchId);
            request.setRoleId(roleId);
            request.setInternalUserPasswordHash("password");
            request.setInternalUserFullName("Seq User");

            Branch branch = new Branch();
            branch.setId(branchId);
            Role role = new Role();
            role.setId(roleId);

            given(internalUserRepository.existsByInternalUserEmail(anyString())).willReturn(false);
            given(branchRepository.findById(branchId)).willReturn(Optional.of(branch));
            given(roleRepository.findById(roleId)).willReturn(Optional.of(role));
            given(passwordEncoder.encode(anyString())).willReturn("hash");
            given(internalUserRepository.findLastEmployeeCodeByYear(anyString())).willReturn(Optional.of("EMP-2026-005"));
            given(internalUserRepository.saveAndFlush(any(InternalUser.class))).willAnswer(inv -> inv.getArgument(0));

            InternalUserResponse response = internalUserService.createInternalUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getInternalUserEmployeeCode()).isEqualTo("EMP-2026-006");
        }

        @Test
        @DisplayName("Should throw exception when branch not found during creation")
        void createInternalUser_branchNotFound_throwsException() {
            UUID branchId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("test@bekal.org");
            request.setBranchId(branchId);
            request.setRoleId(roleId);

            given(internalUserRepository.existsByInternalUserEmail(anyString())).willReturn(false);
            given(branchRepository.findById(branchId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> internalUserService.createInternalUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Branch not found with ID: " + branchId);
        }

        @Test
        @DisplayName("Should throw exception when role not found during creation")
        void createInternalUser_roleNotFound_throwsException() {
            UUID branchId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            CreateInternalUserRequest request = new CreateInternalUserRequest();
            request.setInternalUserEmail("test@bekal.org");
            request.setBranchId(branchId);
            request.setRoleId(roleId);

            Branch branch = new Branch();
            branch.setId(branchId);

            given(internalUserRepository.existsByInternalUserEmail(anyString())).willReturn(false);
            given(branchRepository.findById(branchId)).willReturn(Optional.of(branch));
            given(roleRepository.findById(roleId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> internalUserService.createInternalUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Role not found with ID: " + roleId);
        }
    }

    @Nested
    @DisplayName("updateInternalUser Tests")
    class UpdateInternalUserTests {

        @Test
        @DisplayName("Should update internal user successfully")
        void updateInternalUser_success() {
            UUID userId = UUID.randomUUID();
            UUID branchId = UUID.randomUUID();
            UUID roleId = UUID.randomUUID();

            UpdateInternalUserRequest request = new UpdateInternalUserRequest();
            request.setInternalUserEmail("new@bekal.org");
            request.setInternalUserEmployeeCode("EMP-2026-001");
            request.setBranchId(branchId);
            request.setRoleId(roleId);
            request.setInternalUserFullName("Updated Name");

            InternalUser existing = new InternalUser();
            existing.setId(userId);
            existing.setInternalUserEmail("old@bekal.org");
            existing.setInternalUserEmployeeCode("EMP-2026-001");
            Branch branch = new Branch();
            branch.setId(branchId);
            Role role = new Role();
            role.setId(roleId);
            existing.setBranch(branch);
            existing.setRole(role);

            given(internalUserRepository.findById(userId)).willReturn(Optional.of(existing));
            given(internalUserRepository.existsByInternalUserEmail("new@bekal.org")).willReturn(false);
            given(internalUserRepository.saveAndFlush(any(InternalUser.class))).willAnswer(inv -> inv.getArgument(0));

            InternalUserResponse response = internalUserService.updateInternalUser(userId, request);

            assertThat(response).isNotNull();
            assertThat(response.getInternalUserEmail()).isEqualTo("new@bekal.org");
            assertThat(response.getInternalUserFullName()).isEqualTo("Updated Name");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent user")
        void updateInternalUser_notFound_throwsException() {
            UUID userId = UUID.randomUUID();
            UpdateInternalUserRequest request = new UpdateInternalUserRequest();

            given(internalUserRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> internalUserService.updateInternalUser(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw exception when updating to an already existing email")
        void updateInternalUser_emailExists_throwsException() {
            UUID userId = UUID.randomUUID();
            UpdateInternalUserRequest request = new UpdateInternalUserRequest();
            request.setInternalUserEmail("taken@bekal.org");

            InternalUser existing = new InternalUser();
            existing.setId(userId);
            existing.setInternalUserEmail("old@bekal.org");

            given(internalUserRepository.findById(userId)).willReturn(Optional.of(existing));
            given(internalUserRepository.existsByInternalUserEmail("taken@bekal.org")).willReturn(true);

            assertThatThrownBy(() -> internalUserService.updateInternalUser(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists!");
        }
    }

    @Nested
    @DisplayName("deleteInternalUser & activate Tests")
    class DeleteActivateTests {

        @Test
        @DisplayName("Should soft delete internal user")
        void deleteInternalUser_success() {
            UUID userId = UUID.randomUUID();
            InternalUser user = new InternalUser();
            user.setId(userId);
            user.setInternalUserIsActive(true);

            given(internalUserRepository.findById(userId)).willReturn(Optional.of(user));

            internalUserService.deleteInternalUser(userId);

            assertThat(user.getInternalUserIsActive()).isFalse();
            assertThat(user.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void deleteInternalUser_notFound_throwsException() {
            UUID userId = UUID.randomUUID();
            given(internalUserRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> internalUserService.deleteInternalUser(userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should activate internal user")
        void activateInternalUser_success() {
            UUID userId = UUID.randomUUID();
            InternalUser user = new InternalUser();
            user.setId(userId);
            user.setInternalUserIsActive(false);

            given(internalUserRepository.findById(userId)).willReturn(Optional.of(user));

            internalUserService.activateInternalUser(userId);

            assertThat(user.getInternalUserIsActive()).isTrue();
            assertThat(user.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when activating non-existent user")
        void activateInternalUser_notFound_throwsException() {
            UUID userId = UUID.randomUUID();
            given(internalUserRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> internalUserService.activateInternalUser(userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getInternalUser Queries Tests")
    class GetQueriesTests {

        @Test
        @DisplayName("Should return paginated internal users by status (true)")
        void getInternalUser_paginated_true_success() {
            InternalUser user = new InternalUser();
            user.setInternalUserEmail("user@bekal.org");
            Page<InternalUser> page = new PageImpl<>(List.of(user));

            given(internalUserRepository.findByInternalUserIsActive(eq(true), any(Pageable.class))).willReturn(page);

            Page<InternalUserResponse> result = internalUserService.getInternalUser(0, 10, true);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return paginated internal users by status (false)")
        void getInternalUser_paginated_false_success() {
            InternalUser user = new InternalUser();
            Page<InternalUser> page = new PageImpl<>(List.of(user));

            given(internalUserRepository.findByInternalUserIsActive(eq(false), any(Pageable.class))).willReturn(page);

            Page<InternalUserResponse> result = internalUserService.getInternalUser(0, 10, false);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return paginated internal users when status filter is null")
        void getInternalUser_paginated_nullStatus_success() {
            InternalUser user = new InternalUser();
            Page<InternalUser> page = new PageImpl<>(List.of(user));

            given(internalUserRepository.findAll(any(Pageable.class))).willReturn(page);

            Page<InternalUserResponse> result = internalUserService.getInternalUser(0, 10, null);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return list of all internal users via getAllInternalUser")
        void getAllInternalUser_success() {
            InternalUser user = new InternalUser();
            given(internalUserRepository.findAll()).willReturn(List.of(user));

            List<InternalUserResponse> list = internalUserService.getAllInternalUser();

            assertThat(list).hasSize(1);
        }
    }
}