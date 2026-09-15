package org.edwin.bekal.domain.master.service;

import org.edwin.bekal.domain.master.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MenuService {
    MenuResponse createMenu(CreateMenuRequest request);
    Page<MenuResponse> getMenu(int page, int size, Boolean status);
    MenuResponse updateMenu(UUID id, UpdateMenuRequest request);
    void deleteMenu(UUID id);
}
