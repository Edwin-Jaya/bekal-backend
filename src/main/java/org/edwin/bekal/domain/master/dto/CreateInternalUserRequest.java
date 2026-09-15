package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.Role;

import java.util.UUID;

@Data
public class CreateInternalUserRequest {
    @NotBlank(message = "Employee code cannot be empty")
    @Size(max=30, message = "Employee code max 30 characters")
    private String internalUserEmployeeCode;

    private String internalUserFullName;

    private String internalUserEmail;

    private String internalUserPasswordHash;

    private String internalUserPhoneNumber;

    private UUID roleId;

    private UUID branchId;

    private Boolean internalUserIsActive;
}
