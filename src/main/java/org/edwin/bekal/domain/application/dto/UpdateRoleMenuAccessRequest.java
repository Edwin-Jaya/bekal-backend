package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.entity.Role;

import java.io.Serializable;

/**
 * DTO for {@link org.edwin.bekal.domain.application.entity.RoleMenuAccess}
 */
@Data
public class UpdateRoleMenuAccessRequest implements Serializable {
    @NotNull
    Role role;
    @NotNull
    Menu menu;
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