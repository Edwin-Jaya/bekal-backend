package org.edwin.bekal.domain.customer.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
public class UpdateDocumentRequest implements Serializable {

    @Size(max = 30)
    private String documentType;

    @Size(max = 500)
    private String fileUrl;

    private Boolean isLatest;
}