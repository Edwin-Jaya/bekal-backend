package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRequest {

    @NotNull(message = "Loan ID tidak boleh null")
    private UUID loanId;

    @NotNull(message = "Jumlah pembayaran tidak boleh null")
    @Positive(message = "Jumlah pembayaran harus lebih dari 0")
    private BigDecimal amountPaid;

    @NotBlank(message = "Metode pembayaran wajib diisi")
    private String paymentMethod;

    @NotBlank(message = "Referensi transaksi wajib diisi")
    private String transactionReference;
}
