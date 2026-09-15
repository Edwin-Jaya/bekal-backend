package org.edwin.bekal.domain.application.controller;

import org.edwin.bekal.domain.application.dto.AssignRoleMenuRequest;
import org.edwin.bekal.domain.application.dto.CreateRoleMenuAccessRequest;
import org.edwin.bekal.domain.application.dto.RoleMenuAccessResponse;
import org.edwin.bekal.domain.application.entity.RoleMenuAccess;
import org.edwin.bekal.domain.application.service.RoleMenuAccessService;
import org.edwin.bekal.domain.master.dto.MenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/role-menu-access")
public class RoleMenuAccessController {

    private final RoleMenuAccessService roleMenuAccessService;

    public RoleMenuAccessController(RoleMenuAccessService roleMenuAccessService) {
        this.roleMenuAccessService = roleMenuAccessService;
    }

    @GetMapping
    public ResponseEntity<Page<RoleMenuAccessResponse>> getAccess(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        return ResponseEntity.ok(roleMenuAccessService.getAccess(page, size));
    }

//    // 1. Get paginated menu accesses by Role ID
//    @GetMapping("/role/{roleId}")
//    public ResponseEntity<Page<RoleMenuAccess>> getByRoleId(
//            @PathVariable UUID roleId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "menu.menuSortOrder") String sortBy
//    ) {
//        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).ascending());
//        Page<RoleMenuAccess> result = roleMenuAccessService.getByRoleId(roleId, pageable);
//        return ResponseEntity.ok(result);
//    }

    // 2. Fetch active menu trees for currently assigned user roles (Used by Angular Sidebar)
    @GetMapping("/active-menus")
    public ResponseEntity<List<RoleMenuAccess>> getActiveMenusByRoleIds(@RequestParam List<UUID> roleIds) {
        List<RoleMenuAccess> activeMenus = roleMenuAccessService.getActiveMenusByRoleIds(roleIds);
        return ResponseEntity.ok(activeMenus);
    }

    // 3. Check specific role and menu access permission
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAccess(
            @RequestParam UUID roleId,
            @RequestParam UUID menuId
    ) {
        boolean exists = roleMenuAccessService.existsByRoleIdAndMenuId(roleId, menuId);
        return ResponseEntity.ok(exists);
    }

    // 4. Reset or delete all menu access records for a role
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity<Void> deleteByRoleId(@PathVariable UUID roleId) {
        roleMenuAccessService.deleteByRoleId(roleId);
        return ResponseEntity.noContent().build();
    }

    // 5. Single Add / Update
    @PostMapping
    public ResponseEntity<RoleMenuAccess> create(@RequestBody RoleMenuAccess roleMenuAccess) {
        RoleMenuAccess saved = roleMenuAccessService.save(roleMenuAccess);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/role/{roleId}")
    public ResponseEntity<Page<RoleMenuAccessResponse>> getMatrixByRole(
            @PathVariable UUID roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RoleMenuAccessResponse> response = roleMenuAccessService.getMatrixByRoleId(roleId, page, size);
        return ResponseEntity.ok(response);
    }

    // 6. Bulk Assign / Sync Permission Matrix (Paling sering digunakan di FE)
    @PostMapping("/assign/{roleId}")
    public ResponseEntity<List<RoleMenuAccessResponse>> assignMenusToRole(
            @PathVariable UUID roleId,
            @RequestBody List<CreateRoleMenuAccessRequest> requests
    ) {
        List<RoleMenuAccessResponse> response = roleMenuAccessService.assignMenusToRole(roleId, requests);
        return ResponseEntity.ok(response);
    }
}
