package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequest {
    @NotBlank(message = "Role name cannot be empty")
    @Size(max=50, message = "Role name max 50 characters")
    private String roleName;

    @NotBlank(message = "Role description cannot be empty")
    private String roleDescription;

    private Boolean roleIsActive;
}
