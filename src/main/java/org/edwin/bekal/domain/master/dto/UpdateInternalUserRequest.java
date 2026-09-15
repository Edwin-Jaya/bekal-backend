package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateInternalUserRequest {

    @NotBlank(message = "Employee code cannot be empty")
    @Size(max=30, message = "Employee code max 30 characters")
    private String internalUserEmployeeCode;

    @NotBlank(message = "Internal user full name cannot be empty")
    private String internalUserFullName;

    @NotBlank(message = "Internal user email cannot be empty")
    private String internalUserEmail;

    // Hapus @NotBlank jika password tidak wajib diisi saat update
    private String internalUserPasswordHash;

    @NotBlank(message = "Internal user phone cannot be empty")
    private String internalUserPhoneNumber;

    @NotNull(message = "Internal user role cannot be empty")
    private UUID roleId;

    @NotNull(message = "Internal user branch cannot be empty")
    private UUID branchId;

    @NotNull(message = "Internal user status cannot be empty")
    private Boolean internalUserIsActive;
}