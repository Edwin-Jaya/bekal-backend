package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.edwin.bekal.domain.master.entity.Menu;

import java.io.Serializable;

/**
 * DTO for {@link org.edwin.bekal.domain.master.entity.Menu}
 */
@Data
public class CreateMenuRequest implements Serializable {
    Menu menuParent;
    @NotNull
    @Size(max = 100)
    String menuName;
    @Size(max = 150)
    String menuPath;
    @Size(max = 50)
    String menuIcon;
    @NotNull
    Integer menuSortOrder;
    @NotNull
    Boolean menuIsActive;
}