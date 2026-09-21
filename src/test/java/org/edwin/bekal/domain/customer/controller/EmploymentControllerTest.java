package org.edwin.bekal.domain.customer.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.customer.dto.CreateEmploymentRequest;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateEmploymentRequest;
import org.edwin.bekal.domain.customer.service.EmploymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmploymentControllerTest {

    @Mock
    private EmploymentService employmentService;

    @InjectMocks
    private EmploymentController employmentController;

    @Nested
    @DisplayName("CRUD Employment Tests")
    class CrudEmploymentTests {

        @Test
        @DisplayName("createEmployment - Should create employment record and return CREATED")
        void createEmployment_success() {
            CreateEmploymentRequest request = new CreateEmploymentRequest();
            EmploymentResponse responseDto = EmploymentResponse.builder().customerCompanyName("PT Bekal").build();

            given(employmentService.createEmployment(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<EmploymentResponse>> response = employmentController.createEmployment(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerCompanyName()).isEqualTo("PT Bekal");
        }

        @Test
        @DisplayName("getAllEmployment - Should return list of employment records")
        void getAllEmployment_success() {
            List<EmploymentResponse> responseList = List.of(
                    EmploymentResponse.builder().customerCompanyName("PT Bekal").build(),
                    EmploymentResponse.builder().customerCompanyName("PT Indonesia").build()
            );

            given(employmentService.getAllEmployment()).willReturn(responseList);

            ResponseEntity<ApiResponse<List<EmploymentResponse>>> response = employmentController.getAllEmployment();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(2);
        }

        @Test
        @DisplayName("updateEmployment - Should update employment record and return CREATED")
        void updateEmployment_success() {
            UUID id = UUID.randomUUID();
            UpdateEmploymentRequest request = new UpdateEmploymentRequest();
            EmploymentResponse responseDto = EmploymentResponse.builder().id(id).customerJobTitle("Software Engineer").build();

            given(employmentService.updateEmployment(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<EmploymentResponse>> response = employmentController.updateEmployment(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getCustomerJobTitle()).isEqualTo("Software Engineer");
        }

        @Test
        @DisplayName("deleteEmployment - Should delete employment record and return NO_CONTENT")
        void deleteEmployment_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<EmploymentResponse>> response = employmentController.deleteEmployment(id);

            verify(employmentService).deleteEmployment(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}