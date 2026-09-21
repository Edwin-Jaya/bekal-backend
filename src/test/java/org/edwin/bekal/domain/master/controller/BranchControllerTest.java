package org.edwin.bekal.domain.master.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.domain.master.service.BranchService;
import org.edwin.bekal.enums.BranchStatus;
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
class BranchControllerTest {

    @Mock
    private BranchService branchService;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchController branchController;

    @Nested
    @DisplayName("getActiveBranches Tests")
    class GetActiveBranchesTests {

        @Test
        @DisplayName("Should return list of active branch responses")
        void getActiveBranches_success() {
            Branch branch = new Branch();
            branch.setId(UUID.randomUUID());
            branch.setBranchCode("JKT01");
            branch.setBranchName("Jakarta Pusat");
            branch.setBranchCity("Jakarta");
            branch.setBranchStatus(BranchStatus.ACTIVE);

            given(branchRepository.findByBranchStatus(BranchStatus.ACTIVE)).willReturn(List.of(branch));

            ResponseEntity<List<BranchResponse>> response = branchController.getActiveBranches();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getBranchCode()).isEqualTo("JKT01");
        }
    }

    @Nested
    @DisplayName("CRUD Branch Tests")
    class CrudBranchTests {

        @Test
        @DisplayName("createBranch - Should return CREATED status and BranchResponse")
        void createBranch_success() {
            CreateBranchRequest request = new CreateBranchRequest();
            BranchResponse responseDto = BranchResponse.builder().branchName("Bandung Branch").build();

            given(branchService.createBranch(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BranchResponse>> response = branchController.createBranch(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getBranchName()).isEqualTo("Bandung Branch");
        }

        @Test
        @DisplayName("getAllBranches - Should return list of branches")
        void getAllBranches_success() {
            List<BranchResponse> list = List.of(
                    BranchResponse.builder().branchName("Branch 1").build(),
                    BranchResponse.builder().branchName("Branch 2").build()
            );

            given(branchService.getAllBranches()).willReturn(list);

            ResponseEntity<ApiResponse<List<BranchResponse>>> response = branchController.getAllBranches();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(2);
        }

        @Test
        @DisplayName("updateBranch - Should update branch and return CREATED status")
        void updateBranch_success() {
            UUID id = UUID.randomUUID();
            UpdateBranchRequest request = new UpdateBranchRequest();
            BranchResponse responseDto = BranchResponse.builder().id(id).branchName("Updated Branch").build();

            given(branchService.updateBranch(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<BranchResponse>> response = branchController.updateBranch(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getBranchName()).isEqualTo("Updated Branch");
        }

        @Test
        @DisplayName("deleteBranch - Should delete branch and return NO_CONTENT")
        void deleteBranch_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<BranchResponse>> response = branchController.deleteBranch(id);

            verify(branchService).deleteBranch(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}