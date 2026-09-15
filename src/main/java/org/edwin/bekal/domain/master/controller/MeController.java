package org.edwin.bekal.domain.master.controller;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.domain.master.dto.UserMenuResponse;
import org.edwin.bekal.domain.master.service.DynamicMenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final DynamicMenuService dynamicMenuService;

    @GetMapping("/menus")
    public ResponseEntity<List<UserMenuResponse>> getMyMenus(Authentication authentication) {
        // ID/Role diambil langsung dari konteks Spring Security yang aman
        return ResponseEntity.ok(dynamicMenuService.getMenusForCurrentUser(authentication));
    }
}
