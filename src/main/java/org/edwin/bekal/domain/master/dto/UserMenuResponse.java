package org.edwin.bekal.domain.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuResponse {
    private UUID id;         // Changed from Long to UUID
    private UUID parentId;   // Changed from Long to UUID
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canApprove;
    private List<UserMenuResponse> children;
}
