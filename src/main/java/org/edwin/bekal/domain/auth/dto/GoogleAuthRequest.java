package org.edwin.bekal.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @JsonProperty("firebase_token")
    private String firebaseToken;
}
