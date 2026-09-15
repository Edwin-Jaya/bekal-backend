package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateBranchRequest {

    @NotBlank(message = "Branch code cannot be empty")
    @Size(max = 20, message = "Branch code max 20 characters")
    private String branchCode;

    @NotBlank(message = "Branch name cannot be empty")
    private String branchName;

    @NotBlank(message = "Branch address cannot be empty")
    private String branchAddress;

    @NotBlank(message = "Branch city cannot be empty")
    private String branchCity;

    @NotBlank(message = "Branch status cannot be empty")
    private String branchStatus;
}
