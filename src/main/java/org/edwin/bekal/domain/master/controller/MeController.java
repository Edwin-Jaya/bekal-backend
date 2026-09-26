package org.edwin.bekal.domain.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.config.OpenApiConfig;
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
@Tag(name = "Current User (Me)", description = "Endpoints for the currently authenticated user to retrieve assigned menus and user context")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class MeController {

    private final DynamicMenuService dynamicMenuService;

    @Operation(summary = "Get My Accessible Menus", description = "Retrieves hierarchical menu navigation tree accessible by the current user based on their roles.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Menus retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid token")
    })
    @GetMapping("/menus")
    public ResponseEntity<List<UserMenuResponse>> getMyMenus(Authentication authentication) {
        return ResponseEntity.ok(dynamicMenuService.getMenusForCurrentUser(authentication));
    }
}
