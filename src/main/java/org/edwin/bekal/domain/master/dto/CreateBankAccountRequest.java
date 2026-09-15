package org.edwin.bekal.domain.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBankAccountRequest {

    @NotNull(message = "Customer ID wajib diisi")
    private UUID customerId;

    @NotBlank(message = "Nama bank wajib diisi")
    @Size(max = 100, message = "Nama bank maksimal 100 karakter")
    private String bankName;

    @NotBlank(message = "Nomor rekening wajib diisi")
    @Size(max = 30, message = "Nomor rekening maksimal 30 karakter")
    private String bankAccountNumber;

    @NotBlank(message = "Nama pemilik rekening wajib diisi")
    @Size(max = 100, message = "Nama pemilik rekening maksimal 100 karakter")
    private String bankAccountHolder;

    private Boolean isPrimary = false;
}
