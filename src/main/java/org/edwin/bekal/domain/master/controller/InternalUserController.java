package org.edwin.bekal.domain.master.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.application.dto.CacheablePage;
import org.edwin.bekal.domain.master.dto.*;
import org.edwin.bekal.domain.master.service.InternalUserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal-user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class InternalUserController {

    private final InternalUserService internalUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<InternalUserResponse>> createInternalUser(@Valid @RequestBody CreateInternalUserRequest request){
        InternalUserResponse response = internalUserService.createInternalUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User Created Successfully", response));
    }

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<InternalUserResponse>>> getAllInternalUser(){
//        List<InternalUserResponse> responses = internalUserService.getAllInternalUser();
//        return ResponseEntity.ok(ApiResponse.success("User Fetched Successfully", responses));
//    }

    @GetMapping
    public ResponseEntity<CacheablePage<InternalUserResponse>> getInternalUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(internalUserService.getInternalUser(page, size, status));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> updateInternalUser(@PathVariable UUID id, @Valid @RequestBody UpdateInternalUserRequest request){
        InternalUserResponse response = internalUserService.updateInternalUser(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<InternalUserResponse>> deleteInternalUser(@PathVariable UUID id){
        internalUserService.deleteInternalUser(id);
        return ResponseEntity.noContent().build();
    }
}
