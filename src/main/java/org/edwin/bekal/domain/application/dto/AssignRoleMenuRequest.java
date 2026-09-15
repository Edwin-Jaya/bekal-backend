package org.edwin.bekal.domain.application.dto;

import java.util.List;
import java.util.UUID;

public record AssignRoleMenuRequest(
        UUID roleId,
        List<UUID> menuIds
) {}
