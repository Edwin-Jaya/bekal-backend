package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateBankAccountRequest {

    @Size(max = 100, message = "Nama bank maksimal 100 karakter")
    private String bankName;

    @Size(max = 30, message = "Nomor rekening maksimal 30 karakter")
    private String bankAccountNumber;

    @Size(max = 100, message = "Nama pemilik rekening maksimal 100 karakter")
    private String bankAccountHolder;

    private Boolean isPrimary;
    private String status;
}
