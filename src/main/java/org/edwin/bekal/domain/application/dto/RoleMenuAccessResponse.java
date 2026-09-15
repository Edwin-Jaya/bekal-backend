package org.edwin.bekal.domain.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor // <-- restores the 9-arg constructor Lombok used to generate implicitly
public class RoleMenuAccessResponse implements Serializable {

    @NotNull
    UUID menuId;
    private String menuName;
    @NotNull
    Boolean roleMenuCanView;
    @NotNull
    Boolean roleMenuCanCreate;
    @NotNull
    Boolean roleMenuCanEdit;
    @NotNull
    Boolean roleMenuCanDelete;
    @NotNull
    Boolean roleMenuCanApprove;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;

    // Explicit extra constructor JUST for the JPQL "new" projection (7 fields, no timestamps)
    public RoleMenuAccessResponse(UUID menuId, String menuName,
                                  Boolean roleMenuCanView, Boolean roleMenuCanCreate,
                                  Boolean roleMenuCanEdit, Boolean roleMenuCanDelete,
                                  Boolean roleMenuCanApprove) {
        this(menuId, menuName, roleMenuCanView, roleMenuCanCreate,
                roleMenuCanEdit, roleMenuCanDelete, roleMenuCanApprove, null, null);
    }
}