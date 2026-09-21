package org.edwin.bekal.domain.master.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.CreateMenuRequest;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.edwin.bekal.domain.master.dto.UpdateMenuRequest;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.repository.MenuRepository;
import org.edwin.bekal.domain.master.service.MenuService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    @Override
    @Transactional
    @CacheEvict(value = "menus", allEntries = true) // ✅ evict on create
    public MenuResponse createMenu(CreateMenuRequest request) {
        Menu menu = new Menu();
        menu.setMenuName(request.getMenuName());
        menu.setMenuPath(request.getMenuPath());
        menu.setMenuIcon(request.getMenuIcon());
        menu.setMenuSortOrder(request.getMenuSortOrder());
        menu.setMenuIsActive(request.getMenuIsActive());
        if (request.getMenuParent() != null) {
            Menu parent = menuRepository.findById(request.getMenuParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Parents not found with ID: " + request.getMenuParent().getId()));
            menu.setMenuParent(parent);
        }

        Menu saved = menuRepository.saveAndFlush(menu);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = "menus",
            key = "'page_' + #page + '_size_' + #size + '_status_' + #status"
    )
    public CacheablePage<MenuResponse> getMenu(int page, int size, Boolean status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Menu> menuPage = status != null
                ? menuRepository.findByMenuIsActive(status, pageable)
                : menuRepository.findAll(pageable);

        return CacheablePage.from(menuPage.map(this::mapToResponse)); // ✅
    }

    @Override
    @Transactional
    @CacheEvict(value = "menus", allEntries = true) // ✅ evict on update
    public MenuResponse updateMenu(UUID id, UpdateMenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with ID" + id));
        menu.setMenuName(request.getMenuName());
        menu.setMenuPath(request.getMenuPath());
        menu.setMenuIcon(request.getMenuIcon());
        menu.setMenuSortOrder(request.getMenuSortOrder());
        menu.setMenuIsActive(request.getMenuIsActive());
        if (request.getMenuParent() != null) {
            Menu parent = menuRepository.findById(request.getMenuParent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Parents not found with ID: " + request.getMenuParent().getId()));
            menu.setMenuParent(parent);
        }

        Menu updated = menuRepository.saveAndFlush(menu);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menus", allEntries = true) // ✅ evict on delete
    public void deleteMenu(UUID id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found with ID: " + id));
        menu.setMenuIsActive(false);
        menuRepository.save(menu);
    }

    public MenuResponse mapToResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .menuParent(menu.getMenuParent())
                .menuName(menu.getMenuName())
                .menuPath(menu.getMenuPath())
                .menuIcon(menu.getMenuIcon())
                .menuSortOrder(menu.getMenuSortOrder())
                .menuIsActive(menu.getMenuIsActive())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}