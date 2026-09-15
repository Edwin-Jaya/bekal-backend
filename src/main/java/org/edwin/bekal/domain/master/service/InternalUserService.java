package org.edwin.bekal.domain.master.service;

import org.edwin.bekal.domain.master.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface InternalUserService {
    InternalUserResponse createInternalUser(CreateInternalUserRequest request);
    List<InternalUserResponse> getAllInternalUser();
    String generateEmployeeCode();
    Page<InternalUserResponse> getInternalUser(int page, int size, Boolean status);
    InternalUserResponse updateInternalUser(UUID id, UpdateInternalUserRequest request);
    void deleteInternalUser(UUID id);
    void activateInternalUser(UUID id);
}
