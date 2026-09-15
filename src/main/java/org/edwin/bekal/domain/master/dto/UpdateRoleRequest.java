package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    @NotBlank(message = "Role name cannot be empty")
    private String roleName;

    @NotBlank(message = "Role description cannot be empty")
    private String roleDescription;

    @NotBlank(message = "Role status cannot be empty")
    private Boolean roleIsActive;
}
