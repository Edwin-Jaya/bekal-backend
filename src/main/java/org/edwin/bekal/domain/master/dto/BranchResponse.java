package org.edwin.bekal.domain.master.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private UUID id;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private String branchCity;
    private String branchStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;

}
