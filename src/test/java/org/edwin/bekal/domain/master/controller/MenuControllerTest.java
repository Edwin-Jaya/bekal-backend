package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.CreateMenuRequest;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.edwin.bekal.domain.master.dto.UpdateMenuRequest;
import org.edwin.bekal.domain.master.service.MenuService;
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
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @Nested
    @DisplayName("createMenu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("Should create menu successfully and return CREATED")
        void createMenu_success() {
            CreateMenuRequest request = new CreateMenuRequest();
            MenuResponse responseDto = MenuResponse.builder().menuName("Dashboard").build();

            given(menuService.createMenu(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<MenuResponse>> response = menuController.createMenu(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getMenuName()).isEqualTo("Dashboard");
        }
    }

    @Nested
    @DisplayName("updateMenu Tests")
    class UpdateMenuTests {

        @Test
        @DisplayName("Should update menu successfully and return CREATED")
        void updateMenu_success() {
            UUID id = UUID.randomUUID();
            UpdateMenuRequest request = new UpdateMenuRequest();
            MenuResponse responseDto = MenuResponse.builder().id(id).menuName("Updated Menu").build();

            given(menuService.updateMenu(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<MenuResponse>> response = menuController.updateMenu(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getMenuName()).isEqualTo("Updated Menu");
        }
    }

    @Nested
    @DisplayName("deleteMenu Tests")
    class DeleteMenuTests {

        @Test
        @DisplayName("Should delete menu successfully and return NO_CONTENT")
        void deleteMenu_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<MenuResponse>> response = menuController.deleteMenu(id);

            verify(menuService).deleteMenu(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Nested
    @DisplayName("getMenus Tests")
    class GetMenusTests {

        @Test
        @DisplayName("Should return paginated menus successfully")
        void getMenus_success() {
            Page<MenuResponse> pageResponse = new PageImpl<>(List.of(
                    MenuResponse.builder().menuName("Settings").build()
            ));

            given(menuService.getMenu(0, 10, true)).willReturn(pageResponse);

            ResponseEntity<Page<MenuResponse>> response = menuController.getMenus(0, 10, true);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getMenuName()).isEqualTo("Settings");
        }
    }
}