package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse implements Serializable {

    private UUID id;
    private UUID customerId;
    private String documentType;
    private String fileUrl;
    private String fileHash;
    private String status;
    private UUID verifiedById;
    private Instant verifiedAt;
    private String rejectionReason;
    private Boolean isLatest;
    private Instant uploadedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;
}