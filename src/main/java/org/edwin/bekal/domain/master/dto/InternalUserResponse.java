package org.edwin.bekal.domain.master.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;
import org.edwin.bekal.domain.master.entity.Branch;
import org.edwin.bekal.domain.master.entity.Role;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class InternalUserResponse {
    private UUID id;

    private Branch branch;

    private Role role;

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
