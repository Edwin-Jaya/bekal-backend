package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBranchRequest {
    @NotBlank(message = "Branch code cannot be empty")
    @Size(max=20, message = "Branch code max 20 characters")
    private String branchCode;

    @NotBlank(message = "Branch name cannot be empty")
    private String branchName;

    private String branchAddress;

    private String branchCity;
}
