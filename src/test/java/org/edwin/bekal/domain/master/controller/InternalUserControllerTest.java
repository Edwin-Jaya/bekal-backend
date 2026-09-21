package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.CreateInternalUserRequest;
import org.edwin.bekal.domain.master.dto.InternalUserResponse;
import org.edwin.bekal.domain.master.dto.UpdateInternalUserRequest;
import org.edwin.bekal.domain.master.service.InternalUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private InternalUserService internalUserService;

    @InjectMocks
    private InternalUserController internalUserController;

    @Nested
    @DisplayName("createInternalUser Tests")
    class CreateInternalUserTests {

        @Test
        @DisplayName("Should create internal user successfully and return CREATED")
        void createInternalUser_success() {
            CreateInternalUserRequest request = new CreateInternalUserRequest();
            InternalUserResponse responseDto = InternalUserResponse.builder()
                    .internalUserEmail("admin@bekal.org")
                    .internalUserFullName("Super Admin")
                    .build();

            given(internalUserService.createInternalUser(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<InternalUserResponse>> response = internalUserController.createInternalUser(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getInternalUserEmail()).isEqualTo("admin@bekal.org");
        }
    }

    @Nested
    @DisplayName("getInternalUsers Tests")
    class GetInternalUsersTests {

        @Test
        @DisplayName("Should return paginated internal users successfully")
        void getInternalUsers_success() {
            @SuppressWarnings("unchecked")
            CacheablePage<InternalUserResponse> pageResponse = mock(CacheablePage.class);
            given(pageResponse.getContent()).willReturn(List.of(
                    InternalUserResponse.builder()
                            .internalUserEmail("user1@bekal.org")
                            .build()
            ));

            given(internalUserService.getInternalUser(0, 10, true)).willReturn(pageResponse);

            ResponseEntity<CacheablePage<InternalUserResponse>> response = internalUserController.getInternalUsers(0, 10, true);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getInternalUserEmail()).isEqualTo("user1@bekal.org");
        }
    }

    @Nested
    @DisplayName("updateInternalUser Tests")
    class UpdateInternalUserTests {

        @Test
        @DisplayName("Should update internal user successfully and return CREATED")
        void updateInternalUser_success() {
            UUID id = UUID.randomUUID();
            UpdateInternalUserRequest request = new UpdateInternalUserRequest();
            InternalUserResponse responseDto = InternalUserResponse.builder()
                    .id(id)
                    .internalUserEmail("updated@bekal.org")
                    .build();

            given(internalUserService.updateInternalUser(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<InternalUserResponse>> response = internalUserController.updateInternalUser(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getInternalUserEmail()).isEqualTo("updated@bekal.org");
        }
    }

    @Nested
    @DisplayName("deleteInternalUser Tests")
    class DeleteInternalUserTests {

        @Test
        @DisplayName("Should delete internal user successfully and return NO_CONTENT")
        void deleteInternalUser_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<InternalUserResponse>> response = internalUserController.deleteInternalUser(id);

            verify(internalUserService).deleteInternalUser(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}