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

    @Size(max = 64)
    private String fileHash;

    @Size(max = 20)
    private String status;

    private UUID verifiedById;
    private Instant verifiedAt;

    @Size(max = 255)
    private String rejectionReason;

    private Boolean isLatest;
}