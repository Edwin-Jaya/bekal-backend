package org.edwin.bekal.domain.master.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request){
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role Created Successfully", response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRole(){
        List<RoleResponse> responses = roleService.getAllRole();
        return ResponseEntity.ok(ApiResponse.success("Roles Fetched Successfully", responses));
    }

    @GetMapping
    public ResponseEntity<Page<RoleResponse>> getRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean status) {

        return ResponseEntity.ok(roleService.getRole(page, size, status));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request){
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> deleteRole(@PathVariable UUID id){
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
