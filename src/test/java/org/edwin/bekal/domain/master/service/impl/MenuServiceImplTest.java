package org.edwin.bekal.domain.master.service.impl;

import org.edwin.bekal.domain.master.dto.CreateMenuRequest;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.edwin.bekal.domain.master.dto.UpdateMenuRequest;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.repository.MenuRepository;
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
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Nested
    @DisplayName("createMenu Tests")
    class CreateMenuTests {

        @Test
        @DisplayName("Should create menu successfully without parent")
        void createMenu_success() {
            CreateMenuRequest request = new CreateMenuRequest();
            request.setMenuName("Dashboard");
            request.setMenuPath("/dashboard");
            request.setMenuIsActive(true);

            given(menuRepository.saveAndFlush(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

            MenuResponse response = menuService.createMenu(request);

            assertThat(response).isNotNull();
            assertThat(response.getMenuName()).isEqualTo("Dashboard");
            assertThat(response.getMenuIsActive()).isTrue();
        }
    }
    @Nested
    @DisplayName("Menu Parent and Query Edge Cases")
    class MenuParentAndQueryEdgeCases {

        @Test
        @DisplayName("Should create menu successfully with valid parent")
        void createMenu_withParent_success() {
            UUID parentId = UUID.randomUUID();
            Menu parentMenu = new Menu();
            parentMenu.setId(parentId);

            CreateMenuRequest request = new CreateMenuRequest();
            request.setMenuName("Sub Menu");
            request.setMenuPath("/sub");
            request.setMenuIsActive(true);
            request.setMenuParent(parentMenu);

            given(menuRepository.findById(parentId)).willReturn(Optional.of(parentMenu));
            given(menuRepository.saveAndFlush(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

            MenuResponse response = menuService.createMenu(request);

            assertThat(response).isNotNull();
            assertThat(response.getMenuName()).isEqualTo("Sub Menu");
        }

        @Test
        @DisplayName("Should throw exception when parent menu not found during creation")
        void createMenu_parentNotFound_throwsException() {
            UUID parentId = UUID.randomUUID();
            Menu parentMenu = new Menu();
            parentMenu.setId(parentId);

            CreateMenuRequest request = new CreateMenuRequest();
            request.setMenuParent(parentMenu);

            given(menuRepository.findById(parentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.createMenu(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Parents not found with ID: " + parentId);
        }

        @Test
        @DisplayName("Should return paginated menus filtered by status")
        void getMenu_withStatus_success() {
            Menu menu = new Menu();
            Page<Menu> page = new PageImpl<>(List.of(menu));

            given(menuRepository.findByMenuIsActive(any(Boolean.class), any(Pageable.class))).willReturn(page);

            Page<MenuResponse> result = menuService.getMenu(0, 10, true);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent menu")
        void deleteMenu_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            given(menuRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.deleteMenu(id))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Menu not found with ID: " + id);
        }
    }

    @Nested
    @DisplayName("updateMenu Tests")
    class UpdateMenuTests {

        @Test
        @DisplayName("Should update menu successfully")
        void updateMenu_success() {
            UUID id = UUID.randomUUID();
            UpdateMenuRequest request = new UpdateMenuRequest();
            request.setMenuName("Updated Menu");
            request.setMenuPath("/updated");
            request.setMenuIsActive(true);

            Menu existing = new Menu();
            existing.setId(id);
            existing.setMenuName("Old Menu");

            given(menuRepository.findById(id)).willReturn(Optional.of(existing));
            given(menuRepository.saveAndFlush(any(Menu.class))).willAnswer(inv -> inv.getArgument(0));

            MenuResponse response = menuService.updateMenu(id, request);

            assertThat(response).isNotNull();
            assertThat(response.getMenuName()).isEqualTo("Updated Menu");
        }

        @Test
        @DisplayName("Should throw exception when menu not found for update")
        void updateMenu_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            UpdateMenuRequest request = new UpdateMenuRequest();

            given(menuRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.updateMenu(id, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Menu not found with ID" + id);
        }
    }


    @Nested
    @DisplayName("getMenu & deleteMenu Tests")
    class GetAndDeleteMenuTests {

        @Test
        @DisplayName("Should return paginated menus")
        void getMenu_success() {
            Menu menu = new Menu();
            menu.setMenuName("Settings");
            Page<Menu> page = new PageImpl<>(List.of(menu));

            given(menuRepository.findAll(any(Pageable.class))).willReturn(page);

            Page<MenuResponse> result = menuService.getMenu(0, 10, null);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMenuName()).isEqualTo("Settings");
        }

        @Test
        @DisplayName("Should soft delete menu successfully")
        void deleteMenu_success() {
            UUID id = UUID.randomUUID();
            Menu menu = new Menu();
            menu.setId(id);
            menu.setMenuIsActive(true);

            given(menuRepository.findById(id)).willReturn(Optional.of(menu));

            menuService.deleteMenu(id);

            assertThat(menu.getMenuIsActive()).isFalse();
        }
    }
}