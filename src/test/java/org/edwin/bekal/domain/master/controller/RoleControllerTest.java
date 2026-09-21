package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.CreateRoleRequest;
import org.edwin.bekal.domain.master.dto.RoleResponse;
import org.edwin.bekal.domain.master.dto.UpdateRoleRequest;
import org.edwin.bekal.domain.master.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully and return CREATED")
        void createRole_success() {
            CreateRoleRequest request = new CreateRoleRequest();
            RoleResponse responseDto = RoleResponse.builder().roleName("SUPER_ADMIN").build();

            given(roleService.createRole(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<RoleResponse>> response = roleController.createRole(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getRoleName()).isEqualTo("SUPER_ADMIN");
        }
    }

    @Nested
    @DisplayName("getAllRole Tests")
    class GetAllRoleTests {

        @Test
        @DisplayName("Should return all roles successfully")
        void getAllRole_success() {
            List<RoleResponse> list = List.of(
                    RoleResponse.builder().roleName("ADMIN").build(),
                    RoleResponse.builder().roleName("CUSTOMER").build()
            );

            given(roleService.getAllRole()).willReturn(list);

            ResponseEntity<ApiResponse<List<RoleResponse>>> response = roleController.getAllRole();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getRole (Page) Tests")
    class GetRolePageTests {

        @Test
        @DisplayName("Should return paginated roles successfully")
        void getRole_success() {
            Page<RoleResponse> pageResponse = new PageImpl<>(List.of(
                    RoleResponse.builder().roleName("MANAGER").build()
            ));

            given(roleService.getRole(0, 10, true)).willReturn(pageResponse);

            ResponseEntity<Page<RoleResponse>> response = roleController.getRole(0, 10, true);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getRoleName()).isEqualTo("MANAGER");
        }
    }

    @Nested
    @DisplayName("updateRole Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role successfully and return CREATED")
        void updateRole_success() {
            UUID id = UUID.randomUUID();
            UpdateRoleRequest request = new UpdateRoleRequest();
            RoleResponse responseDto = RoleResponse.builder().id(id).roleName("UPDATED_ROLE").build();

            given(roleService.updateRole(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<RoleResponse>> response = roleController.updateRole(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getRoleName()).isEqualTo("UPDATED_ROLE");
        }
    }

    @Nested
    @DisplayName("deleteRole Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role successfully and return NO_CONTENT")
        void deleteRole_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<RoleResponse>> response = roleController.deleteRole(id);

            verify(roleService).deleteRole(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}