package org.edwin.bekal.domain.customer.service;

import org.edwin.bekal.domain.customer.dto.*;

import java.util.List;
import java.util.UUID;

public interface EmploymentService {
    EmploymentResponse createEmployment(CreateEmploymentRequest request);
    List<EmploymentResponse> getAllEmployment();
    EmploymentResponse updateEmployment(UUID id, UpdateEmploymentRequest request);
    void deleteEmployment(UUID id);
}
