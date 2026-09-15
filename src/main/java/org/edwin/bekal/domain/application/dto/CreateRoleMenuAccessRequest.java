package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.entity.Role;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.edwin.bekal.domain.application.entity.RoleMenuAccess}
 */
@Data
public class CreateRoleMenuAccessRequest implements Serializable {

    @NotNull(message = "Menu ID is required")
    private UUID menuId;
    
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
}