package org.edwin.bekal.domain.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.customer.dto.CreateEmploymentRequest;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateEmploymentRequest;
import org.edwin.bekal.domain.customer.service.EmploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employment")
@RequiredArgsConstructor
public class EmploymentController {

    private final EmploymentService employmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmploymentResponse>> createEmployment(@Valid @RequestBody CreateEmploymentRequest request){
        EmploymentResponse response = employmentService.createEmployment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmploymentResponse>>> getAllEmployment(){
        List<EmploymentResponse> responses = employmentService.getAllEmployment();
        return ResponseEntity.ok(ApiResponse.success("Employment Fetched Successfully", responses));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<EmploymentResponse>> updateEmployment(@PathVariable UUID id, @Valid @RequestBody UpdateEmploymentRequest request){
        EmploymentResponse response = employmentService.updateEmployment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employment Updated Successfully", response));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<EmploymentResponse>> deleteEmployment(@PathVariable UUID id){
        employmentService.deleteEmployment(id);
        return ResponseEntity.noContent().build();
    }
}
