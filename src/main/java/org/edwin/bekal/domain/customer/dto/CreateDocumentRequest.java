package org.edwin.bekal.domain.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDocumentRequest {

    @NotNull(message = "Customer ID wajib diisi")
    private UUID customerId;

    @NotBlank(message = "Document type wajib diisi")
    @Size(max = 30, message = "Document type maksimal 30 karakter")
    private String documentType;

    // Diisi jika file di-upload terpisah (misal ke S3) dan client hanya kirim URL
    @Size(max = 500)
    private String fileUrl;
}