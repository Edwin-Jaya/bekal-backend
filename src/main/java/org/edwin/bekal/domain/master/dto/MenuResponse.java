package org.edwin.bekal.domain.master.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.edwin.bekal.domain.master.entity.Menu;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link org.edwin.bekal.domain.master.entity.Menu}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse implements Serializable {
    UUID id;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;
}