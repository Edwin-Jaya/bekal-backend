package org.edwin.bekal.domain.master.service.impl;

import org.edwin.bekal.domain.master.dto.BranchResponse;
import org.edwin.bekal.domain.master.dto.CreateBranchRequest;
import org.edwin.bekal.domain.master.dto.UpdateBranchRequest;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.repository.BranchRepository;
import org.edwin.bekal.enums.BranchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchServiceImpl branchService;

    @Nested
    @DisplayName("createBranch Tests")
    class CreateBranchTests {

        @Test
        @DisplayName("Should create branch successfully when branch code is unique")
        void createBranch_success() {
            CreateBranchRequest request = new CreateBranchRequest();
            request.setBranchCode("JKT01");
            request.setBranchName("Jakarta Central");
            request.setBranchCity("Jakarta");
            request.setBranchAddress("Jl. Sudirman");

            given(branchRepository.existsByBranchCode("JKT01")).willReturn(false);
            given(branchRepository.saveAndFlush(any(Branch.class))).willAnswer(inv -> inv.getArgument(0));

            BranchResponse response = branchService.createBranch(request);

            assertThat(response).isNotNull();
            assertThat(response.getBranchCode()).isEqualTo("JKT01");
            assertThat(response.getBranchStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("Should throw exception when branch code already exists")
        void createBranch_duplicateCode_throwsException() {
            CreateBranchRequest request = new CreateBranchRequest();
            request.setBranchCode("JKT01");

            given(branchRepository.existsByBranchCode("JKT01")).willReturn(true);

            assertThatThrownBy(() -> branchService.createBranch(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Branch code already exists!");
        }
    }

    @Nested
    @DisplayName("updateBranch Tests")
    class UpdateBranchTests {

        @Test
        @DisplayName("Should update branch successfully when branch exists")
        void updateBranch_success() {
            UUID id = UUID.randomUUID();
            UpdateBranchRequest request = new UpdateBranchRequest();
            request.setBranchCode("JKT01");
            request.setBranchName("Jakarta Updated");
            request.setBranchCity("Jakarta");

            Branch existing = new Branch();
            existing.setId(id);
            existing.setBranchCode("JKT01");
            existing.setBranchStatus(BranchStatus.ACTIVE);

            given(branchRepository.findById(id)).willReturn(Optional.of(existing));
            given(branchRepository.saveAndFlush(any(Branch.class))).willAnswer(inv -> inv.getArgument(0));

            BranchResponse response = branchService.updateBranch(id, request);

            assertThat(response).isNotNull();
            assertThat(response.getBranchName()).isEqualTo("Jakarta Updated");
        }

        @Test
        @DisplayName("Should throw exception when branch not found for update")
        void updateBranch_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            UpdateBranchRequest request = new UpdateBranchRequest();

            given(branchRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.updateBranch(id, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Branch not found with ID" + id);
        }
    }

    @Nested
    @DisplayName("deleteBranch Tests")
    class DeleteBranchTests {

        @Test
        @DisplayName("Should set branch status to INACTIVE on delete")
        void deleteBranch_success() {
            UUID id = UUID.randomUUID();
            Branch branch = new Branch();
            branch.setId(id);
            branch.setBranchStatus(BranchStatus.ACTIVE);

            given(branchRepository.findById(id)).willReturn(Optional.of(branch));

            branchService.deleteBranch(id);

            assertThat(branch.getBranchStatus()).isEqualTo(BranchStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when branch not found for delete")
        void deleteBranch_notFound_throwsException() {
            UUID id = UUID.randomUUID();
            given(branchRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.deleteBranch(id))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getAllBranches Tests")
    class GetAllBranchesTests {

        @Test
        @DisplayName("Should return all branches")
        void getAllBranches_success() {
            Branch b1 = new Branch();
            b1.setBranchCode("JKT01");
            b1.setBranchStatus(BranchStatus.ACTIVE);
            Branch b2 = new Branch();
            b2.setBranchCode("SBY01");
            b2.setBranchStatus(BranchStatus.ACTIVE);

            given(branchRepository.findAll()).willReturn(List.of(b1, b2));

            List<BranchResponse> responses = branchService.getAllBranches();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getBranchCode()).isEqualTo("JKT01");
            assertThat(responses.get(1).getBranchCode()).isEqualTo("SBY01");
        }
    }
}