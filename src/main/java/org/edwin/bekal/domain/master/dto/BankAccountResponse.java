package org.edwin.bekal.domain.master.dto;

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
public class BankAccountResponse {

    private UUID id;
    private UUID customerId;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private Boolean isPrimary;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
