package org.edwin.bekal.domain.auth.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}
