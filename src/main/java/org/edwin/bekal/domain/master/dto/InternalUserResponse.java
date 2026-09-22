package org.edwin.bekal.domain.master.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponse {
    private UUID id;

    private BranchResponse branch;

    private RoleResponse role;

    private String internalUserEmployeeCode;

    private String internalUserFullName;

    private String internalUserEmail;

    private String internalUserPasswordHash;

    private String internalUserPhoneNumber;

    private Boolean internalUserIsActive=true;

    private Instant internalUserLastLoginAt;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;

}
