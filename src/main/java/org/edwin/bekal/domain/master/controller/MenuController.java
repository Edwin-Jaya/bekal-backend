package org.edwin.bekal.domain.master.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody CreateMenuRequest request){
        MenuResponse response = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu Created Successfully", response));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(@PathVariable UUID id, @Valid @RequestBody UpdateMenuRequest request){
        MenuResponse response = menuService.updateMenu(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> deleteMenu(@PathVariable UUID id){
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CacheablePage<MenuResponse>> getMenu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(menuService.getMenu(page, size, status));
    }
}
