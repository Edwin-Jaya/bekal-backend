package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.domain.master.dto.UserMenuResponse;
import org.edwin.bekal.domain.master.service.DynamicMenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MeControllerTest {

    @Mock
    private DynamicMenuService dynamicMenuService;

    @InjectMocks
    private MeController meController;

    @Nested
    @DisplayName("getMyMenus Tests")
    class GetMyMenusTests {

        @Test
        @DisplayName("Should return user menus for current authenticated user successfully")
        void getMyMenus_success() {
            Authentication authentication = mock(Authentication.class);
            List<UserMenuResponse> menuResponses = List.of(
                    UserMenuResponse.builder().name("Dashboard").build(), // Diperbarui dari menuName menjadi name
                    UserMenuResponse.builder().name("Customers").build()
            );

            given(dynamicMenuService.getMenusForCurrentUser(authentication)).willReturn(menuResponses);

            ResponseEntity<List<UserMenuResponse>> response = meController.getMyMenus(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getName()).isEqualTo("Dashboard"); // Diperbarui dari getMenuName() menjadi getName()
        }
    }
}